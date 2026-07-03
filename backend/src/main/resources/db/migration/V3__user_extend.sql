-- user-register change: extend the users table for self-service registration.
-- Adds an optional display nickname, an optional (unique) email, and an account
-- status flag. Existing rows (e.g. the V2 demo user) keep NULL nickname/email and
-- default to ACTIVE.
-- ANSI-standard SQL so it runs on both MySQL (prod) and H2 (test/CI), matching V1/V2.
-- Separate single-column ALTERs are used because they are portable across both engines.

ALTER TABLE users ADD COLUMN nickname VARCHAR(64);
ALTER TABLE users ADD COLUMN email    VARCHAR(128);
ALTER TABLE users ADD COLUMN status   VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';

-- Email is optional but must be unique when present. Both MySQL and H2 permit
-- multiple NULLs under a UNIQUE constraint, so pre-existing NULL-email rows are fine.
ALTER TABLE users ADD CONSTRAINT uq_users_email UNIQUE (email);
