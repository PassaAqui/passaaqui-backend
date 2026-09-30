<img
  src="./docs/banner-passaaqui.png"
/>

# Passa Aqui — Backend

API REST do **Passa Aqui**, plataforma que conecta turistas a pontos de interesse e comércios locais.

## Como rodar o projeto

### Versão das tecnologias utilizadas:

**Java: 21+**

**Maven: 3.9+**

Clone o repositório

```
git clone https://github.com/PassaAqui/passaqui-backend.git
cd passaaqui-backend/backend
```

Compile o projeto

```
./mvnw clean package -DskipTests
```

## Testes

Execute **todos** os testes (unitários, integração e security) com um único comando:

```bash
./mvnw test --no-transfer-progress
```

### Relatório formatado

Para uma saída mais legível com sumário detalhado por classe de teste:

**Windows (PowerShell)**
```powershell
.\test-report.ps1
```

**Linux / macOS**
```bash
./test-report.sh
```

### Organização dos testes

| Tipo | Localização | Descrição |
|---|---|---|
| Unitários | `src/test/java/.../unit/` | Testes isolados com Mockito |
| Integração | `src/test/java/.../integration/` | Testes com banco H2 (`@DataJpaTest`) |
| Security | `src/test/java/.../security/` | Testes de segurança (`@SpringBootTest`) |

> [!WARNING]
> ATENÇÃO: é preciso ter o Java 21 e o Maven instalados em sua máquina

Configure as variáveis de ambiente necessárias para a inicialização da API. O repositório disponibiliza o arquivo [`.env.example`](./.env.example) como modelo de referência contendo todas as variáveis e valores sugeridos para o ambiente de desenvolvimento local.

### 🔑 Guia de Obtenção e Configuração das Chaves (API Keys)

A aplicação integra com serviços externos para pagamento, cálculo de rotas e armazenamento de arquivos. Siga o passo a passo para obter cada chave:

