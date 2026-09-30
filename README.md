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
| Carga / Performance | `k6/` | Testes de carga com k6 (200 e 1000 VUs) |

### Testes de Carga e Performance (k6)

O projeto conta com uma suíte de testes de estresse com [k6](./k6/README.md) simulando fluxos concorrentes reais de turistas e comerciantes (autenticação JWT, consultas geoespaciais, pedidos, catálogo e dashboards).

#### Principais Métricas Obtidas (Benchmark Oficial):

| Métrica Principal | 200 Usuários Simultâneos | 1000 Usuários Simultâneos | Avaliação |
| :--- | :--- | :--- | :--- |
| **VUs Concorrentes** | 200 (100 turistas + 100 lojistas) | **1.000** (500 turistas + 500 lojistas) | **100% Sustentado** |
| **Total de Requisições** | 17.844 reqs | **85.335 reqs** | **Escalabilidade 4.7x** |
| **Throughput Médio** | 164,17 reqs/s | **785,05 reqs/s** | **Excelente vazão** |
| **Latência Mediana (p50)** | 141,54 ms | **2,83 ms** | **Praticamente instantâneo** |
| **Latência p(90)** | 825,13 ms | **72,10 ms** | **Sub-100ms** |
| **Latência p(95)** | 1,10 s | **1,61 s** | **Aprovado** |
| **Taxa de Sucesso (Checks)** | 99,99% | **98,74%** (84.263/85.335) | **Altíssima consistência** |
| **Erros 500 / Quedas** | **0** | **0** | **100% Estabilidade** |

> Para mais detalhes e instruções de execução dos testes de carga, consulte a [documentação do k6](./k6/README.md).

> [!WARNING]
> ATENÇÃO: é preciso ter o Java 21 e o Maven instalados em sua máquina

A configuração das chaves e parâmetros da aplicação é centralizada em **[`src/main/resources`](./src/main/resources)** através dos arquivos de propriedades do Spring Boot:

- **`application.properties`**: Arquivo base da aplicação. Carrega as variáveis de ambiente do sistema ou do Docker e inclui automaticamente o arquivo local de segredos (`application-secrets.properties`), se ele existir.
- **`application-secrets.properties`**: Arquivo dedicado para desenvolvedores inserirem suas credenciais, chaves de API e segredos locais diretamente na pasta `src/main/resources/`. **Este arquivo já está no `.gitignore` e nunca é enviado ao repositório**, garantindo segurança no versionamento.
- **`application-dev.properties`**: Perfil de desenvolvimento com banco H2 em memória e configurações mock para testes rápidos locais sem dependências externas (`spring.profiles.active=dev`).

---

### 🔑 Guia de Obtenção e Configuração das Chaves (API Keys)

A aplicação integra com serviços externos para pagamento, cálculo de rotas e armazenamento de arquivos. Siga o passo a passo para obter cada chave:

