import { Router } from "express";
import bcrypt from "bcrypt";
import { pool } from "../db.js";
import { issueToken, isValidEmail, isValidPassword, isValidFullName } from "../tokens.js";
import { ah } from "../asyncHandler.js";

const router = Router();

router.post("/register", ah(async (req, res) => {
  const { email, password, fullName } = req.body ?? {};
  if (!isValidEmail(email) || !isValidPassword(password) || !isValidFullName(fullName)) {
    return res.status(400).json({ error: "valid full name, email, and password (min 8 chars) required" });
  }

  const passwordHash = await bcrypt.hash(password, 12);
  try {
    const { rows } = await pool.query(
      "INSERT INTO users (email, password_hash, full_name) VALUES ($1, $2, $3) RETURNING id",
      [email.toLowerCase(), passwordHash, fullName.trim()]
    );
    res.status(201).json({ token: issueToken(rows[0].id) });
  } catch (err) {
    if (err.code === "23505") {
      return res.status(409).json({ error: "email already registered" });
    }
    throw err;
  }
}));

router.post("/login", ah(async (req, res) => {
  const { email, password } = req.body ?? {};
  if (!isValidEmail(email) || typeof password !== "string") {
    return res.status(400).json({ error: "email and password required" });
  }

  const { rows } = await pool.query(
    "SELECT id, password_hash FROM users WHERE email = $1",
    [email.toLowerCase()]
  );
  const user = rows[0];
  // constant-shape response whether or not the user exists, to avoid leaking which emails are registered
  const validHash = user?.password_hash ?? "$2b$12$invalidsaltinvalidsaltinvalidsaltinvalidsalt.......";
  const ok = await bcrypt.compare(password, validHash);

  if (!user || !user.password_hash || !ok) {
    return res.status(401).json({ error: "invalid credentials" });
  }
  res.json({ token: issueToken(user.id) });
}));

export default router;
