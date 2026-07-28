$BASE = "http://localhost:8080"
$LOG = "D:\passaaqui-backend\logs.txt"
$TMP = "D:\passaaqui-backend\temp_body.json"

function Log {
    param($msg)
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    "$timestamp $msg" | Out-File -FilePath $LOG -Append -Encoding UTF8
    Write-Host $msg
}

function Curl-Json {
    param($Method, $Path, [string]$Body, $Token)
    $url = "$BASE$Path"
    $curlArgs = @("-s", "-S", "-X", $Method, "-w", "`n%{http_code}")
    $curlArgs += "-H", "Content-Type: application/json"
    if ($Token) { $curlArgs += "-H", "Authorization: Bearer $Token" }
    if ($Body) {
        $Body | Out-File -FilePath $TMP -Encoding UTF8 -Force
        $curlArgs += "-d", "@$TMP"
    }
    $output = & "curl.exe" @curlArgs $url 2>&1
    if ($output -is [array]) {
        $code = $output[-1]
        $bodyLines = $output[0..($output.Length-2)]
    } else {
        $parts = $output -split "`n"
        $code = $parts[-1]
        $bodyLines = $parts[0..($parts.Length-2)]
    }
    return @{ Status = $code; Body = ($bodyLines -join "`n") }
}

function Curl-Multipart {
    param($Method, $Path, [string]$JsonData, [string]$ImagePath, $Token)
    $url = "$BASE$Path"
    $curlArgs = @("-s", "-S", "-X", $Method, "-w", "`n%{http_code}")
    if ($Token) { $curlArgs += "-H", "Authorization: Bearer $Token" }
    $JsonData | Out-File -FilePath $TMP -Encoding UTF8 -Force
    $curlArgs += "-F", "data=@$TMP;type=application/json"
    if ($ImagePath) { $curlArgs += "-F", "image=@$ImagePath" }
    $output = & "curl.exe" @curlArgs $url 2>&1
    if ($output -is [array]) {
        $code = $output[-1]
        $bodyLines = $output[0..($output.Length-2)]
    } else {
        $parts = $output -split "`n"
        $code = $parts[-1]
        $bodyLines = $parts[0..($parts.Length-2)]
    }
    return @{ Status = $code; Body = ($bodyLines -join "`n") }
}

Log "============================================="
Log "INICIANDO TESTE DE ROTAS"
Log "============================================="

# ===== 1. ROTAS PUBLICAS =====
Log ""
Log "--- 1. ROTAS PUBLICAS ---"

$publicEndpoints = @(
    "GET:/api/categories",
    "GET:/api/categories/1",
    "GET:/api/products",
    "GET:/api/products/recent",
    "GET:/api/products/1",
    "GET:/api/pois",
    "GET:/api/pois/1",
    "GET:/api/products/1/ratings",
    "GET:/api/pois/1/ratings",
    "GET:/v3/api-docs",
    "GET:/swagger-ui.html"
)

foreach ($ep in $publicEndpoints) {
    $method, $path = $ep -split ":", 2
    $r = Curl-Json -Method $method -Path $path
    $bodyPreview = if ($r.Body.Length -gt 120) { $r.Body.Substring(0,120) + "..." } else { $r.Body }
    Log "$method $path -> $($r.Status) | $bodyPreview"
}

# ===== 2. REGISTRO E LOGIN TOURIST =====
Log ""
Log "--- 2. REGISTRO E LOGIN TOURIST ---"

$email = "test-$(Get-Random -Max 99999)@test.com"
$password = "Test@1234"
$name = "Test User"

Log "POST /api/auth/register/tourist as $email"
$registerBody = '{ "email": "' + $email + '", "name": "' + $name + '", "password": "' + $password + '", "confirm_password": "' + $password + '", "documentId": "52998224725" }'
$r = Curl-Json -Method POST -Path "/api/auth/register/tourist" -Body $registerBody
Log "  Status: $($r.Status) | Body: $($r.Body)"

