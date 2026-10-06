import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, jsonHeaders, generateRandomCpf } from '../config.js';

let vuToken = null;

export function touristFlow() {
    const vuId = __VU;
    const clientIp = `192.168.${Math.floor(vuId / 250) + 1}.${(vuId % 250) + 1}`;
    const headers = (token = null) => jsonHeaders(token, clientIp);

    if (!vuToken) {
        const email = `tourist_vu_${vuId}_${Date.now()}@loadtest.com`;
        const password = 'TestPassword@123';

        // 1. Cadastro de novo turista
        const registerPayload = JSON.stringify({
            email: email,
            name: `Tourist VU ${vuId}`,
            password: password,
            confirm_password: password,
            documentId: generateRandomCpf()
        });

        const regRes = http.post(`${BASE_URL}/api/auth/register/tourist`, registerPayload, headers());
        check(regRes, {
            'turista cadastrado com sucesso (201/409/429)': (r) => r.status === 201 || r.status === 409 || r.status === 429,
        });

        // 2. Login do turista
        const loginPayload = JSON.stringify({
            email: email,
            password: password
        });

        const loginRes = http.post(`${BASE_URL}/api/auth/login`, loginPayload, headers());
        if (loginRes.status === 200) {
            try {
                const body = JSON.parse(loginRes.body);
                vuToken = body.access_token;
            } catch (e) {
                // parsing fallback
            }
        }

        check(loginRes, {
            'login do turista realizado (200/429)': (r) => r.status === 200 || r.status === 429,
        });
    }

    sleep(1);

    // 3. Consulta de POIs nas proximidades
    const poiRes = http.get(`${BASE_URL}/api/pois?latitude=-8.0578&longitude=-34.8829&mode=foot-walking`, headers(vuToken));
    check(poiRes, {
        'pois listados (200)': (r) => r.status === 200,
    });

    sleep(1);

    // 4. Listagem de produtos no catalogo
    const prodRes = http.get(`${BASE_URL}/api/products`, headers(vuToken));
    check(prodRes, {
        'produtos listados (200)': (r) => r.status === 200,
    });

    // 5. Consulta de produtos recentes
    const recentProdRes = http.get(`${BASE_URL}/api/products/recent`, headers(vuToken));
    check(recentProdRes, {
        'produtos recentes listados (200)': (r) => r.status === 200,
    });

    sleep(1);

    // 6. Historico de pedidos do turista
    if (vuToken) {
        const histRes = http.get(`${BASE_URL}/api/orders/my-history`, headers(vuToken));
        check(histRes, {
            'historico de pedidos OK (200/429)': (r) => r.status === 200 || r.status === 429,
        });

        // 7. Pedido corrente do turista
        const currOrderRes = http.get(`${BASE_URL}/api/orders/my-current`, headers(vuToken));
        check(currOrderRes, {
            'pedido atual checado (200, 204 ou 429)': (r) => r.status === 200 || r.status === 204 || r.status === 429,
        });
    }

    sleep(1);
}
