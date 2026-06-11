INSERT INTO user_model (id, email, name, password, role, created_at, updated_at) 
VALUES (
    nextval('user_model_seq'),
    'root@passaaqui.com', 
    'Administrador Root', 
    '$2b$10$e58y/bH8KJsHFsJ7RGA2L.LyUBOXc5DrqBjBzQ1KHuOx1nTkTgcUi',
    'ADMIN', 
    CURRENT_TIMESTAMP, 
    CURRENT_TIMESTAMP
);

INSERT INTO admin_model (id, admin_type) 
VALUES (
    (SELECT id FROM user_model WHERE email = 'root@passaaqui.com'), 
    1
);