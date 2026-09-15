export const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export function jsonHeaders(token = null) {
    const headers = {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
    };
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }
    return { headers };
}

export function generateRandomCpf() {
    const digits = Array.from({ length: 9 }, () => Math.floor(Math.random() * 9));
    
    let sum1 = 0;
    for (let i = 0; i < 9; i++) {
        sum1 += digits[i] * (10 - i);
    }
    const d1 = (sum1 * 10) % 11 % 10;
    digits.push(d1);

    let sum2 = 0;
    for (let i = 0; i < 10; i++) {
        sum2 += digits[i] * (11 - i);
    }
    const d2 = (sum2 * 10) % 11 % 10;
    digits.push(d2);

    return digits.join('');
}
