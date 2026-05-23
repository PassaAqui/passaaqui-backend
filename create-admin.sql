INSERT INTO user_model (id, email, name, password, role, created_at, updated_at) 
VALUES (
    nextval('user_model_seq'),
    'root@passaaqui.com', 
    'Administrador Root', 
    '$2a$10$eiaT6JJ8AMtlWSVDVDTig.PRhtM8hI4h0y138Z733Quw/5sLn3ltS',
    'ADMIN', 
    CURRENT_TIMESTAMP, 
    CURRENT_TIMESTAMP
);

INSERT INTO admin_model (id, admin_type) 
VALUES (
    (SELECT id FROM user_model WHERE email = 'root@passaaqui.com'), 
    1
);