import * as cheerio from "cheerio";
import { CookieJar } from "tough-cookie";
import axios from "axios";
import { wrapper } from "axios-cookiejar-support";
import type { Student } from "../types/student";
import { SiraError } from "../errors/sira.error";
import md5 from "md5";

process.env.NODE_TLS_REJECT_UNAUTHORIZED = "0";

export default async function getStudentInfoPreview(user: string): Promise<Student> {
  try {
    const jar = new CookieJar();
    const client = wrapper(axios.create({ jar }));
    const response = await client.get("https://opac.univalle.edu.co/cgi-olib/");

    const $ = cheerio.load(response.data);
    const scriptContent = $("script");
    let sessionId = null;
    scriptContent.each((i, elem) => {
      const scriptText = $(elem).html();
      if (scriptText) {
        const match = RegExp(/SessionID\s*=\s*(\d+)/).exec(scriptText);
        if (match) {
          sessionId = match[1];
        }
      }
    });

    if (!sessionId) {
      throw new SiraError("Failed to retrieve session ID from OPAC", 401);
    }

    jar.setCookieSync(
      "cgi-olib_UsesCookies=Notified%3A%209%2F7%2F2025",
      "https://opac.univalle.edu.co/"
    );
    jar.setCookieSync(
      "cgi-olib_SessionID=" + sessionId,
      "https://opac.univalle.edu.co/"
    );
    jar.setCookieSync(
      "cgi-olib_Language=undefined",
      "https://opac.univalle.edu.co/"
    );
    jar.setCookieSync(
      "cgi-olib_lastSearchType=kws2",
      "https://opac.univalle.edu.co/"
    );
    jar.setCookieSync(
      "SessionID=" + sessionId,
      "https://opac.univalle.edu.co/"
    );

    const authentication = sessionId + ":" + user.toUpperCase();
    const authentication2 = authentication;
    const authHash = md5(authentication).toUpperCase();
    const auth2Hash = md5(authentication2).toUpperCase();
    const finalAuth =
      md5(user.toUpperCase()).toUpperCase().slice(-8) + authHash;

    const loginResponse = await client.post(
      "https://opac.univalle.edu.co/cgi-olib/",
      new URLSearchParams({
        action: "authenticate",
        authentication: finalAuth + "-" + auth2Hash,
      }),
      {
        headers: {
          "Content-Type": "application/x-www-form-urlencoded",

          referer: "https://opac.univalle.edu.co/cgi-olib/",
        },
        maxRedirects: 0,
        validateStatus: (status) => !!status && status >= 200 && status < 400,
      }
    );

    const loginHtml = loginResponse.data;
    const $$ = cheerio.load(loginHtml);
    const loginError = $$(".loginForm").text().trim() || "";
    if (loginError) {
      throw new SiraError("Invalid credentials", 401);
    }

    console.log("OPAC login successful for user:", user);
    const nameDetails = $$("ul.nav > li");
    if (nameDetails.length === 0) {
      throw new SiraError("Failed to retrieve user data from OPAC", 500);
    }
    const userResponse = await client.get(
      "https://opac.univalle.edu.co/cgi-olib/?action=getLatestBreadcrumb&defaultType=3",
      {
        headers: {
          "Content-Type": "application/x-www-form-urlencoded",

          referer: "https://opac.univalle.edu.co/cgi-olib/",
        },
      }
    );
    const userHtml = userResponse.data;
    const $$$ = cheerio.load(userHtml);
    const fristName = $$$("#user_fname_text").text().trim() || "";
    const lastName = $$$("#user_sname_text").text().trim() || "";
    const code = $$$("#user_barcode_text").text().trim() || "";
    const program = $$$("#user_depts_text > span").first().attr("code") || "";

    return {
      name: `${fristName} ${lastName}`,
      program: program.replace("P", ""),
      code,
      email: null,
      document: null,
    };
  } catch (error) {
    if (axios.isAxiosError(error)) {
      throw new SiraError(error.message, 503);
    }

    if (error instanceof SiraError) throw error;

    throw new SiraError("Unexpected server error", 500);
  }
}
