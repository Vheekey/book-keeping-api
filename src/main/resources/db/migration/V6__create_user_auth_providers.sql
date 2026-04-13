CREATE TYPE auth_provider as ENUM('LOCAL', 'GOOGLE');
CREATE TABLE user_auth_providers (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    provider auth_provider NOT NULL DEFAULT 'LOCAL',
    provider_subject VARCHAR,
    username varchar(200) DEFAULT NULL,
    password VARCHAR(255) DEFAULT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_user_id
        FOREIGN KEY(user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE UNIQUE INDEX idx_auth_provider_subject_unique on user_auth_providers(provider, provider_subject);
CREATE UNIQUE INDEX idx_auth_provider_username_unique on user_auth_providers(provider, username);
CREATE UNIQUE INDEX idx_auth_user_provider_unique on user_auth_providers(user_id, provider);