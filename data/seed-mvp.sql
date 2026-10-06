-- ==============================================================================
-- Passa Aqui - Script de Inicialização de Dados para MVP (Perfil Turista)
-- ==============================================================================

-- 1. Cidades (CityModel)
INSERT INTO city_model (id, name, description, state, state_name, region, region_code, ibge_code, min_latitude, max_latitude, min_longitude, max_longitude, image, created_at, updated_at)
VALUES 
(1, 'Recife', 'Capital pernambucana, conhecida por suas pontes, cultura, praias e patrimônio histórico singular.', 'PE', 'Pernambuco', 'Nordeste', 2, '2611606', -8.15, -7.95, -35.00, -34.85, 'https://images.unsplash.com/photo-1599839575945-a9e5af0c3fa5', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Olinda', 'Cidade histórica Patrimônio Cultural Mundial da UNESCO, célebre pelas ladeiras, casario colonial e carnaval.', 'PE', 'Pernambuco', 'Nordeste', 2, '2609600', -8.03, -7.97, -34.88, -34.82, 'https://images.unsplash.com/photo-1596701062351-8c2c14d1fdd0', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO UPDATE SET 
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    state = EXCLUDED.state,
    state_name = EXCLUDED.state_name,
    region = EXCLUDED.region,
    region_code = EXCLUDED.region_code,
    ibge_code = EXCLUDED.ibge_code,
    min_latitude = EXCLUDED.min_latitude,
    max_latitude = EXCLUDED.max_latitude,
    min_longitude = EXCLUDED.min_longitude,
    max_longitude = EXCLUDED.max_longitude,
    image = EXCLUDED.image,
    updated_at = CURRENT_TIMESTAMP;

-- 2. Categorias (CategoryModel)
INSERT INTO category_model (id, name, description, category_weight)
VALUES 
(1, 'Alimentação', 'Restaurantes, lanchonetes, tapiocarias e gastronomia regional', 1.0),
(2, 'Mercado', 'Supermercados, hortifrútis e empórios', 0.8),
(3, 'Farmácia', 'Farmácias e drogarias', 1.2),
(4, 'Padaria', 'Padarias, confeitarias e cafeterias', 0.9),
(5, 'Pet Shop', 'Produtos e serviços para animais de estimação', 1.0),
(6, 'Academia', 'Academias e estúdios de ginástica', 1.1),
(7, 'Beleza', 'Salões de beleza, barbearias e estética', 1.0),
(8, 'Oficina', 'Oficinas mecânicas e autopeças', 0.7),
(9, 'Artesanato', 'Souvenirs, peças em cerâmica, couro e rendas tradicionais', 1.0),
(10, 'Cultura e Lazer', 'Monumentos, museus, teatros e atrativos turísticos', 1.2)
ON CONFLICT (id) DO UPDATE SET 
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    category_weight = EXCLUDED.category_weight;

-- 3. Usuários e Perfis (UserModel, AdminModel, TouristModel, ShopkeeperModel)
-- Senha padrão para todos os usuários criados: Password@123
-- Hash BCrypt: $2a$10$M4XQr5O3YOzE8qJkTMPvFOJu59KxKT3D5H9vvzdQsMlHXQQNgJ8si

-- 3.1 Administrador Root (ID 51)
INSERT INTO user_model (id, email, name, password, role, theme, image, created_at, updated_at)
VALUES (51, 'root@passaaqui.com', 'Administrador Root', '$2a$10$M4XQr5O3YOzE8qJkTMPvFOJu59KxKT3D5H9vvzdQsMlHXQQNgJ8si', 'ADMIN', 'LIGHT', 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO UPDATE SET email = EXCLUDED.email, name = EXCLUDED.name, password = EXCLUDED.password, role = EXCLUDED.role, image = EXCLUDED.image;

INSERT INTO admin_model (id, admin_type)
VALUES (51, 1)
ON CONFLICT (id) DO UPDATE SET admin_type = EXCLUDED.admin_type;

-- 3.2 Lojistas (Shopkeepers para fornecer produtos aos turistas)
-- Lojista 1: Armazém do Artesanato (ID 54)
INSERT INTO user_model (id, email, name, password, role, theme, image, created_at, updated_at)
VALUES (54, 'shopkeeper.test@example.com', 'Lojista Artesanatos Recife', '$2a$10$M4XQr5O3YOzE8qJkTMPvFOJu59KxKT3D5H9vvzdQsMlHXQQNgJ8si', 'SHOPKEEPER', 'DARK', 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO UPDATE SET email = EXCLUDED.email, name = EXCLUDED.name, password = EXCLUDED.password, role = EXCLUDED.role, image = EXCLUDED.image;

INSERT INTO shopkeeper_model (id, company_name, description, document_id, category_id)
VALUES (54, 'Armazém do Artesanato', 'Loja de artesanato e doces típicos no Recife Antigo', '11222333000181', 9)
ON CONFLICT (id) DO UPDATE SET company_name = EXCLUDED.company_name, description = EXCLUDED.description, document_id = EXCLUDED.document_id, category_id = EXCLUDED.category_id;

-- Lojista 2: Sabores de Olinda (ID 55)
INSERT INTO user_model (id, email, name, password, role, theme, image, created_at, updated_at)
VALUES (55, 'lojista@passaaqui.com', 'Lojista Olinda Sabores', '$2a$10$M4XQr5O3YOzE8qJkTMPvFOJu59KxKT3D5H9vvzdQsMlHXQQNgJ8si', 'SHOPKEEPER', 'LIGHT', 'https://images.unsplash.com/photo-1494790108377-be9c29b29330', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO UPDATE SET email = EXCLUDED.email, name = EXCLUDED.name, password = EXCLUDED.password, role = EXCLUDED.role, image = EXCLUDED.image;

INSERT INTO shopkeeper_model (id, company_name, description, document_id, category_id)
VALUES (55, 'Sabores do Alto da Sé', 'Tapiocaria e confeitaria tradicional de Olinda', '12345678000199', 1)
ON CONFLICT (id) DO UPDATE SET company_name = EXCLUDED.company_name, description = EXCLUDED.description, document_id = EXCLUDED.document_id, category_id = EXCLUDED.category_id;

-- 3.3 Turistas (Perfil de Teste com XP e Nível para o MVP)
-- Turista 1 (ID 52): tourist.test@example.com
INSERT INTO user_model (id, email, name, password, role, theme, image, created_at, updated_at)
VALUES (52, 'tourist.test@example.com', 'Turista Teste', '$2a$10$M4XQr5O3YOzE8qJkTMPvFOJu59KxKT3D5H9vvzdQsMlHXQQNgJ8si', 'TOURIST', 'LIGHT', 'https://images.unsplash.com/photo-1534528741775-53994a69daeb', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO UPDATE SET email = EXCLUDED.email, name = EXCLUDED.name, password = EXCLUDED.password, role = EXCLUDED.role, image = EXCLUDED.image;

INSERT INTO tourist_model (id, currentxp, level, device_id, document_id)
VALUES (52, 1500, 2, 'dev-tourist-001', '52998224725')
ON CONFLICT (id) DO UPDATE SET currentxp = 1500, level = 2, document_id = '52998224725';

-- Turista 2 (ID 53): turista@passaaqui.com
INSERT INTO user_model (id, email, name, password, role, theme, image, created_at, updated_at)
VALUES (53, 'turista@passaaqui.com', 'Turista Passa Aqui', '$2a$10$M4XQr5O3YOzE8qJkTMPvFOJu59KxKT3D5H9vvzdQsMlHXQQNgJ8si', 'TOURIST', 'LIGHT', 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO UPDATE SET email = EXCLUDED.email, name = EXCLUDED.name, password = EXCLUDED.password, role = EXCLUDED.role, image = EXCLUDED.image;

INSERT INTO tourist_model (id, currentxp, level, device_id, document_id)
VALUES (53, 1250, 2, 'dev-tourist-002', '52998224725')
ON CONFLICT (id) DO UPDATE SET currentxp = 1250, level = 2, document_id = '52998224725';

-- 4. Pontos de Interesse (PoiModel)
-- 4.1 Lojas (poi_type = 0)
INSERT INTO poi_model (id, name, description, poi_type, latitude, longitude, xp_reward, average_rating, ratings_count, city_id, shopkeeper_id, image, created_at, updated_at)
VALUES 
(1, 'Armazém do Artesanato', 'Loja com ampla variedade de artesanatos regionais, sombrinhas de frevo e souvenirs.', 0, -8.0633, -34.8720, NULL, 4.8, 18, 1, 54, 'https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Sabores do Alto da Sé', 'Tapiocas tradicionais doces e salgadas, com a mais bela vista do pôr do sol de Olinda.', 0, -8.0022, -34.8492, NULL, 4.9, 25, 2, 55, 'https://images.unsplash.com/photo-1555396273-367ea4eb4db5', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO UPDATE SET 
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    poi_type = EXCLUDED.poi_type,
    latitude = EXCLUDED.latitude,
    longitude = EXCLUDED.longitude,
    city_id = EXCLUDED.city_id,
    shopkeeper_id = EXCLUDED.shopkeeper_id,
    average_rating = EXCLUDED.average_rating,
    ratings_count = EXCLUDED.ratings_count,
    image = EXCLUDED.image,
    updated_at = CURRENT_TIMESTAMP;

-- 4.2 Pontos Turísticos (poi_type = 1, com recompensa de XP para check-in)
INSERT INTO poi_model (id, name, description, poi_type, latitude, longitude, xp_reward, average_rating, ratings_count, city_id, shopkeeper_id, image, created_at, updated_at)
VALUES 
(51, 'Praça do Marco Zero', 'Marco inaugural da cidade de Recife, às margens do estuário do Porto com vista para as esculturas de Francisco Brennand.', 1, -8.0631, -34.8711, 150, 4.9, 42, 1, NULL, 'https://images.unsplash.com/photo-1599839575945-a9e5af0c3fa5', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(101, 'Parque das Graças', 'Parque linear às margens do Rio Capibaribe com pistas de cooper, mirantes contemplativos e muita área verde.', 1, -8.0450, -34.9050, 100, 4.8, 15, 1, NULL, 'https://images.unsplash.com/photo-1519331379826-f10be5486c6f', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(102, 'Praia de Boa Viagem', 'A orla urbana mais famosa da capital pernambucana, com arrecifes naturais, águas mornas e quiosques com água de coco.', 1, -8.1278, -34.8998, 120, 4.7, 36, 1, NULL, 'https://images.unsplash.com/photo-1507525428034-b723cf961d3e', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(103, 'Instituto Ricardo Brennand', 'Complexo cultural com arquitetura de castelo medieval que abriga o maior acervo de armaduras e pinturas do Brasil holandês.', 1, -8.0583, -34.9669, 200, 5.0, 58, 1, NULL, 'https://images.unsplash.com/photo-1582555172866-f73bb12a2ab3', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(104, 'Alto da Sé', 'Mirante histórico no topo de Olinda com vista deslumbrante para a orla de Recife, feira de artesanato e a famosa Catedral da Sé.', 1, -8.0019, -34.8488, 180, 4.9, 64, 2, NULL, 'https://images.unsplash.com/photo-1596701062351-8c2c14d1fdd0', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO UPDATE SET 
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    poi_type = EXCLUDED.poi_type,
    latitude = EXCLUDED.latitude,
    longitude = EXCLUDED.longitude,
    xp_reward = EXCLUDED.xp_reward,
    average_rating = EXCLUDED.average_rating,
    ratings_count = EXCLUDED.ratings_count,
    city_id = EXCLUDED.city_id,
    shopkeeper_id = EXCLUDED.shopkeeper_id,
    image = EXCLUDED.image,
    updated_at = CURRENT_TIMESTAMP;

-- 5. Produtos Disponíveis para Resgate e Compra (ProductModel)
INSERT INTO product_model (id, name, description, price, max_xp, stock, active, highlight, version, category_id, shopkeeper_id, poi_id, average_rating, ratings_count, created_at, updated_at)
VALUES 
(251, 'Bolo de Rolo Tradicional (500g)', 'O autêntico bolo de rolo pernambucano, massa finíssima com recheio cremoso de goiabada cascão artesanal.', 28.00, 500, 50, true, true, 0, 1, 54, 1, 4.9, 14, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(252, 'Sombrinha de Frevo Colorida', 'Sombrinha oficial de frevo em tecido impermeável, símbolo alegre da cultura e folclore pernambucano.', 15.00, 300, 35, true, true, 0, 9, 54, 1, 4.8, 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(253, 'Tapioca Especial Carne de Sol com Coalho', 'Deliciosa tapioca crocante e quentinha, recheada com carne de sol desfiada e queijo coalho tostado.', 22.00, 400, 40, true, true, 0, 1, 55, 2, 5.0, 22, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(254, 'Ímã de Geladeira Boneco Gigante', 'Lembrança artesanal em resina moldada e pintada à mão em homenagem aos bonecos do carnaval de Olinda.', 12.00, 200, 80, true, false, 0, 9, 54, 1, 4.7, 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(255, 'Café Regional Especial com Broa', 'Café 100% arábica cultivado no brejo de altitude de Pernambuco, servido acompanhado de broa de milho quentinha.', 10.00, 150, 60, true, false, 0, 1, 55, 2, 4.8, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(256, 'Camiseta Recife & Olinda 100% Algodão', 'Camiseta estilizada em algodão nobre com arte inspirada nas pontes históricas e no cordel nordestino.', 45.00, 800, 25, true, false, 0, 9, 54, 1, 4.6, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO UPDATE SET 
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    price = EXCLUDED.price,
    max_xp = EXCLUDED.max_xp,
    stock = EXCLUDED.stock,
    active = EXCLUDED.active,
    highlight = EXCLUDED.highlight,
    category_id = EXCLUDED.category_id,
    shopkeeper_id = EXCLUDED.shopkeeper_id,
    poi_id = EXCLUDED.poi_id,
    average_rating = EXCLUDED.average_rating,
    ratings_count = EXCLUDED.ratings_count,
    updated_at = CURRENT_TIMESTAMP;

-- 5.1 Imagens dos Produtos (product_images)
DELETE FROM product_images WHERE product_id IN (251, 252, 253, 254, 255, 256);
INSERT INTO product_images (product_id, image_name)
VALUES 
(251, 'https://images.unsplash.com/photo-1578985545062-69928b1d9587'),
(252, 'https://images.unsplash.com/photo-1514565131-fce0801e5785'),
(253, 'https://images.unsplash.com/photo-1628294895950-9805252327bc'),
(254, 'https://images.unsplash.com/photo-1513519245088-0e12902e5a38'),
(255, 'https://images.unsplash.com/photo-1509042239860-f550ce710b93'),
(256, 'https://images.unsplash.com/photo-1521572267360-ee0c2909d518');

-- 6. Conquistas do Sistema (tb_achievements)
INSERT INTO tb_achievements (id, name, description, image_name, xp_reward, category_id, poi_id, location_name, created_at, updated_at)
VALUES 
(1, 'Primeiro Passo', 'Realizou seu primeiro check-in em um ponto turístico oficial do Passa Aqui.', 'https://images.unsplash.com/photo-1579783900882-c0d3dad7b119', 100, 10, 51, 'Recife - Marco Zero', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Explorador do Recife', 'Descobriu e explorou os monumentos históricos do Recife Antigo.', 'https://images.unsplash.com/photo-1533105079780-92b9be482077', 250, 10, 51, 'Recife Antigo', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'Mestre das Ladeiras', 'Subiu o Alto da Sé e contemplou a inesquecível paisagem de Olinda.', 'https://images.unsplash.com/photo-1596701062351-8c2c14d1fdd0', 200, 10, 104, 'Olinda - Alto da Sé', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'Paladar Pernambucano', 'Experimentou o legítimo bolo de rolo ou tapioca pernambucana.', 'https://images.unsplash.com/photo-1555396273-367ea4eb4db5', 150, 1, 1, 'Recife & Olinda', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'Colecionador de Aventuras', 'Alcançou mais de 1000 pontos de XP em sua jornada turística.', 'https://images.unsplash.com/photo-1569336415962-a4bd9f69cd83', 300, 10, NULL, 'Pernambuco', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO UPDATE SET 
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    image_name = EXCLUDED.image_name,
    xp_reward = EXCLUDED.xp_reward,
    category_id = EXCLUDED.category_id,
    poi_id = EXCLUDED.poi_id,
    location_name = EXCLUDED.location_name,
    updated_at = CURRENT_TIMESTAMP;

-- 7. Conquistas Desbloqueadas pelo Turista (tb_user_achievements)
-- Desbloqueia 'Primeiro Passo' para os turistas 52 e 53
INSERT INTO tb_user_achievements (id, user_id, achievement_id, poi_id, location_name, unlocked_at)
VALUES 
(1, 52, 1, 51, 'Recife - Marco Zero', CURRENT_TIMESTAMP - interval '2 days'),
(2, 53, 1, 51, 'Recife - Marco Zero', CURRENT_TIMESTAMP - interval '2 days')
ON CONFLICT (user_id, achievement_id) DO NOTHING;

-- 8. Histórico de Visitas / Check-ins do Turista (poi_visit_model)
INSERT INTO poi_visit_model (id, poi_id, user_id, xp_earned, distance_km, visited_at)
VALUES 
(1, 51, 52, 150, 0.04, CURRENT_TIMESTAMP - interval '2 days'),
(2, 101, 52, 100, 0.02, CURRENT_TIMESTAMP - interval '1 day'),
(3, 51, 53, 150, 0.04, CURRENT_TIMESTAMP - interval '2 days'),
(4, 101, 53, 100, 0.02, CURRENT_TIMESTAMP - interval '1 day')
ON CONFLICT (id) DO NOTHING;

-- 9. Avaliações de Pontos de Interesse pelos Turistas (poi_rating_model)
INSERT INTO poi_rating_model (id, poi_id, user_id, rating, created_at)
VALUES 
(1, 51, 52, 5, CURRENT_TIMESTAMP - interval '2 days'),
(2, 101, 52, 5, CURRENT_TIMESTAMP - interval '1 day'),
(3, 51, 53, 5, CURRENT_TIMESTAMP - interval '2 days')
ON CONFLICT (poi_id, user_id) DO UPDATE SET rating = EXCLUDED.rating;

-- 10. Pedidos de Demonstração para Teste do Turista (tb_orders)
-- Pedido 1: Concluído e Resgatado (Bolo de Rolo) - Permite testar histórico e avaliação de produto comprado
INSERT INTO tb_orders (id, tourist_id, shopkeeper_id, product_id, quantity, total_amount, cash_discount, status, code, pickup_code, redemption_code, transaction_id, redeemed_at, created_at, updated_at)
VALUES 
('a1b2c3d4-e5f6-4a1b-8c2d-3e4f5a6b7c8d', 52, 54, 251, 1, 28.00, 5.00, 'COMPLETED', 'ORD-9841', 'BR5421', '948271', 'tx-mvp-completed-001', CURRENT_TIMESTAMP - interval '1 day', CURRENT_TIMESTAMP - interval '2 days', CURRENT_TIMESTAMP - interval '1 day'),
('c3d4e5f6-a1b2-4c3d-ae4f-5a6b7c8d9e0f', 53, 54, 251, 1, 28.00, 5.00, 'COMPLETED', 'ORD-7712', 'BR7712', '839201', 'tx-mvp-completed-003', CURRENT_TIMESTAMP - interval '1 day', CURRENT_TIMESTAMP - interval '2 days', CURRENT_TIMESTAMP - interval '1 day')
ON CONFLICT (id) DO UPDATE SET 
    status = EXCLUDED.status,
    redeemed_at = EXCLUDED.redeemed_at;

-- Pedido 2: Pronto para Retirada (Sombrinha de Frevo) - Permite testar a tela de pedidos em andamento
INSERT INTO tb_orders (id, tourist_id, shopkeeper_id, product_id, quantity, total_amount, cash_discount, status, code, pickup_code, redemption_code, transaction_id, created_at, updated_at)
VALUES 
('b2c3d4e5-f6a1-4b2c-9d3e-4f5a6b7c8d9e', 52, 54, 252, 1, 15.00, 0.00, 'READY_FOR_PICKUP', 'ORD-1024', 'SF1024', '562184', 'tx-mvp-ready-002', CURRENT_TIMESTAMP - interval '2 hours', CURRENT_TIMESTAMP - interval '1 hour'),
('d4e5f6a1-b2c3-4d4e-bf5a-6b7c8d9e0f1a', 53, 54, 252, 1, 15.00, 0.00, 'READY_FOR_PICKUP', 'ORD-1025', 'SF1025', '718293', 'tx-mvp-ready-004', CURRENT_TIMESTAMP - interval '2 hours', CURRENT_TIMESTAMP - interval '1 hour')
ON CONFLICT (id) DO UPDATE SET 
    status = EXCLUDED.status;

-- 11. Avaliação de Produto Comprado (product_rating_model)
INSERT INTO product_rating_model (id, product_id, user_id, rating, comment, order_code, created_at)
VALUES 
(2, 251, 52, 5, 'Bolo de rolo espetacular! Camadas finíssimas e goiabada de alta qualidade. Recomendo muito!', 'ORD-9841', CURRENT_TIMESTAMP - interval '1 day')
ON CONFLICT (product_id, user_id) DO UPDATE SET rating = EXCLUDED.rating, comment = EXCLUDED.comment, order_code = EXCLUDED.order_code;

INSERT INTO product_rating_model (id, product_id, user_id, rating, comment, order_code, created_at)
VALUES 
(3, 251, 53, 5, 'Uma das melhores lembranças gastronômicas de Recife. Atendimento excelente no resgate.', 'ORD-7712', CURRENT_TIMESTAMP - interval '1 day')
ON CONFLICT (product_id, user_id) DO UPDATE SET rating = EXCLUDED.rating, comment = EXCLUDED.comment, order_code = EXCLUDED.order_code;

-- 11.1 Imagens das Avaliações (product_rating_images)
DELETE FROM product_rating_images WHERE rating_id IN (2, 3);
INSERT INTO product_rating_images (rating_id, image_name)
VALUES 
(2, 'https://images.unsplash.com/photo-1578985545062-69928b1d9587');


-- 12. Atualização das Sequences para evitar colisão de IDs em futuras criações via API
SELECT setval('city_model_seq', GREATEST((SELECT MAX(id) FROM city_model), 100), true);
SELECT setval('category_model_seq', GREATEST((SELECT MAX(id) FROM category_model), 100), true);
SELECT setval('user_model_seq', GREATEST((SELECT MAX(id) FROM user_model), 200), true);
SELECT setval('poi_model_seq', GREATEST((SELECT MAX(id) FROM poi_model), 200), true);
SELECT setval('product_model_seq', GREATEST((SELECT MAX(id) FROM product_model), 500), true);
SELECT setval('tb_achievements_seq', GREATEST((SELECT MAX(id) FROM tb_achievements), 100), true);
SELECT setval('tb_user_achievements_seq', GREATEST((SELECT MAX(id) FROM tb_user_achievements), 100), true);
SELECT setval('poi_visit_model_seq', GREATEST((SELECT MAX(id) FROM poi_visit_model), 100), true);
SELECT setval('poi_rating_model_seq', GREATEST((SELECT MAX(id) FROM poi_rating_model), 100), true);
SELECT setval('product_rating_model_seq', GREATEST((SELECT MAX(id) FROM product_rating_model), 100), true);
