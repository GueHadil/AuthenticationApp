-- Roles
CREATE TABLE IF NOT EXISTS roles (
                                     id SERIAL PRIMARY KEY,
                                     name VARCHAR(50) UNIQUE NOT NULL
    );

INSERT INTO roles(name) VALUES
                            ('ADMIN'), ('GESTIONNAIRE'), ('TRAVAILLEUR'), ('ENSEIGNANT'), ('ETUDIANT')
    ON CONFLICT DO NOTHING;

-- Users
CREATE TABLE IF NOT EXISTS users (
                                     id SERIAL PRIMARY KEY,
                                     full_name VARCHAR(150) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password_hash VARCHAR(200) NOT NULL,
    role_id INT NOT NULL REFERENCES roles(id),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
    );

CREATE INDEX IF NOT EXISTS idx_users_status ON users(status);


CREATE TABLE IF NOT EXISTS password_reset_tokens (
                                                     id SERIAL PRIMARY KEY,
                                                     user_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(100) UNIQUE NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
    );
CREATE EXTENSION IF NOT EXISTS pgcrypto;
WITH admin_role AS (
    SELECT id FROM roles WHERE name = 'ADMIN' LIMIT 1
    )
INSERT INTO users (full_name, email, password_hash, role_id, status, enabled)
SELECT
    'admin',
    'admin@gmail.com',
    crypt('admin123', gen_salt('bf')),  -- hash bcrypt
    admin_role.id,
    'ACCEPTED',
    TRUE
FROM admin_role;
SELECT * from u
    FROM users
WHERE email = 'admin@example.com'
  AND password_hash = crypt('motdepasse_simple', password_hash);

ALTER TABLE password_reset_tokens
    ADD COLUMN used_at TIMESTAMP;
