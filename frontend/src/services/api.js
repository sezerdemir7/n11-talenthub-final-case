import axios from 'axios';
import { parseAuthTokensFromResponseBody } from '../utils/authResponse';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
  },
});

let isRefreshing = false;
let failedQueue = [];

const processQueue = (error, token = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });
  failedQueue = [];
};

const clearAuth = () => {
  localStorage.removeItem('accessToken');
  localStorage.removeItem('refreshToken');
  localStorage.removeItem('user');
};

function setRequestAuthHeader(config, bearerToken) {
  if (!config?.headers) return;
  if (typeof config.headers.set === 'function') {
    config.headers.set('Authorization', `Bearer ${bearerToken}`);
  } else {
    config.headers.Authorization = `Bearer ${bearerToken}`;
  }
}

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    // Varsayılan JSON Content-Type, FormData ile gönderilince Spring multipart eşleşmez.
    // FormData için Content-Type'ı kaldır; axios boundary ile multipart/form-data ekler.
    if (config.data instanceof FormData) {
      if (typeof config.headers.delete === 'function') {
        config.headers.delete('Content-Type');
      } else {
        delete config.headers['Content-Type'];
      }
    }
    return config;
  },
  (error) => Promise.reject(error)
);

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    if (
      originalRequest &&
      error.response?.status === 401 &&
      !originalRequest._retry &&
      !originalRequest.url?.includes('/v1/auth/')
    ) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then((token) => {
            setRequestAuthHeader(originalRequest, token);
            return api(originalRequest);
          })
          .catch((err) => Promise.reject(err));
      }

      originalRequest._retry = true;

      const refreshToken = localStorage.getItem('refreshToken');

      if (!refreshToken) {
        const hadSession = Boolean(
          localStorage.getItem('accessToken') || localStorage.getItem('refreshToken')
        );
        if (hadSession) {
          clearAuth();
          if (typeof window !== 'undefined') {
            window.dispatchEvent(new CustomEvent('auth:cleared'));
          }
        }
        return Promise.reject(error);
      }

      isRefreshing = true;

      try {
        const { data: restBody } = await axios.post(
          `${import.meta.env.VITE_API_BASE_URL}/v1/auth/refresh-token`,
          null,
          { params: { refreshToken } }
        );

        const parsed = parseAuthTokensFromResponseBody(restBody);
        if (!parsed) {
          throw new Error('Refresh yanıtında accessToken bulunamadı');
        }

        const { accessToken: newAccess, refreshToken: newRefresh } = parsed;

        localStorage.setItem('accessToken', newAccess);
        if (newRefresh) {
          localStorage.setItem('refreshToken', newRefresh);
        }

        api.defaults.headers.common.Authorization = `Bearer ${newAccess}`;
        setRequestAuthHeader(originalRequest, newAccess);

        if (typeof window !== 'undefined') {
          window.dispatchEvent(
            new CustomEvent('auth:refreshed', { detail: { accessToken: newAccess } })
          );
        }

        processQueue(null, newAccess);
        const retryResult = await api(originalRequest);
        return retryResult;
      } catch (refreshError) {
        processQueue(refreshError, null);
        clearAuth();
        if (typeof window !== 'undefined') {
          window.dispatchEvent(new CustomEvent('auth:cleared'));
        }
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    const message =
      error.response?.data?.message ||
      error.response?.data?.error ||
      'Bir hata oluştu. Lütfen tekrar deneyin.';

    return Promise.reject({ ...error, message });
  }
);

export default api;
