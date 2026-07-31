import { Router } from "express";
import { pool } from "../db.js";
import { ah } from "../asyncHandler.js";

const router = Router();

const SELECT_COLUMNS = `id, amount, type, category_id AS "categoryId", date, note`;

router.get("/", ah(async (req, res) => {
  const { rows } = await pool.query(
    `SELECT ${SELECT_COLUMNS} FROM transactions WHERE user_id = $1 ORDER BY date DESC`,
    [req.userId]
  );
  res.json(rows);
}));

router.post("/", ah(async (req, res) => {
  const { id, amount, type, categoryId, date, note } = req.body ?? {};
  if (id == null || typeof amount !== "number" || typeof type !== "string" || categoryId == null || typeof date !== "string") {
    return res.status(400).json({ error: "id, amount, type, categoryId, date required" });
  }
  const { rows } = await pool.query(
    `INSERT INTO transactions (user_id, id, amount, type, category_id, date, note)
     VALUES ($1, $2, $3, $4, $5, $6, $7)
     ON CONFLICT (user_id, id) DO UPDATE SET
       amount = EXCLUDED.amount, type = EXCLUDED.type, category_id = EXCLUDED.category_id,
       date = EXCLUDED.date, note = EXCLUDED.note
     RETURNING ${SELECT_COLUMNS}`,
    [req.userId, id, amount, type, categoryId, date, note ?? null]
  );
  res.status(201).json(rows[0]);
}));

router.put("/:id", ah(async (req, res) => {
  const { amount, type, categoryId, date, note } = req.body ?? {};
  const { rows } = await pool.query(
    `UPDATE transactions SET amount = $1, type = $2, category_id = $3, date = $4, note = $5
     WHERE user_id = $6 AND id = $7
     RETURNING ${SELECT_COLUMNS}`,
    [amount, type, categoryId, date, note ?? null, req.userId, req.params.id]
  );
  if (!rows[0]) return res.status(404).json({ error: "transaction not found" });
  res.json(rows[0]);
}));

router.delete("/:id", ah(async (req, res) => {
  await pool.query("DELETE FROM transactions WHERE user_id = $1 AND id = $2", [req.userId, req.params.id]);
  res.status(204).end();
}));

export default router;
