import api from './api';

const ORDER_BASE = '/v1/orders';

export const orderService = {
  /**
   * Sipariş oluşturur (ödeme yok). CheckoutRequest: { addressId }.
   * Kullanıcı kimliği Authorization (JWT) ile; X-User-Id gönderilmez.
   */
  checkout: (request) => {
    const { addressId } = request || {};
    if (addressId == null || addressId === '') {
      return Promise.reject(new Error('Teslimat adresi (addressId) zorunludur.'));
    }
    const id = Number(addressId);
    if (Number.isNaN(id)) {
      return Promise.reject(new Error('Geçersiz addressId.'));
    }
    return api.post(`${ORDER_BASE}/checkout`, { addressId: id });
  },

  getById: (orderId) => api.get(`${ORDER_BASE}/${orderId}`),

  // GET /my-orders — kullanıcı JWT'den belirlenir, userId gerekmez
  getMyOrders: (params = {}) => {
    const { page = 0, size = 10 } = params;
    return api.get(`${ORDER_BASE}/my-orders`, { params: { page, size } });
  },

  getOrderItems: (orderId, params = {}) => {
    const { page = 0, size = 10 } = params;
    return api.get(`${ORDER_BASE}/${orderId}/items`, { params: { page, size } });
  },

  // DELETE /{orderId} — kullanıcı JWT'den belirlenir, X-User-Id gerekmez
  cancelOrder: (orderId) => api.delete(`${ORDER_BASE}/${orderId}`),
};
