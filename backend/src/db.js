import pg from "pg";

// pg returns BIGINT (OID 20) as a string by default to avoid silent precision loss;
// our ids/category refs are client-generated Room longs, safely within JS's integer range.
pg.types.setTypeParser(20, (val) => parseInt(val, 10));

export const pool = new pg.Pool({
  connectionString: process.env.DATABASE_URL,
});
