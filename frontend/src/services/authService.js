import axios from 'axios';

const baseURL = () => import.meta.env.VITE_API_BASE_URL;

const AUTH_ENDPOINTS = {
    LOGIN: '/v1/auth/login',
    REGISTER: '/v1/auth/register',
    REGISTER_SELLER: '/v1/auth/register/seller',
    REFRESH_TOKEN: '/v1/auth/refresh-token',
    LOGOUT: '/v1/auth/logout',
};

/**
 * Refresh / logout: ham axios — api instance süresi dolmuş Bearer eklediği için
 * refresh isteği 401 dönebiliyordu. Bu çağrılarda Authorization gönderilmez.
 */
const authAxios = () =>
    axios.create({
        baseURL: baseURL(),
        timeout: 15000,
        headers: { 'Content-Type': 'application/json' },
    });

export const authService = {
    login: (credentials) => authAxios().post(AUTH_ENDPOINTS.LOGIN, credentials),

    register: (userData) => authAxios().post(AUTH_ENDPOINTS.REGISTER, userData),

    registerSeller: (payload) => authAxios().post(AUTH_ENDPOINTS.REGISTER_SELLER, payload),

    refreshToken: (refreshToken) =>
        authAxios().post(AUTH_ENDPOINTS.REFRESH_TOKEN, null, {
            params: { refreshToken },
        }),

    logout: (refreshToken) =>
        authAxios().post(AUTH_ENDPOINTS.LOGOUT, null, {
            params: { refreshToken },
        }),
};