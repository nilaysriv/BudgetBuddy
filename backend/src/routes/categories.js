import { Router } from "express";
import { pool } from "../db.js";
import { ah } from "../asyncHandler.js";

const router = Router();

const SELECT_COLUMNS = `id, name, type, icon_key AS "iconKey", color_hex AS "colorHex", is_default AS "isDefault", parent_category_id AS "parentCategoryId"`;

router.get("/", ah(async (req, res) => {
  const { rows } = await pool.query(
    `SELECT ${SELECT_COLUMNS} FROM categories WHERE user_id = $1`,
    [req.userId]
  );
  res.json(rows);
}));

router.post("/", ah(async (req, res) => {
  const { id, name, type, iconKey, colorHex, isDefault, parentCategoryId } = req.body ?? {};
  if (id == null || typeof name !== "string" || typeof type !== "string" || typeof iconKey !== "string" || typeof colorHex !== "string") {
    return res.status(400).json({ error: "id, name, type, iconKey, colorHex required" });
  }
  const { rows } = await pool.query(
    `INSERT INTO categories (user_id, id, name, type, icon_key, color_hex, is_default, parent_category_id)
     VALUES ($1, $2, $3, $4, $5, $6, $7, $8)
     ON CONFLICT (user_id, id) DO UPDATE SET
       name = EXCLUDED.name, type = EXCLUDED.type, icon_key = EXCLUDED.icon_key,
       color_hex = EXCLUDED.color_hex, is_default = EXCLUDED.is_default, parent_category_id = EXCLUDED.parent_category_id
     RETURNING ${SELECT_COLUMNS}`,
    [req.userId, id, name, type, iconKey, colorHex, Boolean(isDefault), parentCategoryId ?? null]
  );
  res.status(201).json(rows[0]);
}));

router.put("/:id", ah(async (req, res) => {
  const { name, type, iconKey, colorHex, isDefault, parentCategoryId } = req.body ?? {};
  const { rows } = await pool.query(
    `UPDATE categories SET name = $1, type = $2, icon_key = $3, color_hex = $4, is_default = $5, parent_category_id = $6
     WHERE user_id = $7 AND id = $8
     RETURNING ${SELECT_COLUMNS}`,
    [name, type, iconKey, colorHex, Boolean(isDefault), parentCategoryId ?? null, req.userId, req.params.id]
  );
  if (!rows[0]) return res.status(404).json({ error: "category not found" });
  res.json(rows[0]);
}));

router.delete("/:id", ah(async (req, res) => {
  await pool.query("UPDATE categories SET parent_category_id = NULL WHERE user_id = $1 AND parent_category_id = $2", [req.userId, req.params.id]);
  await pool.query("DELETE FROM categories WHERE user_id = $1 AND id = $2", [req.userId, req.params.id]);
  res.status(204).end();
}));

export default router;
