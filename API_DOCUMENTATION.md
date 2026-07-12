# Passa Aqui — API Documentation

## 📋 Introdução

### Objetivo

API REST do **Passa Aqui**, plataforma que conecta turistas a pontos de interesse (POIs) e comércios locais. Permite que turistas naveguem por POIs, avaliem locais, acumulem XP e resgatem produtos; lojistas cadastrem seus produtos; e administradores gerenciem toda a plataforma.

### Tecnologias

| Tecnologia | Versão |
|---|---|
| Java | 21 |
| Spring Boot | 4.0.5 |
| Spring Security | 6.x |
| Spring Data JPA / Hibernate | — |
| PostgreSQL | — |
| JWT (jjwt 0.13.0) | — |
| Maven | — |
| Spring WebSocket / STOMP | — |

### Autenticação

- **JWT** via header `Authorization` no formato `Bearer <token>`
- `access_token`: expira em **15 minutos**
- `refresh_token`: expira em **7 dias**
- O token JWT contém as claims: `sub` (user ID), `role`, `deviceId` e `adminType` (apenas para admins)
- Para acessar endpoints protegidos, envie o header `Authorization: Bearer <access_token>`
- Para refresh, envie o `refresh_token` no header `Authorization: Bearer <refresh_token>`

### URL Base

```
http://localhost:8080/api
```

> A URL base completa depende do ambiente. Todos os endpoints abaixo assumem o prefixo `/api`.

### Formato das Respostas

**Sucesso:** O corpo da resposta contém diretamente o objeto/lista JSON da entidade.

**Erro (tratado globalmente):**
```json
{
  "timestamp": "2026-05-24T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Descrição do erro",
  "path": "/api/exemplo"
}
```

### Códigos de Erro Padrão

| Status | Significado |
|---|---|
| 400 | Bad Request — validação de campos ou requisição inválida |
| 401 | Unauthorized — token ausente, inválido ou expirado |
| 403 | Forbidden — role sem permissão para o recurso |
| 404 | Not Found — recurso não encontrado |
| 409 | Conflict — conflito (e.g., e-mail duplicado) |

### Convenções

- **Identificador de usuário:** endpoints que aceitam `{identifier}` permitem ID (Integer) ou e-mail (String com `@`)
- **Roles:** `TOURIST`, `SHOPKEEPER`, `ADMIN` (com subtipos `ADMIN_USER` e `ADMIN_ROOT`)
- **Datas:** formato ISO 8601 (`yyyy-MM-dd'T'HH:mm:ss`)
- **IDs:** gerados automaticamente pelo banco (Integer)

---

## 📑 Sumário

