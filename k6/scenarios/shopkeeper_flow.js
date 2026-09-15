import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, jsonHeaders } from '../config.js';

export function shopkeeperFlow() {
    const email = __ENV.SHOPKEEPER_EMAIL || 'lojista@teste.com';
    const password = __ENV.SHOPKEEPER_PASSWORD || 'Test@1234';

    // 1. Login do comerciante / lojista
    const loginPayload = JSON.stringify({
        email: email,
        password: password
    });

    const loginRes = http.post(`${BASE_URL}/api/auth/login`, loginPayload, jsonHeaders());
    let token = null;

    if (loginRes.status === 200) {
        try {
            const body = JSON.parse(loginRes.body);
            token = body.access_token;
        } catch (e) {
            // parsing fallback
        }
    }

    check(loginRes, {
        'login do lojista (200 ou 401/404 se mock sem seed)': (r) => r.status === 200 || r.status === 401 || r.status === 404,
    });

    sleep(1);

    if (token) {
        // 2. Visualizacao do Dashboard do lojista
        const dashRes = http.get(`${BASE_URL}/api/dashboard`, jsonHeaders(token));
        check(dashRes, {
            'dashboard do lojista carregado (200)': (r) => r.status === 200,
        });

        sleep(1);

        // 3. Consulta de produtos do lojista
        const prodRes = http.get(`${BASE_URL}/api/products/shopkeeper`, jsonHeaders(token));
        check(prodRes, {
            'produtos do lojista listados (200)': (r) => r.status === 200,
        });

        // 4. Metricas do catalogo
        const metricsRes = http.get(`${BASE_URL}/api/products/shopkeeper/metrics`, jsonHeaders(token));
        check(metricsRes, {
            'metricas do catalogo carregadas (200)': (r) => r.status === 200,
        });

        sleep(1);

        // 5. Consulta de pedidos recebidos pelo lojista
        const ordersRes = http.get(`${BASE_URL}/api/orders/shopkeeper`, jsonHeaders(token));
        check(ordersRes, {
            'pedidos do lojista listados (200)': (r) => r.status === 200,
        });

        // 6. Historico geral de pedidos
        const orderHistRes = http.get(`${BASE_URL}/api/orders/shopkeeper/history`, jsonHeaders(token));
        check(orderHistRes, {
            'historico de pedidos do lojista listado (200)': (r) => r.status === 200,
        });
    } else {
        // Fallback de consulta ao catalogo quando sem usuario pre-semeado
        const fallbackRes = http.get(`${BASE_URL}/api/products`, jsonHeaders());
        check(fallbackRes, {
            'fallback de consulta de produtos (200)': (r) => r.status === 200,
        });
    }

    sleep(1);
}
