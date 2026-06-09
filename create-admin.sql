INSERT INTO user_model (id, email, name, password, role, created_at, updated_at) 
VALUES (
    nextval('user_model_seq'),
    'root@passaaqui.com', 
    'Administrador Root', 
    '$2a$10$3qILDhEwH4eBh0.UkLv3geZBiYNfSF3dz6fRxY8w4P8uY1gL3Xg/m',
    'ADMIN', 
    CURRENT_TIMESTAMP, 
    CURRENT_TIMESTAMP
);

INSERT INTO admin_model (id, admin_type) 
VALUES (
    (SELECT id FROM user_model WHERE email = 'root@passaaqui.com'), 
    1
);