import api from './api';

const CART_BASE = '/v1/carts';

// Cart API — kullanıcı JWT'den belirlenir, X-User-Id header gerekmez
export const cartService = {
  getCart: () => api.get(CART_BASE),

  addItem: (body) => api.post(CART_BASE, body),

  clearCart: () => api.delete(CART_BASE),
};
