import api from './api';

const PRODUCT_ENDPOINTS = {
  LIST: '/v1/products',
  DETAIL: (id) => `/v1/products/${id}`,
  BY_SLUG: (slug) => `/v1/products/slug/${slug}`,
  BY_SELLER: (sellerId) => `/v1/products/seller/${sellerId}`,
};

export const productService = {
  getAll: (params = {}) => {
    const { page = 0, size = 12, sort, keyword, categoryId, minPrice, maxPrice, active } = params;
    return api.get(PRODUCT_ENDPOINTS.LIST, {
      params: {
        page,
        size,
        sort,
        keyword: keyword || undefined,
        categoryId: categoryId || undefined,
        minPrice: minPrice || undefined,
        maxPrice: maxPrice || undefined,
        active: active ?? undefined,
      },
    });
  },

  getById: (id) => api.get(PRODUCT_ENDPOINTS.DETAIL(id)),

  getBySlug: (slug) => api.get(PRODUCT_ENDPOINTS.BY_SLUG(slug)),

  getBySellerId: (sellerId, params = {}) => {
    const { page = 0, size = 12 } = params;
    return api.get(PRODUCT_ENDPOINTS.BY_SELLER(sellerId), {
      params: { page, size },
    });
  },

  /**
   * Multipart create: @RequestPart("request") JSON + @RequestPart("image").
   * Header X-User-Id zorunlu. Content-Type FormData için api interceptor'da temizlenir.
   */
  create: (userId, requestBody, imageFile) => {
    if (!imageFile) {
      return Promise.reject(new Error('Ürün görseli zorunludur.'));
    }

    const formData = new FormData();
    formData.append(
      'request',
      new Blob([JSON.stringify(requestBody)], { type: 'application/json' })
    );
    formData.append('image', imageFile);

    return api.post(PRODUCT_ENDPOINTS.LIST, formData, {
      headers: {
        'X-User-Id': String(userId),
      },
    });
  },

  /**
   * Multipart update: sellerId query + @RequestPart("request") + optional @RequestPart("image").
   */
  update: (sellerId, productId, requestBody, imageFile) => {
    const formData = new FormData();
    formData.append(
      'request',
      new Blob([JSON.stringify(requestBody)], { type: 'application/json' })
    );
    if (imageFile) {
      formData.append('image', imageFile);
    }
    return api.put(PRODUCT_ENDPOINTS.DETAIL(productId), formData, {
      params: { sellerId },
    });
  },
};