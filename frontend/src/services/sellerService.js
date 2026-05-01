import api from './api';

const SELLER_BASE = '/v1/sellers';

export const sellerService = {
  getMyProfile: (userId) =>
    api.get(`${SELLER_BASE}/me`, {
      headers: { 'X-User-Id': String(userId) },
    }),

  updateMyProfile: (userId, body) =>
    api.put(`${SELLER_BASE}/me`, body, {
      headers: { 'X-User-Id': String(userId) },
    }),

  /** Admin: bekleyen satıcı başvuruları */
  getPendingApplications: () => api.get(`${SELLER_BASE}/applications/pending`),

  /** Admin: tüm satıcı profilleri — backend: GET /api/v1/sellers/all (yoksa path'i backend ile eşleştirin) */
  getAllSellerProfiles: () => api.get(`${SELLER_BASE}/all`),

  /** Admin: başvuru / profil durumu */
  updateApplicationStatus: (sellerProfileId, status) =>
    api.patch(`${SELLER_BASE}/${sellerProfileId}/status`, { status }),
};
