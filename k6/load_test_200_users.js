import { touristFlow } from './scenarios/tourist_flow.js';
import { shopkeeperFlow } from './scenarios/shopkeeper_flow.js';
import { Counter } from 'k6/metrics';

export const rateLimitCounter = new Counter('rate_limited_429_total');

const VUS = parseInt(__ENV.VUS_PER_SCENARIO || '500');

export const options = {
    scenarios: {
        // Cenario 1: 500 Turistas simultaneos navegando pelo app
        tourists_scenario: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '30s', target: VUS }, // Rampa de subida ate 500 VUs
                { duration: '1m', target: VUS },  // Carga constante de 500 VUs por 1 minuto
                { duration: '15s', target: 0 },   // Rampa de descida
            ],
            exec: 'runTourist',
        },
        // Cenario 2: 500 Comerciantes/Lojistas simultaneos consultando metricas e pedidos
        shopkeepers_scenario: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '30s', target: VUS }, // Rampa de subida ate 500 VUs
                { duration: '1m', target: VUS },  // Carga constante de 500 VUs por 1 minuto
                { duration: '15s', target: 0 },   // Rampa de descida
            ],
            exec: 'runShopkeeper',
        },
    },
    thresholds: {
        // 95% das requisicoes devem responder em menos de 3.0s e 99% em menos de 6.0s sob 1000 VUs
        http_req_duration: ['p(95)<3000', 'p(99)<6000'],
        // Taxa de falhas toleravel (permite checagens de controle / rate limiters)
        http_req_failed: ['rate<0.15'],
    },
};

export default function () {
    if (__VU % 2 === 0) {
        touristFlow();
    } else {
        shopkeeperFlow();
    }
}

export function runTourist() {
    touristFlow();
}

export function runShopkeeper() {
    shopkeeperFlow();
}
