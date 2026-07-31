// Express 4 doesn't forward rejected promises from async route handlers to error middleware.
export const ah = (fn) => (req, res, next) => fn(req, res, next).catch(next);
