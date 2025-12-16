import { Router } from "express";
import { authenticate } from "../controllers/auth.controller";

const router = Router();
/**
 * POST /api/auth
 * Body: { user: string, password: string }
 * Description: Authenticates a student using their SIRA credentials.
 */
router.post("/", authenticate);

export default router;
