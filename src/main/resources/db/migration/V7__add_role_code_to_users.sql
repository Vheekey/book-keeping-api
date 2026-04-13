ALTER TABLE users
    ADD COLUMN IF NOT EXISTS role_code VARCHAR(50);

UPDATE users
SET role_code = UPPER(roles.name)
FROM roles
WHERE users.role_id = roles.id
  AND (users.role_code IS NULL OR users.role_code = '');

UPDATE users
SET role_code = 'USER'
WHERE role_code IS NULL OR role_code = '';

ALTER TABLE users
    ALTER COLUMN role_code SET NOT NULL,
    ALTER COLUMN role_code SET DEFAULT 'USER';

CREATE INDEX IF NOT EXISTS idx_users_role_code ON users (role_code);