#### 1. AbacatePay (Gateway de Pagamento PIX)
*Necessário para geração de cobranças PIX e webhooks de pedidos.*
- **Onde obter:** Acesse o painel do [AbacatePay](https://app.abacatepay.com/) e faça login ou crie uma conta gratuita.
- **API Key (`abacatepay.api.key`):** Vá em **Configurações / Integrações** (ou modo Sandbox/Desenvolvimento) e copie sua chave de API iniciada em `abc_dev_...` (para testes) ou `abc_prod_...`.
- **Webhook Secret (`abacatepay.webhook.secret`):** Cadastre uma URL de webhook (ex: `https://seu-dominio/api/orders/webhook`) ou configure uma palavra-chave para validação da assinatura HMAC dos webhooks. Em desenvolvimento local, qualquer valor pode ser usado (ex: `testes`).

#### 2. OpenRouteService (Rotas e Distâncias)
*Necessário para o cálculo de rotas turísticas, POIs e distâncias geográficas.*
- **Onde obter:** Acesse [openrouteservice.org/dev/#/home](https://openrouteservice.org/dev/#/home) e crie uma conta gratuita.
- **API Key (`openrouteservice.api.key`):**
  1. No painel de controle do OpenRouteService, acesse a aba **Tokens**.
  2. Clique em **Request a token**, escolhendo o plano **Free** e tipo **Standard**.
  3. Dê um nome ao token e clique em **Create Token**.
  4. Copie o token gerado (chave alfanumérica/base64 de ~110 caracteres).

#### 3. JWT Secret (Segurança e Autenticação)
- **`jwt.secret`:** Chave criptográfica HMAC-SHA256 para assinatura dos tokens JWT.
- **Requisito:** Deve ter **no mínimo 256 bits (32 caracteres)**.
- Você pode gerar uma chave aleatória no Linux/macOS via terminal:
  ```bash
  openssl rand -base64 32
  ```

#### 4. MinIO / S3 (Armazenamento de Imagens e Documentos)
- Armazena uploads de imagens de usuários, produtos, estabelecimentos e conquistas.
- Ao executar via Docker Compose, o MinIO já sobe configurado.
- Para execução local:
  - `minio.url` / `minio.url.external`: Endpoint de conexão (padrão: `http://localhost:9000`).
  - `minio.access.key` / `minio.secret.key`: Credenciais de acesso root (padrão: `root` / `root@123`).
  - `minio.bucket.name`: Nome do bucket (padrão: `passaaqui-bucket`).

---

### 📝 Como Configurar em `src/main/resources/application-secrets.properties` (Recomendado para Dev Local)

Para iniciar o projeto localmente pelo Maven ou pela sua IDE sem precisar definir variáveis no terminal a cada sessão, crie ou edite o arquivo **`src/main/resources/application-secrets.properties`**:

```properties
# src/main/resources/application-secrets.properties (Ignorado pelo Git)

# JWT (mínimo 32 caracteres)
jwt.secret=WIT7xsOCEKXtRi3mQA1ZMsOc5wMODgTXimEvVbbNBNm

# AbacatePay
abacatepay.api.key=abc_dev_qKYZEFLXu4PEcKPAFtH4Tkt2
abacatepay.webhook.secret=testes

# OpenRouteService
openrouteservice.api.key=eyJvcmciOiI1YjNjZTM1OTc4NTExMTAwMDFjZjYyNDgiLCJpZCI6ImZlNjc1ZDlmZTFmZDRmMjA5NWEyZmY2YTFhNGE2ZGE4IiwiaCI6Im11cm11cjY0In0=

# MinIO
minio.access.key=root
minio.secret.key=root@123
```

O `src/main/resources/application.properties` está configurado para carregar automaticamente esse arquivo através de `spring.config.import=optional:classpath:application-secrets.properties`.

---

### Alternativa: Variáveis de Ambiente no Terminal

Caso prefira injetar as chaves diretamente no ambiente ou utilize CI/CD:

**Linux / macOS:**
```bash
export JWT_SECRET="WIT7xsOCEKXtRi3mQA1ZMsOc5wMODgTXimEvVbbNBNm"
export SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/passaaqui"
export SPRING_DATASOURCE_USERNAME="postgres"
export SPRING_DATASOURCE_PASSWORD="postgres"
export ABACATEPAY_API_KEY="sua-chave-abacatepay"
export ABACATEPAY_WEBHOOK_SECRET="seu-webhook-secret"
export OPENROUTESERVICE_API_KEY="seu-token-openrouteservice"
export MINIO_URL="http://localhost:9000"
export MINIO_URL_EXTERNAL="http://localhost:9000"
export MINIO_ACCESS_KEY="root"
export MINIO_SECRET_KEY="root@123"
export MINIO_BUCKET_NAME="passaaqui-bucket"
```

**Windows (PowerShell):**
```powershell
$env:JWT_SECRET="WIT7xsOCEKXtRi3mQA1ZMsOc5wMODgTXimEvVbbNBNm"
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/passaaqui"
$env:SPRING_DATASOURCE_USERNAME="postgres"
$env:SPRING_DATASOURCE_PASSWORD="postgres"
$env:ABACATEPAY_API_KEY="sua-chave-abacatepay"
$env:ABACATEPAY_WEBHOOK_SECRET="seu-webhook-secret"
$env:OPENROUTESERVICE_API_KEY="seu-token-openrouteservice"
$env:MINIO_URL="http://localhost:9000"
$env:MINIO_URL_EXTERNAL="http://localhost:9000"
$env:MINIO_ACCESS_KEY="root"
$env:MINIO_SECRET_KEY="root@123"
$env:MINIO_BUCKET_NAME="passaaqui-bucket"
```

---

### Tabela Resumo das Propriedades

| Propriedade (`application.properties`) | Variável de Ambiente | Obrigatória | Padrão / Exemplo | Descrição |
|---|---|---|---|---|
| `jwt.secret` | `JWT_SECRET` | Sim | `WIT7xsOCEKXt...` | Assinatura JWT (mínimo 32 caracteres) |
| `spring.datasource.url` | `SPRING_DATASOURCE_URL` | Sim | `jdbc:postgresql://localhost:5432/passaaqui` | URL de conexão PostgreSQL |
| `spring.datasource.username` | `SPRING_DATASOURCE_USERNAME` | Sim | `postgres` | Usuário do banco |
| `spring.datasource.password` | `SPRING_DATASOURCE_PASSWORD` | Sim | `postgres` | Senha do banco |
| `abacatepay.api.key` | `ABACATEPAY_API_KEY` | Sim | `abc_dev_...` | Chave no [AbacatePay](https://app.abacatepay.com/) |
| `abacatepay.webhook.secret` | `ABACATEPAY_WEBHOOK_SECRET` | Sim | `testes` | Validação HMAC de webhook |
| `openrouteservice.api.key` | `OPENROUTESERVICE_API_KEY` | Sim | Token base64 | Token em [openrouteservice.org](https://openrouteservice.org/) |
| `minio.url` | `MINIO_URL` | Sim | `http://localhost:9000` | URL do servidor MinIO |
| `minio.url.external` | `MINIO_URL_EXTERNAL` | Sim | `http://localhost:9000` | URL pública para imagens |
| `minio.access.key` | `MINIO_ACCESS_KEY` | Sim | `root` | Access Key do MinIO |
| `minio.secret.key` | `MINIO_SECRET_KEY` | Sim | `root@123` | Secret Key do MinIO |
| `minio.bucket.name` | `MINIO_BUCKET_NAME` | Sim | `passaaqui-bucket` | Bucket do MinIO |
| `spring.data.redis.password` | `REDIS_PASSWORD` | Não | Vazio | Senha do Redis |
| `xp.take-rate` | `XP_TAKE_RATE` | Não | `0.05` | Taxa de XP (5%) |
| `xp.margin-factor` | `XP_MARGIN_FACTOR` | Não | `0.60` | Margem de XP (60%) |
| `xp.absolute-ceiling` | `XP_ABSOLUTE_CEILING` | Não | `15.00` | Teto absoluto (R$ 15,00) |
| `xp.conversion-factor` | `XP_CONVERSION_FACTOR` | Não | `100` | Conversão (100 pts/R$) |

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
