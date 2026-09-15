DO $$
DECLARE
    i INT;
    uid INT;
    pass TEXT;
BEGIN
    SELECT password INTO pass FROM user_model WHERE email = 'seller1@passaaqui.com';
    FOR i IN 9..500 LOOP
        IF NOT EXISTS (SELECT 1 FROM user_model WHERE email = 'seller' || i || '@passaaqui.com') THEN
            uid := nextval('user_model_seq');
            INSERT INTO user_model (id, email, password, name, role, created_at, updated_at)
            VALUES (uid, 'seller' || i || '@passaaqui.com', pass, 'Seller ' || i, 'SHOPKEEPER', NOW(), NOW());

            INSERT INTO shopkeeper_model (id, company_name, description, document_id, category_id)
            VALUES (uid, 'Loja ' || i, 'Descricao da loja ' || i, '12345678' || LPAD(i::text, 6, '0'), 1);
        END IF;
    END LOOP;
END $$;
