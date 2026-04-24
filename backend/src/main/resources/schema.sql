DO $$
BEGIN
    IF to_regclass('users') IS NOT NULL THEN
        ALTER TABLE users ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'ACTIVE' NOT NULL;
        UPDATE users SET status = 'ACTIVE' WHERE status IS NULL;
        ALTER TABLE users ALTER COLUMN status SET DEFAULT 'ACTIVE';
        ALTER TABLE users ALTER COLUMN status SET NOT NULL;
    END IF;
END
$$@@

