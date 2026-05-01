import api from './api';

const CART_BASE = '/v1/carts';

/**
 * Cart API — tüm isteklerde X-User-Id header zorunlu.
 * POST body: AddToCartRequest { productId, quantity }
 */
export const cartService = {
  getCart: (userId) =>
    api.get(CART_BASE, {
      headers: { 'X-User-Id': String(userId) },
    }),

  addItem: (userId, body) =>
    api.post(CART_BASE, body, {
      headers: { 'X-User-Id': String(userId) },
    }),

  clearCart: (userId) =>
    api.delete(CART_BASE, {
      headers: { 'X-User-Id': String(userId) },
    }),
};
