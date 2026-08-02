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

Configure as variáveis de ambiente

### Banco de Dados e JWT

```bash
# Windows (PowerShell)
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/passaaqui"
$env:SPRING_DATASOURCE_USERNAME="postgres"
$env:SPRING_DATASOURCE_PASSWORD="postgres"
$env:JWT_SECRET="your-jwt-secret-key"

# Linux / macOS
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/passaaqui
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=postgres
export JWT_SECRET=your-jwt-secret-key
```

> [!IMPORTANT]
> O `JWT_SECRET` deve ser uma chave de no mínimo 256 bits (32 caracteres) para o algoritmo HS256

### Serviços externos

Algumas funcionalidades dependem de serviços terceiros. Obtenha as credenciais nos links abaixo e configure as variáveis de ambiente correspondentes.

| Variável | Onde obter |
|---|---|
| `ABACATEPAY_API_KEY` | [app.abacatepay.com](https://app.abacatepay.com/) |
| `ABACATEPAY_WEBHOOK_SECRET` | [app.abacatepay.com](https://app.abacatepay.com/) |
| `OPENROUTESERVICE_API_KEY` | [openrouteservice.org](https://openrouteservice.org/) |
| `MINIO_URL` / `MINIO_URL_EXTERNAL` / `MINIO_ACCESS_KEY` / `MINIO_SECRET_KEY` / `MINIO_BUCKET_NAME` | Sua instância MinIO (ou use as do `docker-compose.yaml`) |

Exemplo de configuração:

```bash
# Windows (PowerShell)
$env:ABACATEPAY_API_KEY="sua-chave-aqui"
$env:ABACATEPAY_WEBHOOK_SECRET="seu-webhook-secret"
$env:OPENROUTESERVICE_API_KEY="sua-chave-aqui"

# Linux / macOS
export ABACATEPAY_API_KEY=sua-chave-aqui
export ABACATEPAY_WEBHOOK_SECRET=seu-webhook-secret
export OPENROUTESERVICE_API_KEY=sua-chave-aqui
```

### Redis

Por padrão o Redis assume `localhost:6379` sem senha. Se precisar customizar:

```bash
export REDIS_HOST=localhost
export REDIS_PORT=6379
export REDIS_PASSWORD=
```

### XP Calculation

Os parâmetros de cálculo de XP possuem valores padrão, mas podem ser sobrescritos:

```bash
export XP_TAKE_RATE=0.05
export XP_MARGIN_FACTOR=0.60
export XP_ABSOLUTE_CEILING=15.00
export XP_CONVERSION_FACTOR=100
```

### Docker Compose

Se estiver usando o `docker-compose.yaml` do projeto, as variáveis de banco, MinIO e Redis já vêm pré-configuradas. Basta definir as variáveis dos serviços externos no arquivo ou no ambiente da máquina host.

Suba o banco de dados e a aplicação com Docker

```
docker compose up -d
```

A aplicação estará disponível em `http://localhost:8080`

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
