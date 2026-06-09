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

> [!WARNING]
> ATENÇÃO: é preciso ter o Java 21 e o Maven instalados em sua máquina

Configure as variáveis de ambiente

```
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

Suba o banco de dados e a aplicação com Docker

```
docker compose up -d
```

A aplicação estará disponível em `http://localhost:8080`

### Criar administrador inicial

Após subir o projeto pela primeira vez, execute o script SQL para criar o administrador padrão:

A senha configurada como padrão é: `Teste@123`

```
docker exec -i passaaqui_postgres psql -U postgres -d passaaqui < create-admin.sql
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
