-- Adiciona a coluna achievement_category na tabela tb_achievements com valor padrão 'TUDO'
ALTER TABLE tb_achievements ADD COLUMN IF NOT EXISTS achievement_category VARCHAR(50) DEFAULT 'TUDO';

-- Atualiza registros existentes que estejam com a categoria nula
UPDATE tb_achievements SET achievement_category = 'TUDO' WHERE achievement_category IS NULL;
