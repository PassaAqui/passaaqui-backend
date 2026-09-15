# Testes de Carga com k6 - Passa Aqui Backend

Este diretório contém os scripts de teste de carga e estresse utilizando **k6** para simular **200 usuários simultâneos** (100 turistas e 100 comerciantes) consumindo a API da aplicação.

---

## Estrutura dos Scripts

- `load_test_200_users.js`: Script orquestrador principal configurado com dois cenários paralelos (`tourists_scenario` e `shopkeepers_scenario`) totalizando 200 Virtual Users (VUs).
- `config.js`: Variáveis de ambiente, gerador de CPF válido para cadastro e helpers de cabeçalhos HTTP.
- `scenarios/tourist_flow.js`: Simula a jornada do turista (cadastro, login, listagem de POIs por geolocalização, busca de produtos no catálogo e histórico de pedidos).
- `scenarios/shopkeeper_flow.js`: Simula a jornada do comerciante (login, acesso ao dashboard de métricas, produtos do lojista, métricas de catálogo e listagem de pedidos).

---

## Como Executar

### Opção 1: Executando via Docker Build (Recomendado no Windows / Linux / macOS)

1. Faça o build da imagem do k6:
```bash
docker build -t passaaqui-k6 k6
```

2. Execute o teste apontando para o backend rodando localmente:
```bash
docker run --rm -i -e BASE_URL=http://host.docker.internal:8080 passaaqui-k6
```
*(No Linux, você pode usar `--network="host"` e `-e BASE_URL=http://localhost:8080`)*.

### Opção 2: Executando com k6 nativo

Se você tiver o `k6` instalado localmente:

```bash
k6 run k6/load_test_200_users.js
```

Para customizar a URL da API ou credenciais de comerciante:

```bash
k6 run -e BASE_URL=http://localhost:8080 -e SHOPKEEPER_EMAIL=seu_lojista@email.com -e SHOPKEEPER_PASSWORD=SuaSenha@123 k6/load_test_200_users.js
```

---

## Parâmetros e Métricas de Sucesso

- **VUs simultâneos:** 200 (100 turistas + 100 comerciantes).
- **Ramp-up:** 30 segundos até atingir 200 VUs.
- **Sustentação:** 1 minuto em carga máxima constante.
- **Ramp-down:** 15 segundos para desaquecimento.
- **Critérios (Thresholds):**
  - `p(95)` do tempo de resposta `< 2500ms`
  - `p(99)` do tempo de resposta `< 5000ms`
  - Taxa de falhas de requisições HTTP `< 15%`
