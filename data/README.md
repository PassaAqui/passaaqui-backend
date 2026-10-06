# Dados e Volumes — Passa Aqui Backend

Esta pasta contém os recursos de dados e scripts de inicialização (seeds) do ambiente de desenvolvimento.

## 💾 Persistência dos Dados (Volumes do Docker)
- O Docker Compose utiliza o volume nomeado **`postgres_data`** para persistir as tabelas e dados do PostgreSQL em `/var/lib/docker/volumes/passaaqui-backend_postgres_data`.
- **Nota técnica sobre volumes:** Como este repositório está montado sobre uma partição formatada em **exFAT**, o PostgreSQL do Docker não pode usar montagem direta (`bind mount`) nesta pasta, pois o sistema de arquivos exFAT não oferece suporte a permissões POSIX (`chmod 700` / `chown postgres`). O volume gerenciado pelo Docker garante integridade e persistência de alto desempenho.

---

## 🚀 Como Popular o Banco de Dados

Para carregar todos os dados de demonstração do MVP para o turista, execute:

```bash
docker exec -i passaaqui_postgres psql -U postgres -d passaaqui < data/seed-mvp.sql
```

---

## 👥 Credenciais Criadas para Testes do MVP

Todos os usuários abaixo utilizam a senha: **`Password@123`**

| Perfil | E-mail | Nome / Descrição | Dados Relevantes |
|---|---|---|---|
| **Turista (Principal)** | `turista@passaaqui.com` | Turista Passa Aqui | 1250 XP, Nível 2, Histórico de Viagem, Conquistas e Pedidos |
| **Turista (Secundário)** | `tourist.test@example.com` | Turista Teste | 1500 XP, Nível 2 |
| **Lojista (Recife)** | `shopkeeper.test@example.com` | Lojista Artesanatos Recife | Armazém do Artesanato (Marco Zero) |
| **Lojista (Olinda)** | `lojista@passaaqui.com` | Lojista Olinda Sabores | Sabores do Alto da Sé (Olinda) |
| **Admin Root** | `root@passaaqui.com` | Administrador Root | Permissão total de administrador |

---

## 📦 Conteúdo do MVP Populado

- **Cidades:** Recife (PE) e Olinda (PE) com limites geográficos, descrição e imagens.
- **Categorias:** Alimentação, Artesanato, Cultura e Lazer, Mercado, Farmácia, Padaria, etc.
- **Pontos Turísticos (POIs):**
  - Praça do Marco Zero (150 XP)
  - Parque das Graças (100 XP)
  - Praia de Boa Viagem (120 XP)
  - Instituto Ricardo Brennand (200 XP)
  - Alto da Sé - Olinda (180 XP)
  - Lojas físicas cadastradas para resgate de produtos.
- **Produtos:** Bolo de Rolo, Sombrinha de Frevo, Tapioca com Queijo Coalho, Imã de Geladeira dos Bonecos Gigantes, Café Especial, Camiseta.
- **Conquistas:** Conquistas cadastradas e conquista *"Primeiro Passo"* desbloqueada para o turista.
- **Pedidos de Teste:**
  - Pedido `COMPLETED` resgatado para testar histórico e avaliação de produto comprado.
  - Pedido `READY_FOR_PICKUP` para testar tela de pedidos ativos e código de retirada (pickup code).
- **Check-ins e Avaliações:** Visitas registradas gerando histórico de viagem e avaliações já atribuídas.
