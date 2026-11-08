-- ==============================================================================
-- Passa Aqui - Mock Completo de Dados para Apresentação e Simulação
-- Ponto central de simulação: Lat: -8.0675, Long: -34.9167 (Recife / Ilha do Retiro - Madalena)
-- Senha padrão para todos os usuários: Password@123
-- Hash BCrypt: $2a$10$M4XQr5O3YOzE8qJkTMPvFOJu59KxKT3D5H9vvzdQsMlHXQQNgJ8si
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

-- 3. Usuários e Perfis
-- 3.1 Administradores
INSERT INTO user_model (id, email, name, password, role, theme, image, created_at, updated_at)
VALUES 
(51, 'root@passaaqui.com', 'Administrador Root', '$2a$10$M4XQr5O3YOzE8qJkTMPvFOJu59KxKT3D5H9vvzdQsMlHXQQNgJ8si', 'ADMIN', 'LIGHT', 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(50, 'admin@passaaqui.com', 'Administrador Sistema', '$2a$10$M4XQr5O3YOzE8qJkTMPvFOJu59KxKT3D5H9vvzdQsMlHXQQNgJ8si', 'ADMIN', 'LIGHT', 'https://images.unsplash.com/photo-1534528741775-53994a69daeb', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO UPDATE SET email = EXCLUDED.email, name = EXCLUDED.name, password = EXCLUDED.password, role = EXCLUDED.role, image = EXCLUDED.image;

INSERT INTO admin_model (id, admin_type)
VALUES 
(51, 1),
(50, 0)
ON CONFLICT (id) DO UPDATE SET admin_type = EXCLUDED.admin_type;

-- 3.2 Lojistas
INSERT INTO user_model (id, email, name, password, role, theme, image, created_at, updated_at)
VALUES 
(54, 'shopkeeper.test@example.com', 'Lojista Artesanatos Recife', '$2a$10$M4XQr5O3YOzE8qJkTMPvFOJu59KxKT3D5H9vvzdQsMlHXQQNgJ8si', 'SHOPKEEPER', 'DARK', 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(55, 'lojista@passaaqui.com', 'Lojista Olinda Sabores', '$2a$10$M4XQr5O3YOzE8qJkTMPvFOJu59KxKT3D5H9vvzdQsMlHXQQNgJ8si', 'SHOPKEEPER', 'LIGHT', 'https://images.unsplash.com/photo-1494790108377-be9c29b29330', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(56, 'cafe.capibaribe@passaaqui.com', 'Café & Bistro Capibaribe', '$2a$10$M4XQr5O3YOzE8qJkTMPvFOJu59KxKT3D5H9vvzdQsMlHXQQNgJ8si', 'SHOPKEEPER', 'LIGHT', 'https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(57, 'emporio.madalena@passaaqui.com', 'Empório Gourmet Madalena', '$2a$10$M4XQr5O3YOzE8qJkTMPvFOJu59KxKT3D5H9vvzdQsMlHXQQNgJ8si', 'SHOPKEEPER', 'LIGHT', 'https://images.unsplash.com/photo-1542838132-92c53300491e', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(58, 'padaria.retiro@passaaqui.com', 'Padaria & Delícias da Ilha', '$2a$10$M4XQr5O3YOzE8qJkTMPvFOJu59KxKT3D5H9vvzdQsMlHXQQNgJ8si', 'SHOPKEEPER', 'LIGHT', 'https://images.unsplash.com/photo-1509440159596-0249088772ff', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(59, 'farmacia.vida@passaaqui.com', 'Drogaria & Bem Estar Retiro', '$2a$10$M4XQr5O3YOzE8qJkTMPvFOJu59KxKT3D5H9vvzdQsMlHXQQNgJ8si', 'SHOPKEEPER', 'LIGHT', 'https://images.unsplash.com/photo-1586015555751-63bb77f4322a', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO UPDATE SET email = EXCLUDED.email, name = EXCLUDED.name, password = EXCLUDED.password, role = EXCLUDED.role, image = EXCLUDED.image;

INSERT INTO shopkeeper_model (id, company_name, description, document_id, category_id)
VALUES 
(54, 'Armazém do Artesanato', 'Loja de artesanato e doces típicos no Recife Antigo', '11222333000181', 9),
(55, 'Sabores do Alto da Sé', 'Tapiocaria e confeitaria tradicional de Olinda', '12345678000199', 1),
(56, 'Café do Capibaribe', 'Cafeteria artesanal com cafés especiais e bolos regionais na beira do rio', '45812903000112', 4),
(57, 'Empório Madalena', 'Queijos artesanais, cachaças nobres e produtos regionais selecionados', '67192834000188', 2),
(58, 'Padaria da Ilha do Retiro', 'Pães quentinhos artesanais, salgados finos, tortas e sucos da fruta', '89102938000144', 4),
(59, 'Drogaria & Bem Estar', 'Medicamentos, vitaminas, protetores solares e cuidados de saúde', '34102938000155', 3)
ON CONFLICT (id) DO UPDATE SET company_name = EXCLUDED.company_name, description = EXCLUDED.description, document_id = EXCLUDED.document_id, category_id = EXCLUDED.category_id;

-- 3.3 Turistas
INSERT INTO user_model (id, email, name, password, role, theme, image, created_at, updated_at)
VALUES 
(52, 'tourist.test@example.com', 'Turista Teste', '$2a$10$M4XQr5O3YOzE8qJkTMPvFOJu59KxKT3D5H9vvzdQsMlHXQQNgJ8si', 'TOURIST', 'LIGHT', 'https://images.unsplash.com/photo-1534528741775-53994a69daeb', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(53, 'turista@passaaqui.com', 'Turista Passa Aqui', '$2a$10$M4XQr5O3YOzE8qJkTMPvFOJu59KxKT3D5H9vvzdQsMlHXQQNgJ8si', 'TOURIST', 'LIGHT', 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 'william@gmail.com', 'William Turista', '$2a$10$M4XQr5O3YOzE8qJkTMPvFOJu59KxKT3D5H9vvzdQsMlHXQQNgJ8si', 'TOURIST', 'LIGHT', 'https://images.unsplash.com/photo-1539571696357-5a69c17a67c6', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(201, 'i@i.com', 'Italo Viajante', '$2a$10$M4XQr5O3YOzE8qJkTMPvFOJu59KxKT3D5H9vvzdQsMlHXQQNgJ8si', 'TOURIST', 'LIGHT', 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO UPDATE SET email = EXCLUDED.email, name = EXCLUDED.name, password = EXCLUDED.password, role = EXCLUDED.role, image = EXCLUDED.image;

INSERT INTO tourist_model (id, currentxp, level, device_id, document_id)
VALUES 
(52, 2850, 3, 'dev-tourist-001', '52998224725'),
(53, 3500, 4, 'dev-tourist-002', '52998224725'),
(1, 2400, 3, 'dev-tourist-003', '52998224725'),
(201, 1900, 2, 'dev-tourist-004', '52998224725')
ON CONFLICT (id) DO UPDATE SET currentxp = EXCLUDED.currentxp, level = EXCLUDED.level, document_id = EXCLUDED.document_id;

-- 4. Pontos de Interesse (PoiModel)
-- Localização da Simulação: Lat -8.0675, Long -34.9167
-- 4.1 Lojas Próximas à Simulação (poi_type = 0)
INSERT INTO poi_model (id, name, description, poi_type, latitude, longitude, xp_reward, average_rating, ratings_count, city_id, shopkeeper_id, image, created_at, updated_at)
VALUES 
(1, 'Armazém do Artesanato', 'Loja com ampla variedade de artesanatos regionais, sombrinhas de frevo e souvenirs.', 0, -8.0633, -34.8720, NULL, 4.8, 18, 1, 54, 'https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Sabores do Alto da Sé', 'Tapiocas tradicionais doces e salgadas, com a mais bela vista do pôr do sol de Olinda.', 0, -8.0022, -34.8492, NULL, 4.9, 25, 2, 55, 'https://images.unsplash.com/photo-1555396273-367ea4eb4db5', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'Café do Capibaribe - Ilha do Retiro', 'Cafeteria charmosa e aconchegante com cafés especiais e doces artesanais pernambucanos.', 0, -8.0671, -34.9161, NULL, 4.9, 32, 1, 56, 'https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'Empório Madalena Regional', 'Queijos artesanais da Serra das Russas, manteiga de garrafa, doces e cachaças premiadas.', 0, -8.0682, -34.9174, NULL, 4.8, 27, 1, 57, 'https://images.unsplash.com/photo-1542838132-92c53300491e', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'Padaria da Ilha do Retiro', 'Padaria com pães artesanais de fermentação natural, café da manhã nordestino e lanches rápidos.', 0, -8.0664, -34.9158, NULL, 4.7, 41, 1, 58, 'https://images.unsplash.com/photo-1509440159596-0249088772ff', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 'Drogaria & Bem Estar Retiro', 'Farmácia completa com produtos de conveniência, dermocosméticos e cuidados pessoais.', 0, -8.0688, -34.9152, NULL, 4.8, 19, 1, 59, 'https://images.unsplash.com/photo-1586015555751-63bb77f4322a', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
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

-- 4.2 Pontos Turísticos Próximos e Regionais (poi_type = 1, com XP para check-in)
INSERT INTO poi_model (id, name, description, poi_type, latitude, longitude, xp_reward, average_rating, ratings_count, city_id, shopkeeper_id, image, created_at, updated_at)
VALUES 
(51, 'Praça do Marco Zero', 'Marco inaugural da cidade de Recife, às margens do estuário do Porto com vista para as esculturas de Francisco Brennand.', 1, -8.0631, -34.8711, 150, 4.9, 42, 1, NULL, 'https://images.unsplash.com/photo-1599839575945-a9e5af0c3fa5', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(101, 'Parque das Graças', 'Parque linear às margens do Rio Capibaribe com pistas de cooper, mirantes contemplativos e muita área verde.', 1, -8.0450, -34.9050, 100, 4.8, 15, 1, NULL, 'https://images.unsplash.com/photo-1519331379826-f10be5486c6f', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(102, 'Praia de Boa Viagem', 'A orla urbana mais famosa da capital pernambucana, com arrecifes naturais, águas mornas e quiosques com água de coco.', 1, -8.1278, -34.8998, 120, 4.7, 36, 1, NULL, 'https://images.unsplash.com/photo-1507525428034-b723cf961d3e', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(103, 'Instituto Ricardo Brennand', 'Complexo cultural com arquitetura de castelo medieval que abriga o maior acervo de armaduras e pinturas do Brasil holandês.', 1, -8.0583, -34.9669, 200, 5.0, 58, 1, NULL, 'https://images.unsplash.com/photo-1582555172866-f73bb12a2ab3', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(104, 'Alto da Sé', 'Mirante histórico no topo de Olinda com vista deslumbrante para a orla de Recife, feira de artesanato e a famosa Catedral da Sé.', 1, -8.0019, -34.8488, 180, 4.9, 64, 2, NULL, 'https://images.unsplash.com/photo-1596701062351-8c2c14d1fdd0', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Pontos Turísticos Imediatos (Distâncias entre 70m e 850m da simulação em -8.0675, -34.9167)
(105, 'Praça da Ilha do Retiro', 'Praça arborizada com bancos, ciclovia e centro de convivência do tradicional bairro da Ilha do Retiro.', 1, -8.0673, -34.9163, 120, 4.9, 38, 1, NULL, 'https://images.unsplash.com/photo-1519331379826-f10be5486c6f', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(106, 'Ponte da Madalena & Mirante Capibaribe', 'Histórica travessia sobre o Rio Capibaribe com mirante espetacular e vista panorâmica para as pontes de Recife.', 1, -8.0658, -34.9148, 150, 4.9, 52, 1, NULL, 'https://images.unsplash.com/photo-1506744038136-46273834b3fb', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(107, 'Parque Ecológico do Rio Capibaribe', 'Área de preservação e trilha ecológica urbana às margens do rio, ideal para caminhadas, passeios de caiaque e contemplação.', 1, -8.0692, -34.9180, 180, 4.8, 44, 1, NULL, 'https://images.unsplash.com/photo-1441974231531-c6227db76b6e', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(108, 'Estádio Ilha do Retiro (Adelmar da Costa Carvalho)', 'Templo do futebol pernambucano inaugurado em 1937, palco de jogos históricos da Copa do Mundo de 1950.', 1, -8.0630, -34.9030, 200, 4.8, 77, 1, NULL, 'https://images.unsplash.com/photo-1508098682722-e99c43a406b2', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(109, 'Museu da Abolição', 'Antigo Casarão Sobrado Grande da Madalena dedicado à memória, história e cultura afro-brasileira em Pernambuco.', 1, -8.0560, -34.9070, 220, 5.0, 65, 1, NULL, 'https://images.unsplash.com/photo-1566127444979-b3d2b654e3d7', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
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
(256, 'Camiseta Recife & Olinda 100% Algodão', 'Camiseta estilizada em algodão nobre com arte inspirada nas pontes históricas e no cordel nordestino.', 45.00, 800, 25, true, false, 0, 9, 54, 1, 4.6, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Produtos nas Lojas Próximas da Ilha do Retiro / Madalena
(257, 'Espresso Duplo Especial & Cartola', 'Dose dupla de café espresso especial acompanhado da clássica sobremesa Cartola pernambucana.', 18.00, 350, 40, true, true, 0, 4, 56, 3, 5.0, 19, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(258, 'Cappuccino Cremoso com Canela e Rapadura', 'Cappuccino artesanal finalizado com chantilly caseiro e raspas crocantes de rapadura nordestina.', 14.00, 250, 50, true, true, 0, 4, 56, 3, 4.9, 15, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(259, 'Queijo de Coalho Artesanal Fazenda (500g)', 'Legítimo queijo de coalho pernambucano tostável de fazenda tradicional do Agreste.', 32.00, 600, 30, true, true, 0, 2, 57, 4, 4.9, 21, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(260, 'Manteiga de Garrafa Pura (300ml)', 'Manteiga clarificada artesanalmente segundo a receita sertaneja tradicional.', 24.00, 400, 45, true, false, 0, 2, 57, 4, 4.8, 12, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(261, 'Combo Café Nordestino Completo', 'Cuscuz fofinho recheado, ovo caipira, queijo coalho e café passado na hora.', 20.00, 300, 60, true, true, 0, 4, 58, 5, 4.8, 28, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(262, 'Pão de Fermentação Natural de Queijo do Reino', 'Pão rústico artesanal com crosta dourada e cubos generosos de queijo do reino.', 26.00, 450, 25, true, false, 0, 4, 58, 5, 4.9, 14, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(263, 'Kit Protetor Solar Facial & Labial FPS 50', 'Proteção solar essencial para caminhadas e passeios sob o calor recifense.', 48.00, 700, 35, true, false, 0, 3, 59, 6, 4.8, 9, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(264, 'Água Termal Refrescante 150ml', 'Spray de água termal mineral para hidratação e alívio imediato durante as trilhas e passeios.', 35.00, 500, 40, true, false, 0, 3, 59, 6, 4.7, 7, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
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
DELETE FROM product_images WHERE product_id IN (251, 252, 253, 254, 255, 256, 257, 258, 259, 260, 261, 262, 263, 264);
INSERT INTO product_images (product_id, image_name)
VALUES 
(251, 'https://images.unsplash.com/photo-1578985545062-69928b1d9587'),
(252, 'https://images.unsplash.com/photo-1514565131-fce0801e5785'),
(253, 'https://images.unsplash.com/photo-1628294895950-9805252327bc'),
(254, 'https://images.unsplash.com/photo-1513519245088-0e12902e5a38'),
(255, 'https://images.unsplash.com/photo-1509042239860-f550ce710b93'),
(256, 'https://images.unsplash.com/photo-1521572267360-ee0c2909d518'),
(257, 'https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd'),
(258, 'https://images.unsplash.com/photo-1534778101976-62847782c213'),
(259, 'https://images.unsplash.com/photo-1486297678162-eb2a19b0a32d'),
(260, 'https://images.unsplash.com/photo-1589985270826-4b7bb135bc9d'),
(261, 'https://images.unsplash.com/photo-1525351484163-7529414344d8'),
(262, 'https://images.unsplash.com/photo-1509440159596-0249088772ff'),
(263, 'https://images.unsplash.com/photo-1556228720-195a672e8a03'),
(264, 'https://images.unsplash.com/photo-1608248597359-2e650c82fb1f');

-- 6. Conquistas do Sistema (tb_achievements)
INSERT INTO tb_achievements (id, name, description, image_name, xp_reward, category_id, achievement_category, poi_id, location_name, created_at, updated_at)
VALUES 
(1, 'Primeiro Passo', 'Realizou seu primeiro check-in em um ponto turístico oficial do Passa Aqui.', 'https://images.unsplash.com/photo-1579783900882-c0d3dad7b119', 100, 10, 'DESBRAVADOR', 51, 'Recife - Marco Zero', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Explorador do Recife', 'Descobriu e explorou os monumentos históricos do Recife Antigo.', 'https://images.unsplash.com/photo-1533105079780-92b9be482077', 250, 10, 'RAIZES_DO_BRASIL', 51, 'Recife Antigo', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'Mestre das Ladeiras', 'Subiu o Alto da Sé e contemplou a inesquecível paisagem de Olinda.', 'https://images.unsplash.com/photo-1596701062351-8c2c14d1fdd0', 200, 10, 'DESBRAVADOR', 104, 'Olinda - Alto da Sé', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'Paladar Pernambucano', 'Experimentou o legítimo bolo de rolo ou tapioca pernambucana.', 'https://images.unsplash.com/photo-1555396273-367ea4eb4db5', 150, 1, 'SABORES_DA_MATA', 1, 'Recife & Olinda', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'Colecionador de Aventuras', 'Alcançou mais de 1000 pontos de XP em sua jornada turística.', 'https://images.unsplash.com/photo-1569336415962-a4bd9f69cd83', 300, 10, 'TUDO', NULL, 'Pernambuco', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 'Guardião do Capibaribe', 'Explorou a orla fluvial da Ilha do Retiro e as margens do histórico Rio Capibaribe.', 'https://images.unsplash.com/photo-1506744038136-46273834b3fb', 200, 10, 'DESBRAVADOR', 106, 'Ponte da Madalena & Capibaribe', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, 'Raízes da Ilha', 'Conheceu a Praça da Ilha do Retiro e sua rica vida comunitária.', 'https://images.unsplash.com/photo-1519331379826-f10be5486c6f', 150, 10, 'RAIZES_DO_BRASIL', 105, 'Praça da Ilha do Retiro', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(8, 'Amante do Café da Mata', 'Degustou café artesanal com iguarias pernambucanas nos bistrôs locais.', 'https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb', 150, 4, 'SABORES_DA_MATA', 3, 'Café do Capibaribe', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(9, 'Trilheiro Urbano', 'Caminhou pela trilha do Parque Ecológico do Rio Capibaribe em harmonia com a natureza.', 'https://images.unsplash.com/photo-1441974231531-c6227db76b6e', 220, 10, 'INSTINTO_SELVAGEM', 107, 'Parque Ecológico do Rio Capibaribe', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO UPDATE SET 
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    image_name = EXCLUDED.image_name,
    xp_reward = EXCLUDED.xp_reward,
    category_id = EXCLUDED.category_id,
    achievement_category = EXCLUDED.achievement_category,
    poi_id = EXCLUDED.poi_id,
    location_name = EXCLUDED.location_name,
    updated_at = CURRENT_TIMESTAMP;

-- 7. Conquistas Desbloqueadas pelos Turistas (tb_user_achievements)
INSERT INTO tb_user_achievements (id, user_id, achievement_id, poi_id, location_name, unlocked_at)
VALUES 
(1, 52, 1, 51, 'Recife - Marco Zero', CURRENT_TIMESTAMP - interval '3 days'),
(2, 53, 1, 51, 'Recife - Marco Zero', CURRENT_TIMESTAMP - interval '3 days'),
(3, 53, 6, 106, 'Ponte da Madalena & Capibaribe', CURRENT_TIMESTAMP - interval '1 day'),
(4, 53, 7, 105, 'Praça da Ilha do Retiro', CURRENT_TIMESTAMP - interval '5 hours'),
(5, 52, 7, 105, 'Praça da Ilha do Retiro', CURRENT_TIMESTAMP - interval '1 day'),
(6, 1, 1, 51, 'Recife - Marco Zero', CURRENT_TIMESTAMP - interval '2 days'),
(7, 1, 7, 105, 'Praça da Ilha do Retiro', CURRENT_TIMESTAMP - interval '6 hours')
ON CONFLICT (user_id, achievement_id) DO NOTHING;

-- 8. Histórico de Visitas / Check-ins do Turista (poi_visit_model)
INSERT INTO poi_visit_model (id, poi_id, user_id, xp_earned, distance_km, visited_at)
VALUES 
(1, 51, 52, 150, 0.04, CURRENT_TIMESTAMP - interval '3 days'),
(2, 101, 52, 100, 0.02, CURRENT_TIMESTAMP - interval '2 days'),
(3, 51, 53, 150, 0.04, CURRENT_TIMESTAMP - interval '3 days'),
(4, 101, 53, 100, 0.02, CURRENT_TIMESTAMP - interval '2 days'),
(5, 105, 53, 120, 0.05, CURRENT_TIMESTAMP - interval '5 hours'),
(6, 106, 53, 150, 0.12, CURRENT_TIMESTAMP - interval '1 day'),
(7, 105, 52, 120, 0.06, CURRENT_TIMESTAMP - interval '1 day'),
(8, 105, 1, 120, 0.04, CURRENT_TIMESTAMP - interval '6 hours')
ON CONFLICT (id) DO NOTHING;

-- 9. Avaliações de Pontos de Interesse (poi_rating_model)
INSERT INTO poi_rating_model (id, poi_id, user_id, rating, created_at)
VALUES 
(1, 51, 52, 5, CURRENT_TIMESTAMP - interval '3 days'),
(2, 101, 52, 5, CURRENT_TIMESTAMP - interval '2 days'),
(3, 51, 53, 5, CURRENT_TIMESTAMP - interval '3 days'),
(4, 105, 53, 5, CURRENT_TIMESTAMP - interval '5 hours'),
(5, 106, 53, 5, CURRENT_TIMESTAMP - interval '1 day'),
(6, 3, 53, 5, CURRENT_TIMESTAMP - interval '4 hours'),
(7, 105, 52, 5, CURRENT_TIMESTAMP - interval '1 day'),
(8, 4, 52, 5, CURRENT_TIMESTAMP - interval '8 hours')
ON CONFLICT (poi_id, user_id) DO UPDATE SET rating = EXCLUDED.rating;

-- 10. Pedidos de Demonstração (tb_orders)
-- Diversos status: COMPLETED, READY_FOR_PICKUP, PREPARING, AWAITING_PAYMENT
INSERT INTO tb_orders (id, tourist_id, shopkeeper_id, product_id, quantity, total_amount, cash_discount, status, code, pickup_code, redemption_code, transaction_id, redeemed_at, created_at, updated_at)
VALUES 
-- Pedidos concluídos
('a1b2c3d4-e5f6-4a1b-8c2d-3e4f5a6b7c8d', 52, 54, 251, 1, 28.00, 5.00, 'COMPLETED', 'ORD-9841', 'BR5421', '948271', 'tx-mvp-completed-001', CURRENT_TIMESTAMP - interval '1 day', CURRENT_TIMESTAMP - interval '2 days', CURRENT_TIMESTAMP - interval '1 day'),
('c3d4e5f6-a1b2-4c3d-ae4f-5a6b7c8d9e0f', 53, 54, 251, 1, 28.00, 5.00, 'COMPLETED', 'ORD-7712', 'BR7712', '839201', 'tx-mvp-completed-003', CURRENT_TIMESTAMP - interval '1 day', CURRENT_TIMESTAMP - interval '2 days', CURRENT_TIMESTAMP - interval '1 day'),
('e5f6a1b2-c3d4-4e5f-8a1b-2c3d4e5f6a1b', 53, 56, 257, 1, 18.00, 3.50, 'COMPLETED', 'ORD-4401', 'CF4401', '391824', 'tx-capibaribe-001', CURRENT_TIMESTAMP - interval '3 hours', CURRENT_TIMESTAMP - interval '4 hours', CURRENT_TIMESTAMP - interval '3 hours'),

-- Pedidos prontos para retirada (Permite testar na apresentação o fluxo de retirada com código / QR)
('b2c3d4e5-f6a1-4b2c-9d3e-4f5a6b7c8d9e', 52, 54, 252, 1, 15.00, 0.00, 'READY_FOR_PICKUP', 'ORD-1024', 'SF1024', '562184', 'tx-mvp-ready-002', NULL, CURRENT_TIMESTAMP - interval '2 hours', CURRENT_TIMESTAMP - interval '1 hour'),
('d4e5f6a1-b2c3-4d4e-bf5a-6b7c8d9e0f1a', 53, 54, 252, 1, 15.00, 0.00, 'READY_FOR_PICKUP', 'ORD-1025', 'SF1025', '718293', 'tx-mvp-ready-004', NULL, CURRENT_TIMESTAMP - interval '2 hours', CURRENT_TIMESTAMP - interval '1 hour'),
('f6a1b2c3-d4e5-4f6a-9b2c-3d4e5f6a1b2c', 53, 56, 258, 1, 14.00, 2.00, 'READY_FOR_PICKUP', 'ORD-2048', 'CP2048', '194820', 'tx-capibaribe-002', NULL, CURRENT_TIMESTAMP - interval '40 minutes', CURRENT_TIMESTAMP - interval '10 minutes'),
('11223344-5566-4778-8899-aabbccddeeff', 53, 57, 259, 1, 32.00, 6.00, 'READY_FOR_PICKUP', 'ORD-3099', 'EM3099', '820491', 'tx-madalena-001', NULL, CURRENT_TIMESTAMP - interval '30 minutes', CURRENT_TIMESTAMP - interval '5 minutes'),

-- Pedidos em preparação e aguardando pagamento
('22334455-6677-4889-99aa-bbccddeeff00', 53, 58, 261, 2, 40.00, 5.00, 'PREPARING', 'ORD-5120', 'PD5120', '491028', 'tx-ilha-prep-001', NULL, CURRENT_TIMESTAMP - interval '15 minutes', CURRENT_TIMESTAMP - interval '5 minutes'),
('33445566-7788-499a-aabb-ccddeeff0011', 53, 59, 263, 1, 48.00, 0.00, 'AWAITING_PAYMENT', 'ORD-6001', 'DG6001', '739104', 'tx-drogaria-await-001', NULL, CURRENT_TIMESTAMP - interval '5 minutes', CURRENT_TIMESTAMP - interval '5 minutes')
ON CONFLICT (id) DO UPDATE SET 
    status = EXCLUDED.status,
    redeemed_at = EXCLUDED.redeemed_at;

-- 11. Avaliações de Produtos Comprados (product_rating_model)
INSERT INTO product_rating_model (id, product_id, user_id, rating, comment, order_code, created_at)
VALUES 
(2, 251, 52, 5, 'Bolo de rolo espetacular! Camadas finíssimas e goiabada de alta qualidade. Recomendo muito!', 'ORD-9841', CURRENT_TIMESTAMP - interval '1 day'),
(3, 251, 53, 5, 'Uma das melhores lembranças gastronômicas de Recife. Atendimento excelente no resgate.', 'ORD-7712', CURRENT_TIMESTAMP - interval '1 day'),
(4, 257, 53, 5, 'Café excepcional com a Cartola quentinha! O resgate com pontos foi perfeito.', 'ORD-4401', CURRENT_TIMESTAMP - interval '2 hours'),
(5, 259, 52, 5, 'Queijo coalho maravilhoso, tosta muito bem na chapa e sabor sem igual.', 'ORD-3099', CURRENT_TIMESTAMP - interval '4 hours')
ON CONFLICT (product_id, user_id) DO UPDATE SET rating = EXCLUDED.rating, comment = EXCLUDED.comment, order_code = EXCLUDED.order_code;

-- 11.1 Imagens das Avaliações (product_rating_images)
DELETE FROM product_rating_images WHERE rating_id IN (2, 3, 4, 5);
INSERT INTO product_rating_images (rating_id, image_name)
VALUES 
(2, 'https://images.unsplash.com/photo-1578985545062-69928b1d9587'),
(4, 'https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd');

-- 12. Atualização das Sequences
SELECT setval('city_model_seq', GREATEST((SELECT COALESCE(MAX(id), 0) FROM city_model), 100), true);
SELECT setval('category_model_seq', GREATEST((SELECT COALESCE(MAX(id), 0) FROM category_model), 100), true);
SELECT setval('user_model_seq', GREATEST((SELECT COALESCE(MAX(id), 0) FROM user_model), 300), true);
SELECT setval('poi_model_seq', GREATEST((SELECT COALESCE(MAX(id), 0) FROM poi_model), 300), true);
SELECT setval('product_model_seq', GREATEST((SELECT COALESCE(MAX(id), 0) FROM product_model), 500), true);
SELECT setval('tb_achievements_seq', GREATEST((SELECT COALESCE(MAX(id), 0) FROM tb_achievements), 100), true);
SELECT setval('tb_user_achievements_seq', GREATEST((SELECT COALESCE(MAX(id), 0) FROM tb_user_achievements), 100), true);
SELECT setval('poi_visit_model_seq', GREATEST((SELECT COALESCE(MAX(id), 0) FROM poi_visit_model), 100), true);
SELECT setval('poi_rating_model_seq', GREATEST((SELECT COALESCE(MAX(id), 0) FROM poi_rating_model), 100), true);
SELECT setval('product_rating_model_seq', GREATEST((SELECT COALESCE(MAX(id), 0) FROM product_rating_model), 100), true);
