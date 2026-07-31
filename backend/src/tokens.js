import jwt from "jsonwebtoken";

const SECRET = process.env.JWT_SECRET;
if (!SECRET) {
  throw new Error("JWT_SECRET env var is required");
}

export function issueToken(userId) {
  return jwt.sign({ sub: userId }, SECRET, { expiresIn: "7d" });
}

export function verifyToken(token) {
  const payload = jwt.verify(token, SECRET); // throws on invalid/expired
  return payload.sub;
}

export function isValidEmail(email) {
  return typeof email === "string" && /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

export function isValidPassword(password) {
  return typeof password === "string" && password.length >= 8;
}

export function isValidFullName(fullName) {
  return typeof fullName === "string" && fullName.trim().length >= 1 && fullName.trim().length <= 100;
}
