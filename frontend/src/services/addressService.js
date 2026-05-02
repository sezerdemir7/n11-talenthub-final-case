import api from './api';

const BASE = '/v1/users/me/addresses';

export const addressService = {
  list: () => api.get(BASE),

  create: (body) => api.post(BASE, body),

  update: (addressId, body) => api.put(`${BASE}/${addressId}`, body),

  setDefault: (addressId) => api.patch(`${BASE}/${addressId}/default`, {}),

  remove: (addressId) => api.delete(`${BASE}/${addressId}`),
};
