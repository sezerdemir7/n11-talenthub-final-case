import api from './api';

const BASE = '/v1/users/me/addresses';

const withUser = (userId) => ({ headers: { 'X-User-Id': String(userId) } });

export const addressService = {
  list: (userId) => api.get(BASE, withUser(userId)),

  create: (userId, body) => api.post(BASE, body, withUser(userId)),

  update: (userId, addressId, body) =>
    api.put(`${BASE}/${addressId}`, body, withUser(userId)),

  setDefault: (userId, addressId) =>
    api.patch(`${BASE}/${addressId}/default`, {}, withUser(userId)),

  remove: (userId, addressId) => api.delete(`${BASE}/${addressId}`, withUser(userId)),
};
