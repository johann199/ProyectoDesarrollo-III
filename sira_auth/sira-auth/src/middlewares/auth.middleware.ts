import type { Request, Response, NextFunction } from "express";
import { SiraError } from "../errors/sira.error";

export function authMiddleware(req: Request, res: Response, next: NextFunction) {
  const authHeader = req.headers.authorization;
  if (!authHeader?.startsWith("Bearer ")) {
    return res.status(401).json({ message: "Missing token" });
  }

  const token = authHeader.replace("Bearer ", "");

  try {
    const data = JSON.parse(Buffer.from(token, "base64").toString("utf-8"));
    if (!data.user || !data.session) {
      throw new SiraError("Invalid token", 401);
    }
    (req as any).auth = data;
    next();
  } catch (err) {
    console.info(err);
    res.status(401).json({ message: "Invalid token" });
    return;
  }
}