#### 1. AbacatePay (Gateway de Pagamento PIX)
*Necessário para geração de cobranças PIX e webhooks de pedidos.*
- **Onde obter:** Acesse o painel do [AbacatePay](https://app.abacatepay.com/) e faça login ou crie uma conta gratuita.
- **API Key (`ABACATEPAY_API_KEY`):** Vá em **Configurações / Integrações** (ou modo Sandbox/Desenvolvimento) e copie sua chave de API iniciada em `abc_dev_...` (para testes) ou `abc_prod_...`.
- **Webhook Secret (`ABACATEPAY_WEBHOOK_SECRET`):** Cadastre uma URL de webhook (ex: `https://seu-dominio/api/orders/webhook`) ou configure uma palavra-chave segura para validação da assinatura HMAC dos webhooks. Em desenvolvimento local, qualquer string configurada é aceita (ex: `testes`).

#### 2. OpenRouteService (Rotas e Distâncias)
*Necessário para o cálculo de rotas turísticas, POIs e distâncias geográficas.*
- **Onde obter:** Acesse [openrouteservice.org/dev/#/home](https://openrouteservice.org/dev/#/home) e crie uma conta gratuita.
- **API Key (`OPENROUTESERVICE_API_KEY`):**
  1. No painel de controle do OpenRouteService, navegue até a aba **Tokens**.
  2. Clique em **Request a token** selecionando o plano **Free** e tipo **Standard**.
  3. Dê um nome ao token e clique em **Create Token**.
  4. Copie o token gerado (uma chave alfanumérica/base64 de ~110 caracteres) e atribua à variável `OPENROUTESERVICE_API_KEY`.

#### 3. JWT Secret (Segurança e Autenticação)
- **`JWT_SECRET`:** Chave criptográfica HMAC-SHA256 para assinatura dos tokens JWT.
- **Requisito:** Deve ter **no mínimo 256 bits (32 caracteres)**.
- Você pode gerar uma chave aleatória no Linux/macOS via terminal:
  ```bash
  openssl rand -base64 32
  ```

#### 4. MinIO / S3 (Armazenamento de Imagens e Documentos)
- Armazena uploads de imagens de usuários, produtos, estabelecimentos e conquistas.
- Se você executar via Docker Compose, o MinIO já sobe automaticamente configurado.
- Para execução local direta:
  - `MINIO_URL`: Endpoint de conexão interna (ex: `http://localhost:9000`).
  - `MINIO_URL_EXTERNAL`: Endpoint público acessível pelo front-end/browser (ex: `http://localhost:9000`).
  - `MINIO_ACCESS_KEY` / `MINIO_SECRET_KEY`: Credenciais de acesso root (padrão: `root` / `root@123`).
  - `MINIO_BUCKET_NAME`: Nome do bucket (padrão: `passaaqui-bucket`).

---

### Tabela Resumo das Variáveis

| Variável | Obrigatória | Padrão / Exemplo | Descrição / Onde Obter |
|---|---|---|---|
| `JWT_SECRET` | Sim | `WIT7xsOCEKXt...` | Chave de assinatura JWT (mínimo 32 caracteres) |
| `SPRING_DATASOURCE_URL` | Sim | `jdbc:postgresql://localhost:5432/passaaqui` | URL de conexão PostgreSQL |
| `SPRING_DATASOURCE_USERNAME` | Sim | `postgres` | Usuário do banco de dados |
| `SPRING_DATASOURCE_PASSWORD` | Sim | `postgres` | Senha do banco de dados |
| `ABACATEPAY_API_KEY` | Sim | `abc_dev_...` | Chave de API no [AbacatePay](https://app.abacatepay.com/) |
| `ABACATEPAY_WEBHOOK_SECRET` | Sim | `testes` | Segredo para validação de webhook |
| `OPENROUTESERVICE_API_KEY` | Sim | Token de ~110 chars | Token gerado em [openrouteservice.org](https://openrouteservice.org/) |
| `MINIO_URL` | Sim | `http://localhost:9000` | URL do servidor MinIO |
| `MINIO_URL_EXTERNAL` | Sim | `http://localhost:9000` | URL pública de leitura de imagens |
| `MINIO_ACCESS_KEY` | Sim | `root` | Access Key do MinIO |
| `MINIO_SECRET_KEY` | Sim | `root@123` | Secret Key do MinIO |
| `MINIO_BUCKET_NAME` | Sim | `passaaqui-bucket` | Nome do bucket para imagens |
| `REDIS_PASSWORD` | Não | Vazio | Senha do servidor Redis (se houver) |
| `XP_TAKE_RATE` | Não | `0.05` | Taxa de conversão de XP (padrão 5%) |
| `XP_MARGIN_FACTOR` | Não | `0.60` | Fator de margem (padrão 60%) |
| `XP_ABSOLUTE_CEILING` | Não | `15.00` | Teto absoluto de XP (padrão R$ 15,00) |
| `XP_CONVERSION_FACTOR` | Não | `100` | Fator de conversão (100 pts por Real) |

---

### Como Exportar as Variáveis no Ambiente Local

Se preferir rodar a aplicação fora do Docker (via `./mvnw spring-boot:run` ou pela sua IDE):

**Linux / macOS:**
```bash
# Copie o arquivo de exemplo ou exporte as variáveis no terminal
export JWT_SECRET="WIT7xsOCEKXtRi3mQA1ZMsOc5wMODgTXimEvVbbNBNm"
export SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/passaaqui"
export SPRING_DATASOURCE_USERNAME="postgres"
export SPRING_DATASOURCE_PASSWORD="postgres"
export MINIO_URL="http://localhost:9000"
export MINIO_URL_EXTERNAL="http://localhost:9000"
export MINIO_ACCESS_KEY="root"
export MINIO_SECRET_KEY="root@123"
export MINIO_BUCKET_NAME="passaaqui-bucket"
export ABACATEPAY_API_KEY="sua-chave-abacatepay"
export ABACATEPAY_WEBHOOK_SECRET="seu-webhook-secret"
export OPENROUTESERVICE_API_KEY="seu-token-openrouteservice"
```

**Windows (PowerShell):**
```powershell
$env:JWT_SECRET="WIT7xsOCEKXtRi3mQA1ZMsOc5wMODgTXimEvVbbNBNm"
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/passaaqui"
$env:SPRING_DATASOURCE_USERNAME="postgres"
$env:SPRING_DATASOURCE_PASSWORD="postgres"
$env:MINIO_URL="http://localhost:9000"
$env:MINIO_URL_EXTERNAL="http://localhost:9000"
$env:MINIO_ACCESS_KEY="root"
$env:MINIO_SECRET_KEY="root@123"
$env:MINIO_BUCKET_NAME="passaaqui-bucket"
$env:ABACATEPAY_API_KEY="sua-chave-abacatepay"
$env:ABACATEPAY_WEBHOOK_SECRET="seu-webhook-secret"
$env:OPENROUTESERVICE_API_KEY="seu-token-openrouteservice"
```

---

### Executando com Docker Compose

Ao executar com Docker Compose, todos os serviços periféricos (PostgreSQL, Redis, MinIO) já sobem orquestrados e prontos para uso:

```bash
docker compose up -d
```

A aplicação backend estará disponível em `http://localhost:8080`.

### Criar administrador inicial

Após subir o projeto pela primeira vez, execute o script SQL para criar o administrador padrão:

A senha configurada como padrão é: `Root@123`

**Linux / macOS**
```bash
psql -U postgres -d passaaqui < create-admin.sql
```

**Windows (PowerShell)**
```powershell
Get-Content create-admin.sql | psql -U postgres -d passaaqui
```

**Com Docker (Linux / macOS / WSL)**
```bash
docker exec -i passaaqui_postgres psql -U postgres -d passaaqui < create-admin.sql
```

**Com Docker (Windows PowerShell)**
```powershell
Get-Content create-admin.sql | docker exec -i passaaqui_postgres psql -U postgres -d passaaqui
```

**Credenciais padrão:**

| Campo | Valor |
|---|---|
| E-mail | `root@passaaqui.com` |
| Senha | `Root@123` |
| Tipo | `ADMIN_ROOT` |

## Documentação das rotas

A documentação completa de todos os endpoints da API está disponível em [`API_DOCUMENTATION.md`](./API_DOCUMENTATION.md).

## Tecnologias

| | |
|---|---|
| Framework | Spring Boot 4.0.5 |
| Segurança | Spring Security + JWT (jjwt 0.13.0) |
| ORM | Spring Data JPA / Hibernate |
| Banco | PostgreSQL 16 |
| Build | Maven |
| Docker | Docker Compose |
