import { Router } from "express";
import { pool } from "../db.js";
import { ah } from "../asyncHandler.js";

const router = Router();

const SELECT_COLUMNS = `id, category_id AS "categoryId", month_year AS "monthYear", budget_amount AS "budgetAmount"`;

router.get("/", ah(async (req, res) => {
  const { rows } = await pool.query(
    `SELECT ${SELECT_COLUMNS} FROM budgets WHERE user_id = $1`,
    [req.userId]
  );
  res.json(rows);
}));

router.post("/", ah(async (req, res) => {
  const { id, categoryId, monthYear, budgetAmount } = req.body ?? {};
  if (id == null || categoryId == null || typeof monthYear !== "string" || typeof budgetAmount !== "number") {
    return res.status(400).json({ error: "id, categoryId, monthYear, budgetAmount required" });
  }
  const { rows } = await pool.query(
    `INSERT INTO budgets (user_id, id, category_id, month_year, budget_amount)
     VALUES ($1, $2, $3, $4, $5)
     ON CONFLICT (user_id, id) DO UPDATE SET
       category_id = EXCLUDED.category_id, month_year = EXCLUDED.month_year, budget_amount = EXCLUDED.budget_amount
     RETURNING ${SELECT_COLUMNS}`,
    [req.userId, id, categoryId, monthYear, budgetAmount]
  );
  res.status(201).json(rows[0]);
}));

router.put("/:id", ah(async (req, res) => {
  const { categoryId, monthYear, budgetAmount } = req.body ?? {};
  const { rows } = await pool.query(
    `UPDATE budgets SET category_id = $1, month_year = $2, budget_amount = $3
     WHERE user_id = $4 AND id = $5
     RETURNING ${SELECT_COLUMNS}`,
    [categoryId, monthYear, budgetAmount, req.userId, req.params.id]
  );
  if (!rows[0]) return res.status(404).json({ error: "budget not found" });
  res.json(rows[0]);
}));

router.delete("/:id", ah(async (req, res) => {
  await pool.query("DELETE FROM budgets WHERE user_id = $1 AND id = $2", [req.userId, req.params.id]);
  res.status(204).end();
}));

export default router;
