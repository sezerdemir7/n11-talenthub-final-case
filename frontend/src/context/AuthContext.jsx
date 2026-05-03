import { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { authService } from '../services/authService';
import { getUserFromToken, isTokenExpired } from '../utils/jwt';
import { parseAuthTokensFromResponseBody } from '../utils/authResponse';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem('user');
    return stored ? JSON.parse(stored) : null;
  });
  const [token, setToken] = useState(() => localStorage.getItem('accessToken'));
  const [loading, setLoading] = useState(true);

  const logout = useCallback(async () => {
    const refreshToken = localStorage.getItem('refreshToken');

    try {
      if (refreshToken) {
        await authService.logout(refreshToken);
      }
    } catch {
      // Logout API fails silently — tokens still cleared client-side
    } finally {
      localStorage.clear();
      setUser(null);
      setToken(null);
    }
  }, []);

  useEffect(() => {
    const onCleared = () => {
      setUser(null);
      setToken(null);
    };
    window.addEventListener('auth:cleared', onCleared);
    return () => window.removeEventListener('auth:cleared', onCleared);
  }, []);

  useEffect(() => {
    const onRefreshed = (e) => {
      const access = e.detail?.accessToken;
      if (!access) return;
      setToken(access);
      const userInfo = getUserFromToken(access);
      if (userInfo) {
        setUser(userInfo);
        localStorage.setItem('user', JSON.stringify(userInfo));
      }
    };
    window.addEventListener('auth:refreshed', onRefreshed);
    return () => window.removeEventListener('auth:refreshed', onRefreshed);
  }, []);

  useEffect(() => {
    const initAuth = () => {
      const storedToken = localStorage.getItem('accessToken');

      if (!storedToken || isTokenExpired(storedToken)) {
        const refreshToken = localStorage.getItem('refreshToken');
        if (refreshToken) {
          authService
              .refreshToken(refreshToken)
              .then((response) => {
                const parsed = parseAuthTokensFromResponseBody(response.data);
                if (!parsed) throw new Error('Geçersiz refresh yanıtı');
                const { accessToken: newAccess, refreshToken: newRefresh } = parsed;
                localStorage.setItem('accessToken', newAccess);
                if (newRefresh) {
                  localStorage.setItem('refreshToken', newRefresh);
                }

                const userInfo = getUserFromToken(newAccess);
                localStorage.setItem('user', JSON.stringify(userInfo));
                setToken(newAccess);
                setUser(userInfo);
              })
              .catch(() => {
                localStorage.removeItem('accessToken');
                localStorage.removeItem('refreshToken');
                localStorage.removeItem('user');
                setUser(null);
                setToken(null);
              })
              .finally(() => setLoading(false));
          return;
        }

        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('user');
        setUser(null);
        setToken(null);
        setLoading(false);
        return;
      }

      const userInfo = getUserFromToken(storedToken);
      if (userInfo) {
        setUser(userInfo);
        localStorage.setItem('user', JSON.stringify(userInfo));
      }
      setLoading(false);
    };

    initAuth();
  }, []);

  const login = async (credentials) => {
    const { data: restResponse } = await authService.login(credentials);
    const parsed = parseAuthTokensFromResponseBody(restResponse);
    if (!parsed) {
      throw new Error('Giriş yanıtı geçersiz');
    }
    const { accessToken, refreshToken } = parsed;

    localStorage.setItem('accessToken', accessToken);
    if (refreshToken) {
      localStorage.setItem('refreshToken', refreshToken);
    }

    const userInfo = getUserFromToken(accessToken);
    localStorage.setItem('user', JSON.stringify(userInfo));

    setToken(accessToken);
    setUser(userInfo);

    return { ...restResponse, user: userInfo };
  };

  const register = async (userData) => {
    const { data: restResponse } = await authService.register(userData);
    const parsed = parseAuthTokensFromResponseBody(restResponse);
    if (!parsed) {
      throw new Error('Kayıt yanıtı geçersiz');
    }
    const { accessToken, refreshToken } = parsed;

    localStorage.setItem('accessToken', accessToken);
    if (refreshToken) {
      localStorage.setItem('refreshToken', refreshToken);
    }

    const userInfo = getUserFromToken(accessToken);
    localStorage.setItem('user', JSON.stringify(userInfo));

    setToken(accessToken);
    setUser(userInfo);

    return restResponse;
  };

  const registerSeller = async (payload) => {
    const { data: restResponse } = await authService.registerSeller(payload);
    const parsed = parseAuthTokensFromResponseBody(restResponse);
    if (!parsed) {
      throw new Error('Kayıt yanıtı geçersiz');
    }
    const { accessToken, refreshToken } = parsed;

    localStorage.setItem('accessToken', accessToken);
    if (refreshToken) {
      localStorage.setItem('refreshToken', refreshToken);
    }

    const userInfo = getUserFromToken(accessToken);
    localStorage.setItem('user', JSON.stringify(userInfo));

    setToken(accessToken);
    setUser(userInfo);

    return { ...restResponse, user: userInfo };
  };

  const value = {
    user,
    token,
    loading,
    isAuthenticated: !!token && !!user,
    login,
    register,
    registerSeller,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}