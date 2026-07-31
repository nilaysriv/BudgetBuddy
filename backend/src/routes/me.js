import { Router } from "express";
import { pool } from "../db.js";
import { requireAuth } from "../middleware/auth.js";
import { ah } from "../asyncHandler.js";

const router = Router();

const ALLOWED_CURRENCIES = ["INR", "USD", "EUR", "GBP"];

router.get("/", requireAuth, ah(async (req, res) => {
  const { rows } = await pool.query(
    `SELECT id, email, full_name AS "fullName", currency, created_at AS "createdAt"
     FROM users WHERE id = $1`,
    [req.userId]
  );
  if (!rows[0]) return res.status(404).json({ error: "user not found" });
  res.json(rows[0]);
}));

router.patch("/", requireAuth, ah(async (req, res) => {
  const { currency } = req.body ?? {};
  if (!ALLOWED_CURRENCIES.includes(currency)) {
    return res.status(400).json({ error: `currency must be one of ${ALLOWED_CURRENCIES.join(", ")}` });
  }
  const { rows } = await pool.query(
    `UPDATE users SET currency = $1 WHERE id = $2
     RETURNING id, email, full_name AS "fullName", currency, created_at AS "createdAt"`,
    [currency, req.userId]
  );
  res.json(rows[0]);
}));

export default router;