if ($r.Status -eq 201) {
    Log ""
    Log "POST /api/auth/login as $email"
    $loginBody = '{ "email": "' + $email + '", "password": "' + $password + '" }'
    $r2 = Curl-Json -Method POST -Path "/api/auth/login" -Body $loginBody
    Log "  Status: $($r2.Status) | Body: $($r2.Body)"

    if ($r2.Status -eq 200) {
        $token = ($r2.Body | ConvertFrom-Json).access_token
        Log "  Token obtido: $($token.Substring(0,50))..."

        # ===== 3. ROTAS PROTEGIDAS - TOURIST =====
        Log ""
        Log "--- 3. ROTAS PROTEGIDAS TOURIST ---"

        $touristEndpoints = @(
            "GET:/api/tourists/me",
            "GET:/api/products/1/details",
            "GET:/api/orders/my-history",
            "GET:/api/orders/my-current",
            "GET:/api/orders/00000000-0000-0000-0000-000000000000"
        )

        foreach ($ep in $touristEndpoints) {
            $m, $p = $ep -split ":", 2
            $r3 = Curl-Json -Method $m -Path $p -Token $token
            $bodyPreview = if ($r3.Body.Length -gt 120) { $r3.Body.Substring(0,120) + "..." } else { $r3.Body }
            Log "$m $p -> $($r3.Status) | $bodyPreview"
        }

        Log "POST /api/city/locate"
        $r3 = Curl-Json -Method POST -Path "/api/city/locate" -Token $token -Body '{ "latitude": -23.5505, "longitude": -46.6333 }'
        Log "  Status: $($r3.Status) | $($r3.Body)"

        Log "POST /api/pois/1/checkin"
        $r3 = Curl-Json -Method POST -Path "/api/pois/1/checkin" -Token $token -Body '{ "poiId": 1 }'
        Log "  Status: $($r3.Status) | $($r3.Body)"

        Log "POST /api/orders/checkout"
        $r3 = Curl-Json -Method POST -Path "/api/orders/checkout" -Token $token -Body '{ "productId": 1, "xpToUse": 0 }'
        Log "  Status: $($r3.Status) | $($r3.Body)"

        Log "GET /api/auth/logout"
        $r3 = Curl-Json -Method GET -Path "/api/auth/logout" -Token $token
        Log "  Status: $($r3.Status)"

        Log "GET /api/auth/refresh"
        $r3 = Curl-Json -Method GET -Path "/api/auth/refresh" -Token $token
        Log "  Status: $($r3.Status)"
    }
} else {
    Log ""
    Log "Tentando login com dados existentes do seed..."
    $rOld = Curl-Json -Method POST -Path "/api/auth/login" -Body '{ "email": "shop@test.com", "password": "Test@1234" }'
    Log "  Login shop@test.com: $($rOld.Status) | $($rOld.Body)"
}

# ===== 4. REGISTRO SHOPKEEPER (multipart) =====
Log ""
Log "--- 4. REGISTRO SHOPKEEPER (multipart) ---"
$shopEmail = "shop-$(Get-Random -Max 99999)@test.com"
$shopData = '{ "email": "' + $shopEmail + '", "name": "Shop Owner", "password": "Test@1234", "confirm_password": "Test@1234", "documentId": "11222333000181", "companyName": "My Company", "description": "Test", "categoryId": 1, "poiName": "Minha Loja", "cityId": 1 }'
$r4 = Curl-Multipart -Method POST -Path "/api/auth/register/shopkeeper" -JsonData $shopData
Log "  Status: $($r4.Status) | $($r4.Body)"

# ===== 4. ROTAS SEM AUTENTICACAO (401 esperado) =====
Log ""
Log "--- 4. ROTAS SEM TOKEN (401 esperado) ---"

$noAuthEndpoints = @(
    "GET:/api/tourists/me",
    "GET:/api/tourists",
    "GET:/api/shopkeepers/me",
    "GET:/api/admin",
    "GET:/api/orders/shopkeeper",
    "GET:/api/dashboard",
    "POST:/api/products",
    "POST:/api/pois",
    "GET:/api/users",
    "POST:/api/city/create"
)

foreach ($ep in $noAuthEndpoints) {
    $method, $path = $ep -split ":", 2
    $r = Curl-Json -Method $method -Path $path
    Log "  $method $path -> $($r.Status) (esperado 401)"
}

# ===== 5. DOCKER LOGS =====
Log ""
Log "--- 5. LOGS DO DOCKER ---"
docker compose logs backend 2>&1 | Out-File -FilePath $LOG -Append -Encoding UTF8
Log "Logs do Docker anexados ao final do arquivo."

Log ""
Log "============================================="
Log "TESTE DE ROTAS FINALIZADO - LOGS EM logs.txt"
Log "============================================="
