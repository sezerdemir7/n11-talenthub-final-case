import api from './api';

const PAY_BASE = '/v1/payments';

/**
 * Ödeme — gateway JWT Authorization ekler; X-User-Id gönderilmez.
 * Body: { orderId, userId, amount, card: { cardHolderName, cardNumber, expireMonth, expireYear, cvc } }
 */
export const paymentService = {
  pay: (body) => api.post(`${PAY_BASE}/pay`, body),
};
