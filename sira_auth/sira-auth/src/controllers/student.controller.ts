import type { Request, Response } from "express";
import { SiraError } from "../errors/sira.error";
import { getStudentInfo } from "../services/sira.service";
import getStudentInfoPreview from "../services/opac.service";
export async function getStudent(req: Request, res: Response): Promise<void> {
 
  try {
    const { user, session } = (req as any).auth;
    const student = await getStudentInfo(user, session);
    res.json(student);
  } catch (err: any) {
    if (err instanceof SiraError) {
      res.status(err.statusCode).json({ message: err.message });
    } else {
      res.status(500).json({ message: "Unexpected server error" });
    }
  }
}


export async function getPreviewStudent(req: Request, res: Response): Promise<void> {
  try {
    const code = req.params.code;
    if (!code) {
      res.status(400).json({ message: "Missing code parameter" });
      return;
    }
    const student = await getStudentInfoPreview(code);
    res.json(student);
  } catch (err: any) {
    if (err instanceof SiraError) {
      res.status(err.statusCode).json({ message: err.message });
    } else {
      res.status(500).json({ message: "Unexpected server error" });
    }
  }
}