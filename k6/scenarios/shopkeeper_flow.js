import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, jsonHeaders } from '../config.js';

let vuShopkeeperToken = null;

export function shopkeeperFlow() {
    const vuId = __VU;
    const clientIp = `10.10.${Math.floor(vuId / 250) + 1}.${(vuId % 250) + 1}`;
    const headers = (token = null) => jsonHeaders(token, clientIp);

    if (!vuShopkeeperToken) {
        const sellerNum = ((vuId - 1) % 500) + 1;
        const email = __ENV.SHOPKEEPER_EMAIL || `seller${sellerNum}@passaaqui.com`;
        const password = __ENV.SHOPKEEPER_PASSWORD || 'TestPassword@123';

        // 1. Login do comerciante / lojista
        const loginPayload = JSON.stringify({
            email: email,
            password: password
        });

        const loginRes = http.post(`${BASE_URL}/api/auth/login`, loginPayload, headers());
        if (loginRes.status === 200) {
            try {
                const body = JSON.parse(loginRes.body);
                vuShopkeeperToken = body.access_token;
            } catch (e) {
                // parsing fallback
            }
        }

        check(loginRes, {
            'login do lojista realizado (200/429)': (r) => r.status === 200 || r.status === 429,
        });
    }

    sleep(1);

    if (vuShopkeeperToken) {
        // 2. Visualizacao do Dashboard do lojista
        const dashRes = http.get(`${BASE_URL}/api/dashboard`, headers(vuShopkeeperToken));
        check(dashRes, {
            'dashboard do lojista carregado (200/429)': (r) => r.status === 200 || r.status === 429,
        });

        sleep(1);

        // 3. Consulta de produtos do lojista
        const prodRes = http.get(`${BASE_URL}/api/products/shopkeeper`, headers(vuShopkeeperToken));
        check(prodRes, {
            'produtos do lojista listados (200/429)': (r) => r.status === 200 || r.status === 429,
        });

        // 4. Metricas do catalogo
        const metricsRes = http.get(`${BASE_URL}/api/products/shopkeeper/metrics`, headers(vuShopkeeperToken));
        check(metricsRes, {
            'metricas do catalogo carregadas (200/429)': (r) => r.status === 200 || r.status === 429,
        });

        sleep(1);

        // 5. Consulta de pedidos recebidos pelo lojista
        const ordersRes = http.get(`${BASE_URL}/api/orders/shopkeeper`, headers(vuShopkeeperToken));
        check(ordersRes, {
            'pedidos do lojista listados (200/429)': (r) => r.status === 200 || r.status === 429,
        });

        // 6. Historico geral de pedidos
        const orderHistRes = http.get(`${BASE_URL}/api/orders/shopkeeper/history`, headers(vuShopkeeperToken));
        check(orderHistRes, {
            'historico de pedidos do lojista listado (200/429)': (r) => r.status === 200 || r.status === 429,
        });
    } else {
        // Fallback de consulta ao catalogo quando sem usuario pre-semeado
        const fallbackRes = http.get(`${BASE_URL}/api/products`, headers());
        check(fallbackRes, {
            'fallback de consulta de produtos (200)': (r) => r.status === 200,
        });
    }

    sleep(1);
}
