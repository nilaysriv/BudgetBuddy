import assert from "node:assert/strict";

process.env.JWT_SECRET = "test-secret";
const { issueToken, verifyToken, isValidEmail, isValidPassword } = await import("../src/tokens.js");
const { ah } = await import("../src/asyncHandler.js");

// token round-trip
const token = issueToken(42);
assert.equal(verifyToken(token), 42);

// tampered/garbage tokens are rejected
assert.throws(() => verifyToken(token + "x"));
assert.throws(() => verifyToken("not-a-token"));

// validation
assert.equal(isValidEmail("a@b.com"), true);
assert.equal(isValidEmail("not-an-email"), false);
assert.equal(isValidPassword("short"), false);
assert.equal(isValidPassword("longenough"), true);

// async route errors reach next(err) instead of hanging/crashing
let caught;
const handler = ah(async () => {
  throw new Error("boom");
});
await handler({}, {}, (err) => {
  caught = err;
});
assert.equal(caught.message, "boom");

console.log("all auth self-checks passed");
