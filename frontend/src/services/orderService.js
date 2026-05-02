import api from './api';

const ORDER_BASE = '/v1/orders';

export const orderService = {
  checkout: (request) => {
    const { addressId, selectedProductIds } = request || {};
    if (addressId == null || addressId === '') {
      return Promise.reject(new Error('Teslimat adresi (addressId) zorunludur.'));
    }
    const id = Number(addressId);
    if (Number.isNaN(id)) {
      return Promise.reject(new Error('Gecersiz addressId.'));
    }
    const productIds = Array.isArray(selectedProductIds)
      ? selectedProductIds.map(Number).filter((productId) => !Number.isNaN(productId))
      : [];
    if (productIds.length === 0) {
      return Promise.reject(new Error('Siparis icin en az bir urun secmelisiniz.'));
    }
    return api.post(`${ORDER_BASE}/checkout`, { addressId: id, selectedProductIds: productIds });
  },

  getById: (orderId) => api.get(`${ORDER_BASE}/${orderId}`),

  getMyOrders: (params = {}) => {
    const { page = 0, size = 10 } = params;
    return api.get(`${ORDER_BASE}/my-orders`, { params: { page, size } });
  },

  getOrderItems: (orderId, params = {}) => {
    const { page = 0, size = 10 } = params;
    return api.get(`${ORDER_BASE}/${orderId}/items`, { params: { page, size } });
  },

  cancelOrder: (orderId) => api.delete(`${ORDER_BASE}/${orderId}`),
};
