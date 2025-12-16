import type { Request, Response } from "express";
import { isUserStudent, siraLogin } from "../services/sira.service";
import { SiraError } from "../errors/sira.error";

/**
 * Authenticates a student using their SIRA credentials.
 * @param req - The request object containing student credentials.
 * @param res - The response object to send the authentication result.
 * @returns A promise that resolves to void.
 */
export async function authenticate(req: Request, res: Response) : Promise<void> {
  const {  user, password } = req.body;

  if (!user || !password) {
    res.status(400).json({ message: "Missing credentials" });
    return;
  }

  try {
    const session = await siraLogin(user, password);
    const token = Buffer.from(JSON.stringify({ user, session })).toString("base64");
    const isStudent = await isUserStudent(session);
    res.json({token, isStudent});
  } catch (err: any) {
    if (err instanceof SiraError) {
      res.status(err.statusCode).json({ message: err.message });
    } else {
      res.status(500).json({ message: "Unexpected server error" });
    }
  }
}
