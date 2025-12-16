import express from "express";
import authRoutes from "./routes/auth.routes";
import studentRoutes from "./routes/student.routes";

const app = express();


app.use(express.json());
app.use("/api/auth", authRoutes);
app.use("/api/student", studentRoutes);

const PORT = process.env.PORT || 3000;

app.listen(PORT, () => {
  console.log(`🚀 Server running on port ${PORT}`);
});
