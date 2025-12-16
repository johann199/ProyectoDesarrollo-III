import * as cheerio from "cheerio";
import * as iconv from "iconv-lite";
import { CookieJar } from "tough-cookie";
import axios from "axios";
import { wrapper } from "axios-cookiejar-support";
import type { Student } from "../types/student";
import { SiraError } from "../errors/sira.error";

process.env.NODE_TLS_REJECT_UNAUTHORIZED = "0";

/**
 * Logs in to SIRA and retrieves user information.
 * @param user
 * @param password
 * @returns
 */
export async function siraLogin(user: string, password: string): Promise<string> {
  const jar = new CookieJar();
  const client = wrapper(axios.create({ jar, responseType: "arraybuffer" }));
  try {
    const response = await client.post(
      "https://sira.univalle.edu.co/sra/",
      new URLSearchParams({
        redirect: "",
        usu_login_aut: user,
        usu_password_aut: password,
        boton: "Ingresar al Sistema",
      }),
      {
        maxRedirects: 0,
        validateStatus: (status) => !!status && status >= 200 && status < 400,
      }
    );

    const html = iconv.decode(Buffer.from(response.data), "latin1");
    const $ = cheerio.load(html);

    const errorMessage = $(".resaltar").text().trim() || "";
    if (errorMessage) {
      const clean = errorMessage.replace(/^[A-Z]+\s*\d+\s*.*:\s/, "").trim();
      throw new SiraError(clean || "Invalid credentials", 401);
    }

    return jar.getCookieStringSync("https://sira.univalle.edu.co/");
  } catch (error) {
    if (axios.isAxiosError(error)) {
      throw new SiraError("Failed to connect to SIRA portal", 503);
    }

    if (error instanceof SiraError) throw error;

    throw new SiraError("Unexpected server error", 500);
  }
}

export async function isUserStudent(session: string): Promise<boolean> {
  const jar = new CookieJar();
  jar.setCookieSync(session, "https://sira.univalle.edu.co/");
  const client = wrapper(axios.create({ jar, responseType: "arraybuffer" }));
  const response = await client.get(
    "https://sira.univalle.edu.co/sra//paquetes/inicioestudiante/index.php?accion=Inicio"
  );

  const html = iconv.decode(Buffer.from(response.data), "latin1");
  const $ = cheerio.load(html);
  return $('input[name="est_codigo"]').length > 0;
}

/**
 * Fetches student information from SIRA.
 * @param user
 * @param session
 * @returns
 */
export async function getStudentInfo(
  user: string,
  session: string
): Promise<Student> {

  const jar = new CookieJar();
  jar.setCookieSync(session, "https://sira.univalle.edu.co/");
  const client = wrapper(axios.create({ jar, responseType: "arraybuffer" }));
  const response = await client.post(
    "https://sira.univalle.edu.co/sra//paquetes/tablaMaestro/persona/index.php",
    new URLSearchParams({
      accion: "desplegarFmInformacionDeContacto",
      x: "31",
      y: "24"
    }
  )
  );

  const html = iconv.decode(Buffer.from(response.data), "latin1");
  const $ = cheerio.load(html);
  if ($('input[name="per_nombre"]').length === 0) {
    throw new SiraError("Invalid session", 401);
  }
  
  const name =
    $('input[name="per_nombre"]').attr("value") +
    " " +
    $('input[name="per_apellido"]').attr("value");
  const email =
    $('input[name="per_email_institucional"]').attr("value") || "";
  const document = $('input[name="per_doc_ide_numero"]').attr("value") || "";

  const partCode = user.split("-");
  const code = partCode.length > 1 ? partCode[0] : '-';
  const program = partCode.length > 1 ? partCode[1] : '-';

  return {
    name: name.trim(),
    program,
    code,
    email,
    document
  };
}


