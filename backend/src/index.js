import express from "express";
import helmet from "helmet";
import cors from "cors";
import rateLimit from "express-rate-limit";
import authRoutes from "./routes/auth.js";
import meRoutes from "./routes/me.js";
import categoryRoutes from "./routes/categories.js";
import transactionRoutes from "./routes/transactions.js";
import budgetRoutes from "./routes/budgets.js";
import { requireAuth } from "./middleware/auth.js";

const app = express();
app.use(helmet());
app.use(cors());
app.use(express.json());

const authLimiter = rateLimit({ windowMs: 15 * 60 * 1000, limit: 20 }); // brute-force guard on login/register/google
app.use("/auth", authLimiter, authRoutes);
app.use("/me", meRoutes);
app.use("/categories", requireAuth, categoryRoutes);
app.use("/transactions", requireAuth, transactionRoutes);
app.use("/budgets", requireAuth, budgetRoutes);

app.get("/health", (_req, res) => res.json({ ok: true }));

// last-resort handler so a thrown error returns 500 instead of crashing the process
app.use((err, _req, res, _next) => {
  console.error(err);
  res.status(500).json({ error: "internal server error" });
});

const port = process.env.PORT || 3000;
app.listen(port, () => console.log(`BudgetBuddy backend listening on ${port}`));
