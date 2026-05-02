import api from './api';

const BASE = '/v1/notifications';

export const notificationService = {
  getAll: (page = 0, size = 20) =>
    api.get(BASE, {
      params: { page, size },
    }),

  getUnread: () => api.get(`${BASE}/unread`),

  markAllAsRead: () => api.patch(`${BASE}/read-all`),

  markAsRead: (id) => api.patch(`${BASE}/${id}/read`),
};
