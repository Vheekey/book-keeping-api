INSERT INTO users(email, name, role_id, role_code)
SELECT 'test@gmail.com', 'sadmin', id, 'SADMIN'
FROM roles
WHERE name = 'sadmin';

INSERT INTO user_auth_providers(user_id, provider, provider_subject, username, password)
SELECT id, 'LOCAL', NULL, 'test', '$2a$10$cOyCBnmxErTLS2tnInuCruj06QFfIRlIXOcY9LbFDqzJ1ACAzZR8S'
FROM users
WHERE email = 'test@gmail.com';
