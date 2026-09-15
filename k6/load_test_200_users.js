import { touristFlow } from './scenarios/tourist_flow.js';
import { shopkeeperFlow } from './scenarios/shopkeeper_flow.js';
import { Counter } from 'k6/metrics';

export const rateLimitCounter = new Counter('rate_limited_429_total');

export const options = {
    scenarios: {
        // Cenario 1: 100 Turistas simultaneos navegando pelo app
        tourists_scenario: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '30s', target: 100 }, // Rampa de subida ate 100 VUs
                { duration: '1m', target: 100 },  // Carga constante de 100 VUs por 1 minuto
                { duration: '15s', target: 0 },   // Rampa de descida
            ],
            exec: 'runTourist',
        },
        // Cenario 2: 100 Comerciantes/Lojistas simultaneos consultando metricas e pedidos
        shopkeepers_scenario: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '30s', target: 100 }, // Rampa de subida ate 100 VUs
                { duration: '1m', target: 100 },  // Carga constante de 100 VUs por 1 minuto
                { duration: '15s', target: 0 },   // Rampa de descida
            ],
            exec: 'runShopkeeper',
        },
    },
    thresholds: {
        // 95% das requisicoes devem responder em menos de 2.5s e 99% em menos de 5s
        http_req_duration: ['p(95)<2500', 'p(99)<5000'],
        // Taxa de falhas toleravel (permite checagens de controle / dados mock)
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
