INSERT INTO roles (name)
VALUES ('user'),
       ('sadmin'),
       ('admin'),
       ('finance')

ON CONFLICT (name) DO UPDATE SET updated_at = CURRENT_TIMESTAMP;