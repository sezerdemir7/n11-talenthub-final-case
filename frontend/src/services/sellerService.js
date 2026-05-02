import api from './api';

const SELLER_BASE = '/v1/sellers';

export const sellerService = {
  // Seller: JWT-based
  getMyProfile: () => api.get(`${SELLER_BASE}/me`),
  updateMyProfile: (body) => api.put(`${SELLER_BASE}/me`, body),

  // Admin: all sellers with optional filters + pagination
  getAllSellers: (params = {}) => {
    const { storeName, status, verified, page = 0, size = 10 } = params;
    return api.get(SELLER_BASE, {
      params: {
        storeName: storeName || undefined,
        status: status || undefined,
        verified: verified != null ? verified : undefined,
        page,
        size,
      },
    });
  },

  // Admin: pending applications
  getPendingApplications: () => api.get(`${SELLER_BASE}/applications/pending`),

  // Admin: update seller status
  updateApplicationStatus: (userId, status) =>
    api.patch(`${SELLER_BASE}/${userId}/status`, { status }),
};