### 🔐 Autenticação
- [`POST /api/auth/register/tourist`](#post-apiauthregistertourist)
- [`POST /api/auth/register/shopkeeper`](#post-apiauthregistershopkeeper)
- [`POST /api/auth/login`](#post-apiauthlogin)
- [`GET /api/auth/refresh`](#get-apiauthrefresh)
- [`GET /api/auth/logout`](#get-apiauthlogout)

### 👤 Usuários
- [`GET /api/users`](#get-apiusers)
- [`GET /api/users/{identifier}`](#get-apiusersidentifier)

### 🛡️ Administradores
- [`POST /api/admin`](#post-apiadmin)
- [`GET /api/admin`](#get-apiadmin)
- [`GET /api/admin/{id}`](#get-apiadminid)
- [`GET /api/admin/email`](#get-apiadminemail)
- [`PUT /api/admin/{id}`](#put-apiadminid)
- [`DELETE /api/admin/{id}`](#delete-apiadminid)

### 🏖️ Turistas
- [`GET /api/tourists`](#get-apitourists)
- [`GET /api/tourists/{identifier}`](#get-apitouristsidentifier)
- [`PUT /api/tourists/{identifier}`](#put-apitouristsidentifier)
- [`DELETE /api/tourists/{identifier}`](#delete-apitouristsidentifier)

### 🏪 Lojistas
- [`GET /api/shopkeepers`](#get-apishopkeepers)
- [`GET /api/shopkeepers/{identifier}`](#get-apishopkeepersidentifier)
- [`PUT /api/shopkeepers/{identifier}`](#put-apishopkeepersidentifier)
- [`DELETE /api/shopkeepers/{identifier}`](#delete-apishopkeepersidentifier)

### 🏙️ Cidades
- [`POST /api/city/create`](#post-apicitycreate)
- [`GET /api/city`](#get-apicity)
- [`GET /api/city/{id}`](#get-apicityid)
- [`PUT /api/city/{id}`](#put-apicityid)
- [`DELETE /api/city/{id}`](#delete-apicityid)
- [`POST /api/city/locate`](#post-apicitylocate)

### 📂 Categorias
- [`POST /api/categories`](#post-apicategories)
- [`GET /api/categories`](#get-apicategories)
- [`GET /api/categories/{id}`](#get-apicategoriesid)
- [`PUT /api/categories/{id}`](#put-apicategoriesid)
- [`DELETE /api/categories/{id}`](#delete-apicategoriesid)

### 📍 Pontos de Interesse (POIs)
- [`POST /api/pois`](#post-appois)
- [`GET /api/pois`](#get-appois)
- [`GET /api/pois/{id}`](#get-appoisid)
- [`PUT /api/pois/{id}`](#put-appoisid)
- [`DELETE /api/pois/{id}`](#delete-appoisid)
- [`POST /api/pois/{poiId}/checkin`](#post-appoispoidcheckin)
- [`POST /api/pois/{poiId}/ratings`](#post-appoispoidratings)
- [`GET /api/pois/{poiId}/ratings`](#get-appoispoidratings)

### 🧭 Direções
- [`POST /api/direction`](#post-apidirection)

### 🗺️ Rota (Route Session)
- [`POST /api/route/start`](#post-apiroutestart)
- [`GET /api/route/current`](#get-apiroutecurrent)
- [`POST /api/route/location`](#post-apiroutelocation)
- [`DELETE /api/route/current`](#delete-apiroutecurrent)

### 🛒 Produtos
- [`POST /api/products`](#post-apiproducts)
- [`GET /api/products`](#get-apiproducts)
- [`GET /api/products/recent`](#get-apiproductsrecent)
- [`GET /api/products/{id}`](#get-apiproductsid)
- [`PUT /api/products/{id}`](#put-apiproductsid)
- [`DELETE /api/products/{id}`](#delete-apiproductsid)

### 🛒 Pedidos (Orders)
- [`POST /api/orders/checkout`](#post-apiorderscheckout)
- [`GET /api/orders/shopkeeper`](#get-apiordersshopkeeper)
- [`GET /api/orders/shopkeeper/history`](#get-apiordersshopkeeperhistory)
- [`GET /api/orders/my-current`](#get-apiordersmy-current)
- [`GET /api/orders/my-history`](#get-apiordersmyhistory)

## 🧭 Direções (Rotas)

### POST /api/direction

#### Descrição

Calcula a **rota** entre dois pontos geográficos (origem e destino) utilizando a **OpenRouteService API**. Retorna as coordenadas do trajeto, duração e distância.

#### Controller

`DirectionController`

#### Autenticação

✅ Obrigatória

#### Permissões

Apenas `TOURIST`

#### Headers

| Nome | Obrigatório | Descrição |
|---|---|---|
| Authorization | Sim | `Bearer <access_token>` |
| Content-Type | Sim | `application/json` |

#### Request Body

| Campo | Tipo | Obrigatório | Validação |
|---|---|---|---|---|
| mode | String | Sim | Tipo de locomoção (ex: `driving-car`, `foot-walking`, `cycling-regular`) |
| startLongitude | Double | Sim | Longitude do ponto de partida |
| startLatitude | Double | Sim | Latitude do ponto de partida |
| endLongitude | Double | Sim | Longitude do destino |
| endLatitude | Double | Sim | Latitude do destino |
| poiId | Integer | Não | ID do POI de destino (opcional — usado para auto check-in ao chegar) |

**Modos disponíveis:**

| Modo | Descrição |
|---|---|
| `driving-car` | Carro |
| `driving-hgv` | Caminhão |
| `cycling-regular` | Bicicleta comum |
| `cycling-road` | Bicicleta de estrada |
| `cycling-mountain` | Mountain bike |
| `cycling-electric` | Bicicleta elétrica |
| `foot-walking` | A pé |
| `foot-hiking` | Trilha |
| `wheelchair` | Cadeira de rodas |

**Exemplo:**

```json
{
  "mode": "driving-car",
  "startLongitude": -46.6576,
  "startLatitude": -23.5874,
  "endLongitude": -46.6333,
  "endLatitude": -23.5505,
  "poiId": 1
}
```

#### Response 200 (OK)

Resposta da OpenRouteService contendo as informações da rota.

```json
{
  "routes": [...]
}
```

> ⚠️ O formato exato da resposta depende da API da OpenRouteService.

#### Possíveis Erros

| Status | Motivo |
|---|---|
| 400 | Modo de locomoção inválido |
| 400 | Dados de validação inválidos |
| 400 | Nenhuma sessão de rota ativa |
| 401 | Token ausente ou inválido |
| 403 | Role não é TOURIST |

#### Integração com Route Session

Ao calcular uma direção, o sistema **automaticamente atualiza a sessão de rota ativa** do turista com os dados de destino (`startLatitude`, `startLongitude`, `stopLatitude`, `stopLongitude`, `mode`, `poiId`). Se fornecido `poiId`, o sistema associará o destino ao POI para detecção de chegada. Se o turista não possui uma rota ativa (`POST /api/route/start` não foi chamado antes), o endpoint retorna **400** com a mensagem "No active route session. Start a route first.".

---

## 🗺️ Rota (Route Session)

### POST /api/route/start

#### Descrição

Inicia uma **nova sessão de rota** para o turista. A sessão é armazenada no **Redis** com TTL de **25 minutos** (renovado a cada requisição). Se já existir uma sessão ativa, o TTL é renovado e a sessão existente é retornada.

#### Controller

`RouteController`

#### Autenticação

✅ Obrigatória

#### Permissões

Apenas `TOURIST`

#### Headers

| Nome | Obrigatório | Descrição |
|---|---|---|
| Authorization | Sim | `Bearer <access_token>` |
| Content-Type | Não | `application/json` (se houver body) |

#### Request Body

Opcional. Se enviado, define a localização inicial do turista.

| Campo | Tipo | Obrigatório | Validação |
|---|---|---|---|---|
| latitude | Double | Não | — |
| longitude | Double | Não | — |
| poiId | Integer | Não | ID do POI de destino (opcional) |

**Exemplo:**
```json
{
  "latitude": -23.5505,
  "longitude": -46.6333,
  "poiId": 1
}
```

#### Response 200 (OK)

```json
{
  "status": "ACTIVE",
  "destination": null,
  "lastLocation": {
    "latitude": -23.5505,
    "longitude": -46.6333
  }
}
```

Se não houver body, `lastLocation` será `null`.

#### Possíveis Erros

| Status | Motivo |
|---|---|
| 401 | Token ausente ou inválido |
| 403 | Role não é TOURIST |

---

### GET /api/route/current

#### Descrição

Retorna a **sessão de rota ativa** do turista autenticado, incluindo status, destino e última localização conhecida.

#### Controller

`RouteController`

#### Autenticação

✅ Obrigatória

#### Permissões

Apenas `TOURIST`

#### Headers

| Nome | Obrigatório | Descrição |
|---|---|---|
| Authorization | Sim | `Bearer <access_token>` |

#### Response 200 (OK)

```json
{
  "status": "ACTIVE",
  "destination": {
    "startLatitude": -23.5874,
    "startLongitude": -46.6576,
    "stopLatitude": -23.5505,
    "stopLongitude": -46.6333,
    "mode": "driving-car",
    "poiId": 1
  },
  "lastLocation": {
    "latitude": -23.5505,
    "longitude": -46.6333
  }
}
```

#### Possíveis Erros

| Status | Motivo |
|---|---|
| 401 | Token ausente ou inválido |
| 403 | Role não é TOURIST |
| 404 | Nenhuma rota ativa encontrada |

---

### POST /api/route/location

#### Descrição

Atualiza a **localização atual** do turista na sessão de rota ativa e **transmite em tempo real** para o tópico WebSocket `/topic/routes/tracking/{userId}`. Utilizado para **tracking ao vivo**: o app envia a coordenada GPS periodicamente (ex: a cada 5 segundos) e quem estiver inscrito no tópico recebe a posição em tempo real.

> ⚠️ Requer uma sessão de rota ativa. Se não houver, retorna 400.

#### Auto Check-in ao Chegar no POI

Se a rota possuir um destino com `poiId` associado, o sistema automaticamente verifica a cada atualização de localização se o turista **chegou ao POI**. A detecção considera:
- **Bounding box** do POI (`minLatitude`, `maxLatitude`, `minLongitude`, `maxLongitude`) — se definida
- **Raio de 100m** do centro do POI — caso não haja bounding box

Quando a chegada é detectada, o sistema:
1. Dispara o **check-in** automático no POI
2. Calcula o XP com base na **distância percorrida** (Haversine do ponto de partida ao POI)
3. **Acumula o XP** no saldo do turista (`currentXP`)
4. Notifica via WebSocket em `/user/{userId}/queue/poi` com ação `checkin-result`

#### Controller

`RouteController`

#### Autenticação

✅ Obrigatória

#### Permissões

Apenas `TOURIST`

#### Headers

| Nome | Obrigatório | Descrição |
|---|---|---|
| Authorization | Sim | `Bearer <access_token>` |
| Content-Type | Sim | `application/json` |

#### Request Body

| Campo | Tipo | Obrigatório | Validação |
|---|---|---|---|
| latitude | Double | Sim | — |
| longitude | Double | Sim | — |

**Exemplo:**
```json
{
  "latitude": -23.5505,
  "longitude": -46.6333
}
```

#### Response 200 (OK)

Corpo vazio.

#### Possíveis Erros

| Status | Motivo |
|---|---|
| 400 | Nenhuma rota ativa |
| 401 | Token ausente ou inválido |
| 403 | Role não é TOURIST |

---

### DELETE /api/route/current

#### Descrição

**Encerra** a sessão de rota ativa: remove os dados do Redis, limpa o rastreamento de sessão WebSocket e notifica o usuário via `/user/{userId}/queue/route` com ação `route-ended`. O cliente deve se desconectar do WebSocket ao receber essa notificação.

#### Controller

`RouteController`

#### Autenticação

✅ Obrigatória

#### Permissões

Apenas `TOURIST`

#### Headers

| Nome | Obrigatório | Descrição |
|---|---|---|
| Authorization | Sim | `Bearer <access_token>` |

#### Response 204 (No Content)

Corpo vazio.

#### Possíveis Erros

| Status | Motivo |
|---|---|
| 401 | Token ausente ou inválido |
| 403 | Role não é TOURIST |

---

### Fluxo Completo: Direction → Route → Tracking

1. **Iniciar rota:** `POST /api/route/start` → sessão criada no Redis (TTL 25min)
2. **Pedir direção:** `POST /api/direction` → calcula rota na OpenRouteService **e** atualiza `destination` na sessão Redis automaticamente (com `poiId` opcional)
3. **Enviar localização:** `POST /api/route/location` (a cada 5s) → atualiza `lastLocation` + **broadcast** via WebSocket para `/topic/routes/tracking/{userId}`
4. **Chegada ao POI:** sistema detecta proximidade → dispara **check-in automático** → calcula e acumula XP → notifica via `/user/{userId}/queue/poi`
5. **Consultar sessão:** `GET /api/route/current` → retorna status + destination + lastLocation atuais
6. **Encerrar rota:** `DELETE /api/route/current` → remove Redis + notifica socket (`route-ended`)
7. **Sessão expira automaticamente** após 25 minutos de inatividade (TTL do Redis)

---

### 📊 Resumo de Endpoints
- [`ws://host/ws`](#ws-websocket-stomp)

---

## 🔐 Autenticação

### POST /api/auth/register/tourist

#### Descrição

Registra uma nova conta de **turista** na plataforma.

#### Controller

`AuthController`

#### Autenticação

❌ **Pública** — não exige token

#### Permissões

Nenhuma (público)

#### Headers

| Nome | Obrigatório | Descrição |
|---|---|---|
| Content-Type | Sim | `application/json` |

#### Request Body

| Campo | Tipo | Obrigatório | Validação |
|---|---|---|---|
| email | String | Sim | E-mail válido |
| name | String | Sim | Não vazio |
| password | String | Sim | 8-16 caracteres, ao menos 1 letra, 1 número, 1 caractere especial |
| confirm_password | String | Sim | Deve ser igual a `password` |
| documentId | String | Sim | 11-14 caracteres, deve ser CPF ou CNPJ válido |

**Exemplo:**

```json
{
  "email": "turista@email.com",
  "name": "João Turista",
  "password": "Senha@123",
  "confirm_password": "Senha@123",
  "documentId": "12345678909"
}
```

#### Response 201 (Created)

Retorna o objeto `TouristModel` criado.

```json
{
  "id": 1,
  "email": "turista@email.com",
  "name": "João Turista",
  "role": "TOURIST",
  "createdAt": "2026-05-24T10:00:00",
  "updatedAt": "2026-05-24T10:00:00",
  "deviceId": null,
  "documentId": "12345678909",
  "lastKnownLocation": null,
  "currentXP": 0,
  "level": 0
}
```

> ⚠️ O campo `password` nunca é retornado nas respostas (é ignorado pelo serializador).

#### Possíveis Erros

| Status | Motivo |
|---|---|
| 400 | Dados inválidos (e-mail, senha, documento) |
| 400 | `password` e `confirm_password` não conferem |
| 409 | E-mail já cadastrado |

---

### POST /api/auth/register/shopkeeper

#### Descrição

Registra uma nova conta de **lojista** na plataforma.

#### Controller

`AuthController`

#### Autenticação

❌ **Pública**

#### Permissões

Nenhuma (público)

#### Headers

| Nome | Obrigatório | Descrição |
|---|---|---|
| Content-Type | Sim | `application/json` |

#### Request Body

| Campo | Tipo | Obrigatório | Validação |
|---|---|---|---|
| email | String | Sim | E-mail válido |
| name | String | Sim | Não vazio |
| password | String | Sim | 8-16 caracteres, ao menos 1 letra, 1 número, 1 caractere especial |
| confirm_password | String | Sim | Deve ser igual a `password` |
| documentId | String | Sim | 14-18 caracteres, CPF ou CNPJ válido |
| companyName | String | Sim | Não vazio |
| description | String | Não | — |
| categoryId | Integer | Sim | ID de categoria existente |
| poiName | String | Sim | Nome do POI (loja física) |
| poiDescription | String | Não | Descrição do POI |
| latitude | Double | Não | Latitude do centro do POI |
| longitude | Double | Não | Longitude do centro do POI |
| minLatitude | Double | Não | Limite sul do geofencing |
| maxLatitude | Double | Não | Limite norte do geofencing |
| minLongitude | Double | Não | Limite oeste do geofencing |
| maxLongitude | Double | Não | Limite leste do geofencing |
| cityId | Integer | Sim | ID de cidade existente |

> O sistema cria automaticamente um **POI do tipo STORE** vinculado ao lojista no momento do registro.

**Exemplo:**

```json
{
  "email": "lojista@email.com",
  "name": "Maria Lojista",
  "password": "Senha@123",
  "confirm_password": "Senha@123",
  "documentId": "11222333000181",
  "companyName": "Maria's Comércio",
  "description": "Loja de artesanato local",
  "categoryId": 1,
  "poiName": "Maria's Comércio",
  "poiDescription": "Loja de artesanato local",
  "latitude": -23.5505,
  "longitude": -46.6333,
  "cityId": 1
}
```

#### Response 201 (Created)

Retorna o objeto `ShopkeeperModel` criado.

```json
{
  "id": 2,
  "email": "lojista@email.com",
  "name": "Maria Lojista",
  "role": "SHOPKEEPER",
  "createdAt": "2026-05-24T10:01:00",
  "updatedAt": "2026-05-24T10:01:00",
  "documentId": "11222333000181",
  "companyName": "Maria's Comércio",
  "description": "Loja de artesanato local",
  "category": {
    "id": 1,
    "name": "Alimentação",
    "description": "Restaurantes, lanchonetes e food trucks"
  }
}
```

#### Possíveis Erros

| Status | Motivo |
|---|---|
| 400 | Dados inválidos |
| 400 | `password` e `confirm_password` não conferem |
| 404 | `categoryId` não encontrado |
| 404 | `cityId` não encontrado |
| 409 | E-mail já cadastrado |

---

### POST /api/auth/login

#### Descrição

Realiza o login e retorna tokens JWT nos cookies `access_token` e `refresh_token`.

#### Controller

`AuthController`

#### Autenticação

❌ **Pública**

#### Permissões

Nenhuma (público)

#### Headers

| Nome | Obrigatório | Descrição |
|---|---|---|
| Content-Type | Sim | `application/json` |

#### Request Body

| Campo | Tipo | Obrigatório | Validação |
|---|---|---|---|
| email | String | Sim | E-mail válido |
| password | String | Sim | Não vazio |

**Exemplo:**

```json
{
  "email": "turista@email.com",
  "password": "Senha@123"
}
```

#### Response 200 (OK)

```json
{
  "access_token": "eyJhbGciOiJIUzI1NiJ9...",
  "refresh_token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

> Os tokens devem ser armazenados pelo cliente e enviados no header `Authorization` das requisições autenticadas como `Bearer <access_token>`. O `refresh_token` deve ser enviado no header `Authorization: Bearer <refresh_token>` para renovar o access token.

#### Possíveis Erros

| Status | Motivo |
|---|---|
| 400 | Credenciais inválidas (e-mail ou senha incorretos) |

---

### GET /api/auth/refresh

#### Descrição

Renova o `access_token` usando o `refresh_token` enviado no header `Authorization`.

#### Controller

`AuthController`

#### Autenticação

❌ **Pública** (usa o `refresh_token` do header)

#### Permissões

Nenhuma (baseada no token de refresh)

#### Headers

| Nome | Obrigatório | Descrição |
|---|---|---|
| Authorization | Sim | `Bearer <refresh_token>` |

#### Response 200 (OK)

```json
{
  "access_token": "eyJhbGciOiJIUzI1NiJ9...",
  "refresh_token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

#### Possíveis Erros

| Status | Motivo |
|---|---|
| 404 | Header `Authorization` ausente |
| 400 | Token inválido, expirado ou sessão revogada |

---

### GET /api/auth/logout

#### Descrição

Revoga a sessão atual.

#### Controller

`AuthController`

#### Autenticação

❌ **Pública** (usa o `refresh_token` do header)

#### Headers

| Nome | Obrigatório | Descrição |
|---|---|---|
| Authorization | Não | `Bearer <refresh_token>` da sessão a ser revogada |

#### Response 200 (OK)

Corpo vazio.

#### Possíveis Erros

| Status | Motivo |
|---|---|
| 400 | Token com formato inválido |

---

## 👤 Usuários

### GET /api/users

#### Descrição

Retorna a lista de **todos os usuários** cadastrados (tourists, shopkeepers e admins).

#### Controller

`UserController`

#### Autenticação

✅ Obrigatória

#### Permissões

A classe `UserController` possui `@PreAuthorize("hasRole('ROLE_ADMIN')")` — entretanto, o `SecurityFilter` nunca cria uma authority com o nome `ROLE_ROLE_ADMIN` (apenas `ROLE_ADMIN_USER` ou `ROLE_ADMIN_ROOT`). Isso torna este endpoint **inacessível** na prática (veja [Observações Técnicas](#-observações-técnicas)).

#### Headers

| Nome | Obrigatório | Descrição |
|---|---|---|
| Authorization | Sim | `Bearer <access_token>` |

#### Response 200 (OK)

```json
[
  {
    "id": 1,
    "email": "turista@email.com",
    "name": "João Turista",
    "role": "TOURIST",
    "createdAt": "2026-05-24T10:00:00",
    "updatedAt": "2026-05-24T10:00:00"
  },
  {
    "id": 2,
    "email": "lojista@email.com",
    "name": "Maria Lojista",
    "role": "SHOPKEEPER",
    "createdAt": "2026-05-24T10:01:00",
    "updatedAt": "2026-05-24T10:01:00"
  }
]
```

> ⚠️ O campo `password` é ignorado na serialização. O campo `authSessions` (lista de sessões) possui `@JsonIgnore`.

#### Possíveis Erros

| Status | Motivo |
|---|---|
| 401 | Token ausente ou inválido |

---

### GET /api/users/{identifier}

#### Descrição

Retorna um usuário específico por **ID** ou **e-mail**.

#### Controller

`UserController`

#### Autenticação

✅ Obrigatória

#### Permissões

Mesmo problema do endpoint anterior — `@PreAuthorize("hasRole('ROLE_ADMIN')")` nunca corresponde a uma authority real.

#### Path Params

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `identifier` | String | ID (Integer) ou e-mail do usuário |

#### Response 200 (OK)

```json
{
  "id": 1,
  "email": "turista@email.com",
  "name": "João Turista",
  "role": "TOURIST",
  "createdAt": "2026-05-24T10:00:00",
  "updatedAt": "2026-05-24T10:00:00"
}
```

#### Possíveis Erros

| Status | Motivo |
|---|---|
| 400 | Formato do `identifier` inválido |
| 401 | Token ausente ou inválido |
| 404 | Usuário não encontrado |

---

## 🛡️ Administradores

### POST /api/admin

#### Descrição

Cria um novo **administrador** na plataforma.

#### Controller

`AdminController`

#### Autenticação

✅ Obrigatória

#### Permissões

`ADMIN_ROOT`

#### Headers

| Nome | Obrigatório | Descrição |
|---|---|---|
| Authorization | Sim | `Bearer <access_token>` |
| Content-Type | Sim | `application/json` |

#### Request Body

| Campo | Tipo | Obrigatório | Validação |
|---|---|---|---|
| email | String | Sim | E-mail válido |
| name | String | Sim | Não vazio |
| password | String | Sim | Deve seguir regra de senha (8-16 chars, letra+número+especial) |
| confirm_password | String | Sim | Deve ser igual a `password` |
| adminType | String (enum) | Sim | `USER` ou `ROOT` |

**Exemplo:**

```json
{
  "email": "admin@email.com",
  "name": "Admin Root",
  "password": "Admin@123",
  "confirm_password": "Admin@123",
  "adminType": "ROOT"
}
```

#### Response 200 (OK)

```json
{
  "id": 3,
  "email": "admin@email.com",
  "name": "Admin Root",
  "role": "ADMIN",
  "adminType": "ROOT",
  "createdAt": "2026-05-24T11:00:00",
  "updatedAt": "2026-05-24T11:00:00"
}
```

#### Possíveis Erros

| Status | Motivo |
|---|---|
| 400 | Dados inválidos ou senhas não conferem |
| 409 | E-mail já em uso |

---

### GET /api/admin

#### Descrição

Lista **todos os administradores** cadastrados.

#### Controller

`AdminController`

#### Autenticação

✅ Obrigatória

#### Permissões

`ADMIN_ROOT`

#### Response 200 (OK)

```json
[
  {
    "id": 3,
    "email": "admin@email.com",
    "name": "Admin Root",
    "role": "ADMIN",
    "adminType": "ROOT",
    "createdAt": "2026-05-24T11:00:00",
    "updatedAt": "2026-05-24T11:00:00"
  }
]
```

#### Possíveis Erros

| Status | Motivo |
|---|---|
| 401 | Token ausente ou inválido |
| 403 | Usuário não é ADMIN_ROOT |

---

### GET /api/admin/{id}

#### Descrição

Retorna um administrador específico por **ID**.

#### Controller

`AdminController`

#### Autenticação

✅ Obrigatória

#### Permissões

`ADMIN_ROOT`

#### Path Params

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `id` | Integer | ID do administrador |

#### Response 200 (OK)

```json
{
  "id": 3,
  "email": "admin@email.com",
  "name": "Admin Root",
  "role": "ADMIN",
  "adminType": "ROOT",
  "createdAt": "2026-05-24T11:00:00",
  "updatedAt": "2026-05-24T11:00:00"
}
```

#### Possíveis Erros

| Status | Motivo |
|---|---|
| 401 | Token ausente ou inválido |
| 403 | Usuário não é ADMIN_ROOT |
| 404 | Admin não encontrado |

---

### GET /api/admin/email

#### Descrição

Retorna um administrador por **e-mail**.

#### Controller

`AdminController`

#### Autenticação

✅ Obrigatória

#### Permissões

`ADMIN_ROOT`

#### Query Params

| Parâmetro | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `email` | String | Sim | E-mail do administrador |

#### Response 200 (OK)

```json
{
  "id": 3,
  "email": "admin@email.com",
  "name": "Admin Root",
  "role": "ADMIN",
  "adminType": "ROOT",
  "createdAt": "2026-05-24T11:00:00",
  "updatedAt": "2026-05-24T11:00:00"
}
```

#### Possíveis Erros

| Status | Motivo |
|---|---|
| 400 | Parâmetro `email` ausente |
| 401 | Token ausente ou inválido |
| 403 | Usuário não é ADMIN_ROOT |
| 404 | Admin não encontrado |

---

### PUT /api/admin/{id}

#### Descrição

Atualiza os dados de um administrador.

#### Controller

`AdminController`

#### Autenticação

✅ Obrigatória

#### Permissões

`ADMIN_ROOT`

#### Path Params

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `id` | Integer | ID do administrador |

#### Request Body

| Campo | Tipo | Obrigatório | Validação |
|---|---|---|---|
| email | String | Sim | E-mail válido |
| name | String | Sim | Não vazio |
| password | String | Sim | Deve seguir regra de senha |
| adminType | String (enum) | Sim | `USER` ou `ROOT` |

**Exemplo:**

```json
{
  "email": "admin.novo@email.com",
  "name": "Admin Atualizado",
  "password": "Nova@Senha1",
  "adminType": "USER"
}
```

#### Response 200 (OK)

```json
{
  "id": 3,
  "email": "admin.novo@email.com",
  "name": "Admin Atualizado",
  "role": "ADMIN",
  "adminType": "USER",
  "createdAt": "2026-05-24T11:00:00",
  "updatedAt": "2026-05-24T11:05:00"
}
```

#### Possíveis Erros

| Status | Motivo |
|---|---|
| 400 | Dados inválidos |
| 401 | Token ausente ou inválido |
| 403 | Usuário não é ADMIN_ROOT |
| 404 | Admin não encontrado |

---

### DELETE /api/admin/{id}

#### Descrição

Remove um administrador da plataforma.

#### Controller

`AdminController`

#### Autenticação

✅ Obrigatória

#### Permissões

`ADMIN_ROOT`

#### Path Params

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `id` | Integer | ID do administrador |

#### Response 204 (No Content)

Corpo vazio.

#### Possíveis Erros

| Status | Motivo |
|---|---|
| 401 | Token ausente ou inválido |
| 403 | Usuário não é ADMIN_ROOT |
| 404 | Admin não encontrado |

---

## 🏖️ Turistas

### GET /api/tourists

#### Descrição

Lista **todos os turistas** cadastrados.

#### Controller

`TouristController`

#### Autenticação

✅ Obrigatória

#### Permissões

`ADMIN_USER` ou `ADMIN_ROOT`

#### Headers

| Nome | Obrigatório | Descrição |
|---|---|---|
| Authorization | Sim | `Bearer <access_token>` |

#### Response 200 (OK)

```json
[
  {
    "id": 1,
    "email": "turista@email.com",
    "name": "João Turista",
    "role": "TOURIST",
    "createdAt": "2026-05-24T10:00:00",
    "updatedAt": "2026-05-24T10:00:00",
    "deviceId": null,
    "documentId": "12345678909",
    "lastKnownLocation": null,
    "currentXP": 0,
    "level": 0
  }
]
```

#### Possíveis Erros

| Status | Motivo |
|---|---|
| 401 | Token ausente ou inválido |
| 403 | Role não é ADMIN_USER ou ADMIN_ROOT |

---

### GET /api/tourists/{identifier}

#### Descrição

Retorna um turista por **ID** ou **e-mail**.

#### Controller

`TouristController`

#### Autenticação

✅ Obrigatória

#### Permissões

`ADMIN_USER` ou `ADMIN_ROOT`

#### Path Params

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `identifier` | String | ID ou e-mail |

#### Response 200 (OK)

```json
{
  "id": 1,
  "email": "turista@email.com",
  "name": "João Turista",
  "role": "TOURIST",
  "createdAt": "2026-05-24T10:00:00",
  "updatedAt": "2026-05-24T10:00:00",
  "deviceId": null,
  "documentId": "12345678909",
  "lastKnownLocation": null,
  "currentXP": 0,
  "level": 0
}
```

#### Possíveis Erros

| Status | Motivo |
|---|---|
| 400 | Formato do `identifier` inválido |
| 401 | Token ausente ou inválido |
| 403 | Role não autorizada |
| 404 | Turista não encontrado |

---

### PUT /api/tourists/{identifier}

#### Descrição

Atualiza dados de um turista.

#### Controller

`TouristController`

#### Autenticação

✅ Obrigatória

#### Permissões

`ADMIN_USER` ou `ADMIN_ROOT`

#### Path Params

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `identifier` | String | ID ou e-mail |

#### Request Body

| Campo | Tipo | Obrigatório | Validação |
|---|---|---|---|
| name | String | Sim | Não vazio |
| password | String | Não | Se preenchido, deve seguir a regra de senha |
| documentId | String | Sim | Não vazio |

**Exemplo:**

```json
{
  "name": "João Atualizado",
  "password": "Nova@Senha1",
  "documentId": "98765432100"
}
```

#### Response 200 (OK)

```json
{
  "id": 1,
  "email": "turista@email.com",
  "name": "João Atualizado",
  "role": "TOURIST",
  "createdAt": "2026-05-24T10:00:00",
  "updatedAt": "2026-05-24T12:00:00",
  "documentId": "98765432100",
  "currentXP": 0,
  "level": 0
}
```

#### Possíveis Erros

| Status | Motivo |
|---|---|
| 400 | Dados inválidos |
| 401 | Token ausente ou inválido |
| 403 | Role não autorizada |
| 404 | Turista não encontrado |

---

### DELETE /api/tourists/{identifier}

#### Descrição

Remove um turista da plataforma.

#### Controller

`TouristController`

#### Autenticação

✅ Obrigatória

#### Permissões

`ADMIN_USER` ou `ADMIN_ROOT`

#### Path Params

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `identifier` | String | ID ou e-mail |

#### Response 204 (No Content)

Corpo vazio.

#### Possíveis Erros

| Status | Motivo |
|---|---|
| 401 | Token ausente ou inválido |
| 403 | Role não autorizada |
| 404 | Turista não encontrado |

---

## 🏪 Lojistas

### GET /api/shopkeepers

#### Descrição

Lista **todos os lojistas** cadastrados.

#### Controller

`ShopkeeperController`

#### Autenticação

✅ Obrigatória

#### Permissões

`ADMIN_USER` ou `ADMIN_ROOT`

#### Response 200 (OK)

```json
[
  {
    "id": 2,
    "email": "lojista@email.com",
    "name": "Maria Lojista",
    "role": "SHOPKEEPER",
    "createdAt": "2026-05-24T10:01:00",
    "updatedAt": "2026-05-24T10:01:00",
    "documentId": "11222333000181",
    "companyName": "Maria's Comércio",
    "description": "Loja de artesanato local",
    "category": {
      "id": 1,
      "name": "Alimentação",
      "description": "Restaurantes, lanchonetes e food trucks"
    }
  }
]
```

---

### GET /api/shopkeepers/{identifier}

#### Descrição

Retorna um lojista por **ID** ou **e-mail**.

#### Controller

`ShopkeeperController`

#### Autenticação

✅ Obrigatória

#### Permissões

`ADMIN_USER` ou `ADMIN_ROOT`

#### Path Params

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `identifier` | String | ID ou e-mail |

---

### PUT /api/shopkeepers/{identifier}

#### Descrição

Atualiza dados de um lojista.

#### Controller

`ShopkeeperController`

#### Autenticação

✅ Obrigatória

#### Permissões

`ADMIN_USER` ou `ADMIN_ROOT`

#### Path Params

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `identifier` | String | ID ou e-mail |

#### Request Body

| Campo | Tipo | Obrigatório | Validação |
|---|---|---|---|
| name | String | Não | — |
| password | String | Não | Se preenchido, deve seguir a regra de senha |
| documentId | String | Não | — |
| companyName | String | Não | — |
| description | String | Não | — |
| categoryId | Integer | Não | Deve ser um ID de categoria existente |

**Exemplo:**

```json
{
  "name": "Maria Atualizada",
  "companyName": "Novo Comércio",
  "categoryId": 2
}
```

---

### DELETE /api/shopkeepers/{identifier}

#### Descrição

Remove um lojista da plataforma.

#### Controller

`ShopkeeperController`

#### Autenticação

✅ Obrigatória

#### Permissões

`ADMIN_USER` ou `ADMIN_ROOT`

#### Path Params

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `identifier` | String | ID ou e-mail |

#### Response 204 (No Content)

---

## 🏙️ Cidades

### POST /api/city/create

#### Descrição

Cria uma nova cidade. Os dados geográficos (nome, estado, região) são enriquecidos automaticamente via **API externa do IBGE** usando o `ibgeCode`.

#### Controller

`CityController`

#### Autenticação

✅ Obrigatória

#### Permissões

`ADMIN_USER` ou `ADMIN_ROOT`

#### Headers

| Nome | Obrigatório | Descrição |
|---|---|---|
| Authorization | Sim | `Bearer <access_token>` |
| Content-Type | Sim | `application/json` (JSON) ou `multipart/form-data` (com imagem) |

#### Request Body (JSON)

| Campo | Tipo | Obrigatório | Validação |
|---|---|---|---|
| ibgeCode | String | Sim | Código IBGE da cidade |
| description | String | Sim | Não vazio |
| minLatitude | Double | Não | — |
| maxLatitude | Double | Não | — |
| minLongitude | Double | Não | — |
| maxLongitude | Double | Não | — |

**Exemplo JSON:**

```json
{
  "ibgeCode": "3550308",
  "description": "Capital do estado de São Paulo",
  "minLatitude": -23.6821,
  "maxLatitude": -23.3620,
  "minLongitude": -46.8259,
  "maxLongitude": -46.3656
}
```

#### Multipart Form (com imagem)

Para criar com imagem, envie como `multipart/form-data`:
- **data:** JSON string do `CreateCityDTO`
- **image:** arquivo de imagem (opcional)

#### Response 200 (OK)

```json
{
  "id": 1,
  "name": "São Paulo",
  "description": "Capital do estado de São Paulo",
  "state": "SP",
  "ibgeCode": "3550308",
  "region": "Sudeste",
  "microRegion": "São Paulo",
  "mesoRegion": "Metropolitana de São Paulo",
  "stateName": "São Paulo",
  "regionCode": 3,
  "minLatitude": -23.6821,
  "maxLatitude": -23.3620,
  "minLongitude": -46.8259,
  "maxLongitude": -46.3656,
  "createdAt": "2026-05-24T12:00:00",
  "updatedAt": "2026-05-24T12:00:00"
}
```

---

### GET /api/city

#### Descrição

Lista **todas as cidades** cadastradas.

#### Controller

`CityController`

#### Autenticação

✅ Obrigatória

#### Permissões

`ADMIN_USER` ou `ADMIN_ROOT`

#### Response 200 (OK)

```json
[
  {
    "id": 1,
    "name": "São Paulo",
    "description": "Capital do estado de São Paulo",
    "state": "SP",
    "ibgeCode": "3550308",
    "region": "Sudeste",
    "microRegion": "São Paulo",
    "mesoRegion": "Metropolitana de São Paulo",
    "stateName": "São Paulo",
    "regionCode": 3,
    "minLatitude": -23.6821,
    "maxLatitude": -23.3620,
    "minLongitude": -46.8259,
    "maxLongitude": -46.3656,
    "createdAt": "2026-05-24T12:00:00",
    "updatedAt": "2026-05-24T12:00:00"
  }
]
```

---

### GET /api/city/{id}

#### Descrição

Retorna uma cidade específica por ID.

#### Controller

`CityController`

#### Autenticação

✅ Obrigatória

#### Permissões

`ADMIN_USER` ou `ADMIN_ROOT`

#### Path Params

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `id` | Integer | ID da cidade |

#### Response 200 (OK)

```json
{
  "id": 1,
  "name": "São Paulo",
  "description": "Capital do estado de São Paulo",
  "state": "SP",
  "ibgeCode": "3550308",
  "region": "Sudeste",
  "microRegion": "São Paulo",
  "mesoRegion": "Metropolitana de São Paulo",
  "stateName": "São Paulo",
  "regionCode": 3,
  "minLatitude": -23.6821,
  "maxLatitude": -23.3620,
  "minLongitude": -46.8259,
  "maxLongitude": -46.3656,
  "createdAt": "2026-05-24T12:00:00",
  "updatedAt": "2026-05-24T12:00:00"
}
```

#### Possíveis Erros

| Status | Motivo |
|---|---|
| 404 | Cidade não encontrada |

---

### PUT /api/city/{id}

#### Descrição

Atualiza os dados de uma cidade.

#### Controller

`CityController`

#### Autenticação

✅ Obrigatória

#### Permissões

`ADMIN_USER` ou `ADMIN_ROOT`

#### Path Params

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `id` | Integer | ID da cidade |

#### Request Body

| Campo | Tipo | Obrigatório | Validação |
|---|---|---|---|
| name | String | Sim | Não vazio |
| description | String | Sim | Não vazio |
| state | String | Sim | Não vazio |
| ibgeCode | String | Sim | Não vazio |
| region | String | Sim | Não vazio |
| microRegion | String | Sim | Não vazio |
| mesoRegion | String | Sim | Não vazio |
| stateName | String | Sim | Não vazio |
| regionCode | Long | Sim | Não nulo |
| minLatitude | Double | Não | — |
| maxLatitude | Double | Não | — |
| minLongitude | Double | Não | — |
| maxLongitude | Double | Não | — |

**Exemplo:**

```json
{
  "name": "São Paulo",
  "description": "Atualização da descrição",
  "state": "SP",
  "ibgeCode": "3550308",
  "region": "Sudeste",
  "microRegion": "São Paulo",
  "mesoRegion": "Metropolitana de São Paulo",
  "stateName": "São Paulo",
  "regionCode": 3,
  "maxLatitude": -23.3000
}
```

---

### DELETE /api/city/{id}

#### Descrição

Remove uma cidade cadastrada.

#### Controller

`CityController`

#### Autenticação

✅ Obrigatória

#### Permissões

`ADMIN_USER` ou `ADMIN_ROOT`

#### Path Params

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `id` | Integer | ID da cidade |

#### Response 204 (No Content)

---

### POST /api/city/locate

#### Descrição

Localiza uma cidade a partir de coordenadas geográficas (latitude e longitude). O sistema busca qual cidade possui um **bounding box** (minLatitude/maxLatitude, minLongitude/maxLongitude) que contenha o ponto informado.

#### Controller

`CityController`

#### Autenticação

✅ Obrigatória

#### Permissões

`TOURIST`, `SHOPKEEPER`, `ADMIN_USER` ou `ADMIN_ROOT`

#### Headers

| Nome | Obrigatório | Descrição |
|---|---|---|
| Authorization | Sim | `Bearer <access_token>` |
| Content-Type | Sim | `application/json` |

#### Request Body

| Campo | Tipo | Obrigatório | Validação |
|---|---|---|---|
| latitude | Double | Sim | Deve estar entre -90 e 90 |
| longitude | Double | Sim | Deve estar entre -180 e 180 |

**Exemplo:**
```json
{
  "latitude": -23.5505,
  "longitude": -46.6333
}
```

#### Response 200 (OK)

Retorna o objeto completo da cidade cujo bounding box contém as coordenadas fornecidas.

```json
{
  "id": 1,
  "name": "São Paulo",
  "description": "Capital do estado de São Paulo",
  "state": "SP",
  "ibgeCode": "3550308",
  "region": "Sudeste",
  "microRegion": "São Paulo",
  "mesoRegion": "Metropolitana de São Paulo",
  "stateName": "São Paulo",
  "regionCode": 3,
  "minLatitude": -23.6821,
  "maxLatitude": -23.3620,
  "minLongitude": -46.8259,
  "maxLongitude": -46.3656,
  "createdAt": "2026-05-24T12:00:00",
  "updatedAt": "2026-05-24T12:00:00"
}
```

#### Possíveis Erros

| Status | Motivo |
|---|---|
| 400 | Latitude ou longitude ausentes ou inválidos |
| 401 | Token ausente ou inválido |
| 403 | Role não autorizada (TOURIST, SHOPKEEPER, ADMIN) |
| 404 | Nenhuma cidade encontrada para as coordenadas fornecidas |

---

## 📂 Categorias

### POST /api/categories

#### Descrição

Cria uma nova categoria.

#### Controller

`CategoryController`

#### Autenticação

✅ Obrigatória

#### Permissões

`ADMIN_USER` ou `ADMIN_ROOT`

#### Request Body

| Campo | Tipo | Obrigatório | Validação |
|---|---|---|---|
| name | String | Sim | Não vazio |
| description | String | Não | — |

**Exemplo:**

```json
{
  "name": "Padaria",
  "description": "Padarias e confeitarias"
}
```

#### Response 200 (OK)

```json
{
  "id": 4,
  "name": "Padaria",
  "description": "Padarias e confeitarias"
}
```

> ⚠️ As categorias comuns já são **semeadas automaticamente** na inicialização da aplicação via `@PostConstruct`: Alimentação, Mercado, Farmácia, Padaria, Pet Shop, Academia, Beleza, Oficina.

---

### GET /api/categories

#### Descrição

Lista **todas as categorias** cadastradas.

#### Controller

`CategoryController`

#### Autenticação

✅ Obrigatória

#### Permissões

`ADMIN_USER` ou `ADMIN_ROOT`

#### Response 200 (OK)

```json
[
  {
    "id": 1,
    "name": "Alimentação",
    "description": "Restaurantes, lanchonetes e food trucks"
  },
  {
    "id": 2,
    "name": "Mercado",
    "description": "Supermercados, hortifrútis e açougues"
  }
]
```

---

### GET /api/categories/{id}

#### Descrição

Retorna uma categoria por ID.

#### Controller

`CategoryController`

#### Autenticação

✅ Obrigatória

#### Permissões

`ADMIN_USER` ou `ADMIN_ROOT`

#### Path Params

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `id` | Integer | ID da categoria |

#### Response 200 (OK)

```json
{
  "id": 1,
  "name": "Alimentação",
  "description": "Restaurantes, lanchonetes e food trucks"
}
```

---

### PUT /api/categories/{id}

#### Descrição

Atualiza uma categoria.

#### Controller

`CategoryController`

#### Autenticação

✅ Obrigatória

#### Permissões

`ADMIN_USER` ou `ADMIN_ROOT`

#### Path Params

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `id` | Integer | ID da categoria |

#### Request Body

| Campo | Tipo | Obrigatório | Validação |
|---|---|---|---|
| name | String | Não | — |
| description | String | Não | — |

**Exemplo:**

```json
{
  "name": "Padaria & Confeitaria"
}
```

---

### DELETE /api/categories/{id}

#### Descrição

Remove uma categoria.

#### Controller

`CategoryController`

#### Autenticação

✅ Obrigatória

#### Permissões

`ADMIN_USER` ou `ADMIN_ROOT`

#### Path Params

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `id` | Integer | ID da categoria |

#### Response 204 (No Content)

---

## 📍 Pontos de Interesse (POIs)

### POST /api/pois

#### Descrição

Cria um novo **Ponto de Interesse**.

#### Controller

`PoiController`

#### Autenticação

✅ Obrigatória

#### Permissões

`ADMIN_USER` ou `ADMIN_ROOT`

#### Request Body

| Campo | Tipo | Obrigatório | Validação |
|---|---|---|---|
| name | String | Sim | Não vazio |
| description | String | Não | — |
| xpReward | Integer | Não | `>= 0` (ignorado se `type` for `STORE`) |
| type | String (enum) | Sim | `STORE` ou `TOURIST_POINT` |
| latitude | Double | Não | — |
| longitude | Double | Não | — |
| minLatitude | Double | Não | — |
| maxLatitude | Double | Não | — |
| minLongitude | Double | Não | — |
| maxLongitude | Double | Não | — |
| cityId | Integer | Sim | ID de cidade existente |

**Exemplo (ponto turístico):**

```json
{
  "name": "Parque Ibirapuera",
  "description": "Principal parque da cidade",
  "xpReward": 50,
  "type": "TOURIST_POINT",
  "latitude": -23.5874,
  "longitude": -46.6576,
  "cityId": 1
}
```

**Exemplo (loja):**

```json
{
  "name": "Loja de Artesanato",
  "description": "Artesanato local",
  "type": "STORE",
  "latitude": -23.5505,
  "longitude": -46.6333,
  "cityId": 1
}
```

#### Response 200 (OK)

```json
{
  "id": 1,
  "name": "Parque Ibirapuera",
  "description": "Principal parque da cidade",
  "xpReward": 50,
  "type": "TOURIST_POINT",
  "latitude": -23.5874,
  "longitude": -46.6576,
  "minLatitude": null,
  "maxLatitude": null,
  "minLongitude": null,
  "maxLongitude": null,
  "city": {
    "id": 1,
    "name": "São Paulo",
    "state": "SP"
  },
  "averageRating": null,
  "ratingsCount": null,
  "createdAt": "2026-05-24T13:00:00",
  "updatedAt": "2026-05-24T13:00:00"
}
```

---

### GET /api/pois

#### Descrição

Lista **todos os POIs** cadastrados com **paginação**, incluindo avaliação média.

#### Controller

`PoiController`

#### Autenticação

✅ Obrigatória (qualquer role autenticada)

#### Permissões

`TOURIST`, `SHOPKEEPER`, `ADMIN_USER` ou `ADMIN_ROOT`

#### Query Params (paginação)

| Parâmetro | Tipo | Padrão | Descrição |
|---|---|---|---|
| `page` | Integer | `0` | Número da página |
| `size` | Integer | `20` | Tamanho da página |
| `sort` | String | — | Campo para ordenação (ex: `name,asc`) |

#### Response 200 (OK)

```json
{
  "content": [
    {
      "id": 1,
      "name": "Parque Ibirapuera",
      "description": "Principal parque da cidade",
      "xpReward": 50,
      "type": "TOURIST_POINT",
      "latitude": -23.5874,
      "longitude": -46.6576,
      "city": { "id": 1, "name": "São Paulo", "state": "SP" },
      "averageRating": 4.5,
      "ratingsCount": 10,
      "createdAt": "2026-05-24T13:00:00",
      "updatedAt": "2026-05-24T13:00:00"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

---

### GET /api/pois/{id}

#### Descrição

Retorna um POI específico por ID, incluindo avaliação média.

#### Controller

`PoiController`

#### Autenticação

✅ Obrigatória (qualquer role)

#### Permissões

`TOURIST`, `SHOPKEEPER`, `ADMIN_USER` ou `ADMIN_ROOT`

#### Path Params

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `id` | Integer | ID do POI |

#### Response 200 (OK)

```json
{
  "id": 1,
  "name": "Parque Ibirapuera",
  "description": "Principal parque da cidade",
  "xpReward": 50,
  "type": "TOURIST_POINT",
  "latitude": -23.5874,
  "longitude": -46.6576,
  "city": { "id": 1, "name": "São Paulo", "state": "SP" },
  "averageRating": 4.5,
  "ratingsCount": 10,
  "createdAt": "2026-05-24T13:00:00",
  "updatedAt": "2026-05-24T13:00:00"
}
```

---

### PUT /api/pois/{id}

#### Descrição

Atualiza um POI.

#### Controller

`PoiController`

#### Autenticação

✅ Obrigatória

#### Permissões

`ADMIN_USER` ou `ADMIN_ROOT`

#### Path Params

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `id` | Integer | ID do POI |

#### Request Body

Todos os campos opcionais.

| Campo | Tipo | Validação |
|---|---|---|
| name | String | Se preenchido, não vazio |
| description | String | — |
| xpReward | Integer | — |
| type | String (enum) | `STORE` ou `TOURIST_POINT` |
| latitude | Double | — |
| longitude | Double | — |
| minLatitude | Double | — |
| maxLatitude | Double | — |
| minLongitude | Double | — |
| maxLongitude | Double | — |
| cityId | Integer | Deve existir |

**Exemplo:**

```json
{
  "name": "Parque Ibirapuera - Atualizado",
  "xpReward": 75
}
```

---

### POST /api/pois/{poiId}/checkin

#### Descrição

Realiza o **check-in** de um turista em um POI do tipo **ponto turístico**. O sistema calcula o XP com base na **fórmula oficial**:

```
XP = (distancia_km * 2.5) * (100 / (visitas_recentes + 1))
```

**Regras aplicadas:**
- **Anti-farming:** mesmo usuário não pode check-in no mesmo POI dentro de 30 dias
- **Deslocamento mínimo:** `distanciaKm` deve ser >= 0.1 km (caso contrário, tratado como GPS inválido)
- **Invisibilidade:** POIs com mais visitas recentes rendem menos XP
- **Arredondamento:** o XP final é arredondado para o inteiro mais próximo

#### Controller

`PoiController`

#### Autenticação

✅ Obrigatória

#### Permissões

Apenas `TOURIST`

#### Path Params

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `poiId` | Integer | ID do POI |

#### Headers

| Nome | Obrigatório | Descrição |
|---|---|---|
| Authorization | Sim | `Bearer <access_token>` |
| Content-Type | Sim | `application/json` |

#### Request Body

| Campo | Tipo | Obrigatório | Validação |
|---|---|---|---|
| distanceKm | Double | Não | `>= 0` — distância real percorrida em km |

**Exemplo:**
```json
{
  "distanceKm": 3.0
}
```

#### Response 200 (OK)

```json
{
  "xp_concedido": 750,
  "calculo": {
    "distancia_km": 3.0,
    "fator_deslocamento": 7.5,
    "visitas_recentes": 0,
    "fator_invisibilidade": 100.0,
    "xp_bruto": 750.0,
    "xp_final": 750
  },
  "regras_aplicadas": {
    "anti_farming_ativo": false,
    "gps_invalido": false
  },
  "motivo_bloqueio": null
}
```

#### Responses Bloqueadas

**Cooldown ativo:**
```json
{
  "xp_concedido": 0,
  "calculo": null,
  "regras_aplicadas": {
    "anti_farming_ativo": true,
    "gps_invalido": false
  },
  "motivo_bloqueio": "Cooldown ativo (30 dias)."
}
```

**GPS inválido:**
```json
{
  "xp_concedido": 0,
  "calculo": null,
  "regras_aplicadas": {
    "anti_farming_ativo": false,
    "gps_invalido": true
  },
  "motivo_bloqueio": "Deslocamento insuficiente detectado."
}
```

#### Possíveis Erros

| Status | Motivo |
|---|---|
| 400 | Dados inválidos |
| 401 | Token ausente ou inválido |
| 403 | Role não é TOURIST |
| 404 | POI não encontrado |

---

### DELETE /api/pois/{id}

#### Descrição

Remove um POI.

#### Controller

`PoiController`

#### Autenticação

✅ Obrigatória

#### Permissões

`ADMIN_USER` ou `ADMIN_ROOT`

#### Path Params

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `id` | Integer | ID do POI |

#### Response 204 (No Content)

---

### POST /api/pois/{poiId}/ratings

#### Descrição

Envia ou atualiza a **avaliação em estrelas** de um turista para um POI. Se o turista já avaliou aquele POI anteriormente, a nota é atualizada (upsert).

#### Controller

`PoiRatingController`

#### Autenticação

✅ Obrigatória

#### Permissões

Apenas `TOURIST`

#### Path Params

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `poiId` | Integer | ID do POI |

#### Request Body

| Campo | Tipo | Obrigatório | Validação |
|---|---|---|---|
| rating | Integer | Sim | `0` a `5` |

**Exemplo:**

```json
{
  "rating": 4
}
```

#### Response 200 (OK)

```json
{
  "id": 1,
  "rating": 4,
  "createdAt": "2026-05-24T14:00:00"
}
```

> ⚠️ Os campos `poi` e `user` não são retornados na resposta (`@JsonIgnore`).

#### Possíveis Erros

| Status | Motivo |
|---|---|
| 400 | `rating` fora do intervalo 0-5 |
| 401 | Token ausente ou inválido |
| 403 | Role não é TOURIST |
| 404 | POI ou usuário não encontrado |

---

### GET /api/pois/{poiId}/ratings

#### Descrição

Lista **todas as avaliações** de um POI específico.

#### Controller

`PoiRatingController`

#### Autenticação

✅ Obrigatória (qualquer role)

#### Permissões

`TOURIST`, `SHOPKEEPER`, `ADMIN_USER` ou `ADMIN_ROOT`

#### Path Params

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `poiId` | Integer | ID do POI |

#### Response 200 (OK)

```json
[
  {
    "id": 1,
    "rating": 4,
    "createdAt": "2026-05-24T14:00:00"
  },
  {
    "id": 2,
    "rating": 5,
    "createdAt": "2026-05-24T14:30:00"
  }
]
```

#### Possíveis Erros

| Status | Motivo |
|---|---|
| 401 | Token ausente ou inválido |
| 404 | POI não encontrado |

---

## 🛒 Produtos

### POST /api/products

#### Descrição

Cria um novo produto associado a um lojista e uma categoria.

#### Controller

`ProductController`

#### Autenticação

✅ Obrigatória

#### Permissões

`SHOPKEEPER`, `ADMIN_USER` ou `ADMIN_ROOT`

#### Request Body

| Campo | Tipo | Obrigatório | Validação |
|---|---|---|---|
| name | String | Sim | Não vazio |
| description | String | Não | — |
| price | Double | Não | `>= 0` |
| xpCost | Integer | Não | `>= 0` |
| stock | Integer | Não | `>= 0` (padrão: 0) |
| shopkeeperId | Integer | Sim | ID de lojista existente |
| categoryId | Integer | Sim | ID de categoria existente |
| poiId | Integer | Sim | ID de POI do tipo `STORE` existente |

**Exemplo:**

```json
{
  "name": "Artesanato Local",
  "description": "Peça feita à mão",
  "price": 49.90,
  "xpCost": 10,
  "stock": 100,
  "shopkeeperId": 2,
  "categoryId": 1,
  "poiId": 1
}
```

#### Response 200 (OK)

```json
{
  "id": 1,
  "name": "Artesanato Local",
  "description": "Peça feita à mão",
  "price": 49.90,
  "xpCost": 10,
  "stock": 100,
  "shopkeeper": {
    "id": 2,
    "name": "Maria Lojista",
    "companyName": "Maria's Comércio"
  },
  "category": {
    "id": 1,
    "name": "Alimentação"
  },
  "poi": {
    "id": 1,
    "name": "Loja de Artesanato",
    "type": "STORE"
  },
  "createdAt": "2026-05-24T15:00:00",
  "updatedAt": "2026-05-24T15:00:00"
}
```

---

### GET /api/products

#### Descrição

Lista **todos os produtos** cadastrados.

#### Controller

`ProductController`

#### Autenticação

✅ Obrigatória (qualquer role)

#### Permissões

`TOURIST`, `SHOPKEEPER`, `ADMIN_USER` ou `ADMIN_ROOT`

#### Response 200 (OK)

```json
[
  {
    "id": 1,
    "name": "Artesanato Local",
    "description": "Peça feita à mão",
    "price": 49.90,
    "xpCost": 10,
    "stock": 100,
    "shopkeeper": { "id": 2, "name": "Maria Lojista" },
    "category": { "id": 1, "name": "Alimentação" },
    "createdAt": "2026-05-24T15:00:00",
    "updatedAt": "2026-05-24T15:00:00"
  }
]
```

---

### GET /api/products/recent

#### Descrição

Lista os **50 produtos mais recentes** cadastrados (ordenados por `createdAt` decrescente).

#### Controller

`ProductController`

#### Autenticação

✅ Obrigatória (qualquer role)

#### Permissões

`TOURIST`, `SHOPKEEPER`, `ADMIN_USER` ou `ADMIN_ROOT`

#### Response 200 (OK)

```json
[
  {
    "id": 1,
    "name": "Artesanato Local",
    "description": "Peça feita à mão",
    "price": 49.90,
    "xpCost": 10,
    "stock": 100,
    "shopkeeper": { "id": 2, "name": "Maria Lojista" },
    "category": { "id": 1, "name": "Alimentação" },
    "createdAt": "2026-05-24T15:00:00",
    "updatedAt": "2026-05-24T15:00:00"
  }
]
```

---

### GET /api/products/{id}

#### Descrição

Retorna um produto por ID.

#### Controller

`ProductController`

#### Autenticação

✅ Obrigatória (qualquer role)

#### Permissões

`TOURIST`, `SHOPKEEPER`, `ADMIN_USER` ou `ADMIN_ROOT`

#### Path Params

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `id` | Integer | ID do produto |

#### Response 200 (OK)

```json
{
  "id": 1,
  "name": "Artesanato Local",
  "description": "Peça feita à mão",
  "price": 49.90,
  "xpCost": 10,
  "stock": 100,
  "shopkeeper": { "id": 2, "name": "Maria Lojista" },
  "category": { "id": 1, "name": "Alimentação" },
  "createdAt": "2026-05-24T15:00:00",
  "updatedAt": "2026-05-24T15:00:00"
}
```

---

### PUT /api/products/{id}

#### Descrição

Atualiza um produto.

#### Controller

`ProductController`

#### Autenticação

✅ Obrigatória

#### Permissões

`SHOPKEEPER`, `ADMIN_USER` ou `ADMIN_ROOT`

#### Path Params

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `id` | Integer | ID do produto |

#### Request Body

Todos os campos opcionais.

| Campo | Tipo | Validação |
|---|---|---|
| name | String | — |
| description | String | — |
| price | Double | — |
| xpCost | Integer | — |
| stock | Integer | `>= 0` |
| shopkeeperId | Integer | Deve existir |
| categoryId | Integer | Deve existir |
| poiId | Integer | Deve existir |

**Exemplo:**

```json
{
  "price": 39.90,
  "xpCost": 5,
  "stock": 200
}
```

---

### DELETE /api/products/{id}

#### Descrição

Remove um produto.

#### Controller

`ProductController`

#### Autenticação

✅ Obrigatória

#### Permissões

`SHOPKEEPER`, `ADMIN_USER` ou `ADMIN_ROOT`

#### Path Params

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `id` | Integer | ID do produto |

#### Response 204 (No Content)

---

## 🛒 Pedidos (Orders)

### POST /api/orders/checkout

#### Descrição

Cria um novo pedido (checkout) para um produto, gerando um PIX para pagamento. O turista deve resgatar o pedido anterior antes de comprar outro.

#### Controller

`OrderController`

#### Autenticação

✅ Obrigatória

#### Permissões

Apenas `TOURIST`

#### Headers

| Nome | Obrigatório | Descrição |
|---|---|---|
| Authorization | Sim | `Bearer <access_token>` |
| Content-Type | Sim | `application/json` |

#### Request Body

| Campo | Tipo | Obrigatório | Validação |
|---|---|---|---|
| productId | Integer | Sim | ID de produto existente |

**Exemplo:**

```json
{
  "productId": 1
}
```

#### Response 200 (OK)

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "productId": 1,
  "productName": "Artesanato Local",
  "shopkeeperId": 2,
  "shopkeeperName": "Maria's Comércio",
  "quantity": 1,
  "unitPrice": 49.90,
  "totalAmount": 49.90,
  "status": "AWAITING_PAYMENT",
  "transactionId": "abc123",
  "createdAt": "2026-06-22T10:00:00",
  "pix": "00020126580014BR.GOV.BCB.PIX0136...",
  "qrCodeBase64": "iVBORw0KGgo...",
  "pixExpiresAt": "2026-06-22T10:15:00",
  "pickupCode": null
}
```

> ⚠️ O `pickupCode` é gerado apenas quando o pagamento é confirmado.

#### Possíveis Erros

| Status | Motivo |
|---|---|
| 400 | Dados inválidos |
| 400 | Produto sem estoque disponível |
| 401 | Token ausente ou inválido |
| 403 | Role não é TOURIST |
| 404 | Produto ou turista não encontrado |
| 409 | Turista já possui um pedido ativo |

---

### GET /api/orders/shopkeeper

#### Descrição

Lista todos os pedidos **PAID** do lojista autenticado, contendo o `pickupCode` para retirada.

#### Controller

`OrderController`

#### Autenticação

✅ Obrigatória

#### Permissões

Apenas `SHOPKEEPER`

#### Headers

| Nome | Obrigatório | Descrição |
|---|---|---|
| Authorization | Sim | `Bearer <access_token>` |

#### Response 200 (OK)

```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "productId": 1,
    "productName": "Artesanato Local",
    "shopkeeperId": 2,
    "shopkeeperName": "Maria's Comércio",
    "quantity": 1,
    "unitPrice": 49.90,
    "totalAmount": 49.90,
    "status": "PAID",
    "transactionId": "abc123",
    "createdAt": "2026-06-22T10:00:00",
    "pix": "00020126580014BR.GOV.BCB.PIX0136...",
    "qrCodeBase64": "iVBORw0KGgo...",
    "pixExpiresAt": "2026-06-22T10:15:00",
    "pickupCode": "A7X9K2"
  }
]
```

#### Possíveis Erros

| Status | Motivo |
|---|---|
| 401 | Token ausente ou inválido |
| 403 | Role não é SHOPKEEPER |
| 404 | Lojista não encontrado |

---

### GET /api/orders/my-current

#### Descrição

Retorna o **último pedido PAID** do turista autenticado.

#### Controller

`OrderController`

#### Autenticação

✅ Obrigatória

#### Permissões

Apenas `TOURIST`

#### Headers

| Nome | Obrigatório | Descrição |
|---|---|---|
| Authorization | Sim | `Bearer <access_token>` |

#### Response 200 (OK)

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "productId": 1,
  "productName": "Artesanato Local",
  "shopkeeperId": 2,
  "shopkeeperName": "Maria's Comércio",
  "quantity": 1,
  "unitPrice": 49.90,
  "totalAmount": 49.90,
  "status": "PAID",
  "transactionId": "abc123",
  "createdAt": "2026-06-22T10:00:00",
  "pix": "00020126580014BR.GOV.BCB.PIX0136...",
  "qrCodeBase64": "iVBORw0KGgo...",
  "pixExpiresAt": "2026-06-22T10:15:00",
  "pickupCode": "A7X9K2"
}
```

#### Possíveis Erros

| Status | Motivo |
|---|---|
| 401 | Token ausente ou inválido |
| 403 | Role não é TOURIST |
| 404 | Nenhum pedido pago encontrado ou turista não encontrado |

---

### GET /api/orders/shopkeeper/history

#### Descrição

Lista **todos os pedidos** do lojista autenticado, independentemente do status, ordenados do mais recente para o mais antigo.

#### Controller

`OrderController`

#### Autenticação

✅ Obrigatória

#### Permissões

Apenas `SHOPKEEPER`

#### Headers

| Nome | Obrigatório | Descrição |
|---|---|---|
| Authorization | Sim | `Bearer <access_token>` |

#### Response 200 (OK)

```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "productId": 1,
    "productName": "Artesanato Local",
    "shopkeeperId": 2,
    "shopkeeperName": "Maria's Comércio",
    "quantity": 1,
    "unitPrice": 49.90,
    "totalAmount": 49.90,
    "status": "PAID",
    "transactionId": "abc123",
    "createdAt": "2026-06-22T10:00:00",
    "pix": "00020126580014BR.GOV.BCB.PIX0136...",
    "qrCodeBase64": "iVBORw0KGgo...",
    "pixExpiresAt": "2026-06-22T10:15:00",
    "pickupCode": "A7X9K2"
  }
]
```

#### Possíveis Erros

| Status | Motivo |
|---|---|
| 401 | Token ausente ou inválido |
| 403 | Role não é SHOPKEEPER |
| 404 | Lojista não encontrado |

---

### GET /api/orders/my-history

#### Descrição

Lista **todos os pedidos** do turista autenticado, independentemente do status, ordenados do mais recente para o mais antigo.

#### Controller

`OrderController`

#### Autenticação

✅ Obrigatória

#### Permissões

Apenas `TOURIST`

#### Headers

| Nome | Obrigatório | Descrição |
|---|---|---|
| Authorization | Sim | `Bearer <access_token>` |

#### Response 200 (OK)

```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "productId": 1,
    "productName": "Artesanato Local",
    "shopkeeperId": 2,
    "shopkeeperName": "Maria's Comércio",
    "quantity": 1,
    "unitPrice": 49.90,
    "totalAmount": 49.90,
    "status": "AWAITING_PAYMENT",
    "transactionId": "abc123",
    "createdAt": "2026-06-22T10:00:00",
    "pix": "00020126580014BR.GOV.BCB.PIX0136...",
    "qrCodeBase64": "iVBORw0KGgo...",
    "pixExpiresAt": "2026-06-22T10:15:00",
    "pickupCode": null
  }
]
```

#### Possíveis Erros

| Status | Motivo |
|---|---|
| 401 | Token ausente ou inválido |
| 403 | Role não é TOURIST |
| 404 | Turista não encontrado |

---

## 🔌 WebSocket (STOMP)

### ws://host/ws

#### Descrição

Endpoint **WebSocket com STOMP** para notificações em tempo real. Utilizado para receber atualizações do status do pedido sem necessidade de polling.

#### Configuração

| Parâmetro | Valor |
|---|---|
| Endpoint de conexão | `ws://localhost:8080/ws` |
| Protocolo | STOMP sobre WebSocket nativo |
| Broker de tópicos | `/topic/**` |

#### Autenticação

✅ Obrigatória — enviar JWT no header `Authorization: Bearer <token>` do frame STOMP `CONNECT`.

**Exemplo de frame CONNECT:**

```stomp
CONNECT
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
accept-version:1.1,1.0
host:localhost:8080

```

#### Tópicos

| Tópico | Descrição | Payload |
|---|---|---|---|
| `/topic/orders/{orderId}` | Notificações de alteração de status do pedido | `OrderStatusDTO` |
| `/topic/routes/tracking/{userId}` | Tracking ao vivo de localização do turista | `PushMessageDTO(action, LocationDTO)` |
| `/user/{userId}/queue/route` | Notificações de rota (destino atualizado, rota encerrada) | `PushMessageDTO(action, data)` |
| `/user/{userId}/queue/poi` | Notificações de check-in e resultado de XP | `PushMessageDTO(action, CheckinResponseDTO)` |

**Payload (`OrderStatusDTO`):**

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "status": "PAID",
  "pickupCode": "A7X9K2"
}
```

**Payload (`PushMessageDTO - tracking`):**
```json
{
  "action": "location-update",
  "data": {
    "latitude": -23.5505,
    "longitude": -46.6333
  }
}
```

**Payload (`PushMessageDTO - route-ended`):**
```json
{
  "action": "route-ended",
  "data": "Session closed"
}
```

#### Fluxo

1. **Turista** faz `POST /api/orders/checkout` e recebe o `orderId`
2. **Turista** conecta ao WebSocket e envia JWT no header `Authorization` do CONNECT
3. **Turista** inscreve em `/topic/orders/{orderId}`
4. **Pagamento confirmado** (webhook ou reconciliação) → servidor publica no tópico
5. **Turista** recebe a mensagem em tempo real com o novo status

#### Possíveis Erros

| Situação | Comportamento |
|---|---|
| Token ausente | Conexão rejeitada com erro `IllegalArgumentException` |
| Token inválido/expirado | Conexão rejeitada com erro `IllegalArgumentException` |

---

### 📊 Resumo de Endpoints

| Módulo | Endpoints | Públicos | Autenticados | Admin | Role Específica |
|---|---|---|---|---|---|
| Auth | 5 | 5 | — | — | — |
| Users | 2 | — | — | 2 | ADMIN_USER/ROOT |
| Admin | 6 | — | — | 6 | ADMIN_ROOT |
| Tourists | 4 | — | — | 4 | ADMIN_USER/ROOT |
| Shopkeepers | 4 | — | — | 4 | ADMIN_USER/ROOT |
| Cities | 5 | — | — | 5 | ADMIN_USER/ROOT |
| Categories | 5 | — | — | 5 | ADMIN_USER/ROOT |
| POIs | 6 | — | 3 | 3 | TOURIST (checkin) |
| POI Ratings | 2 | — | 1 | — | TOURIST |
| Direction | 1 | — | — | — | TOURIST |
| Route | 4 | — | — | — | TOURIST |
| Products | 6 | — | 3 | — | SHOPKEEPER |
| Orders | 5 | — | — | — | TOURIST / SHOPKEEPER |
| WebSocket (STOMP) | 1 | — | — | — | TOURIST / SHOPKEEPER |
| **Total** | **57** | **5** | **10** | **29** | **14** |
