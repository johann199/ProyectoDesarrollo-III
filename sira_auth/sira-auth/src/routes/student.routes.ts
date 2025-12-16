import express from "express";
import { authMiddleware } from "../middlewares/auth.middleware";
import { getPreviewStudent, getStudent } from "../controllers/student.controller";

const router = express.Router();

router.get("/info", authMiddleware, getStudent);
router.get("/preview/:code", getPreviewStudent);

export default router;
