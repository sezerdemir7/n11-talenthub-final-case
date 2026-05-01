import api from './api';

const CATEGORY_ENDPOINTS = {
  BASE: '/v1/categories',
  BY_ID: (id) => `/v1/categories/${id}`,
  BY_SLUG: (slug) => `/v1/categories/slug/${slug}`,
  TREE: '/v1/categories/tree',
  FILTERS: '/v1/categories/filters',
};

export const categoryService = {
  getAll: () => api.get(CATEGORY_ENDPOINTS.BASE),

  getById: (id) => api.get(CATEGORY_ENDPOINTS.BY_ID(id)),

  getBySlug: (slug) => api.get(CATEGORY_ENDPOINTS.BY_SLUG(slug)),

  getTree: () => api.get(CATEGORY_ENDPOINTS.TREE),

  getFilters: () => api.get(CATEGORY_ENDPOINTS.FILTERS),

  create: (data) => api.post(CATEGORY_ENDPOINTS.BASE, data),

  update: (id, data) => api.put(CATEGORY_ENDPOINTS.BY_ID(id), data),

  delete: (id) => api.delete(CATEGORY_ENDPOINTS.BY_ID(id)),
};
