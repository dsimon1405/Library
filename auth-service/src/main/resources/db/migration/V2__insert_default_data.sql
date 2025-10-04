INSERT INTO roles (name) VALUES ('ROLE_ADMIN'), ('ROLE_USER');

INSERT INTO users (name, password)
VALUES
('admin', '$2a$10$20xo4esaXwY.46XNCgZzGuJa3z9FMDxbegiFbHMzSV.lIH8RvgEti');

INSERT INTO users_roles (user_id, role_id)
SELECT u.user_id, r.role_id
FROM users u
JOIN roles r ON r.name = 'ROLE_ADMIN'
WHERE u.name = 'admin';

INSERT INTO users_roles (user_id, role_id)
SELECT u.user_id, r.role_id
FROM users u
JOIN roles r ON r.name = 'ROLE_USER'
WHERE u.name = 'admin';
