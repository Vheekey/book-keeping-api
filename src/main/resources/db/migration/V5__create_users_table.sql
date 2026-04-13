CREATE TABLE users
(
    id                BIGSERIAL PRIMARY KEY,
    email             VARCHAR(50)              NOT NULL,
    name              VARCHAR(255)             NOT NULL,
    role_id           BIGINT                   NOT NULL,
    role_code         VARCHAR(50)              NOT NULL DEFAULT 'USER',
    is_active         BOOLEAN                           DEFAULT TRUE,
    email_verified_at TIMESTAMP WITH TIME ZONE          DEFAULT NULL,
    created_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_users_role_id
        FOREIGN KEY (role_id)
            REFERENCES roles (id)
            ON DELETE RESTRICT
            ON UPDATE CASCADE
);

CREATE UNIQUE INDEX idx_users_email_unique ON users (email);
CREATE INDEX idx_users_email_role ON users (email, role_id);
CREATE INDEX idx_users_role_code ON users (role_code);
