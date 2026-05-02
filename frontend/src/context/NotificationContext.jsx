import { createContext, useContext, useEffect, useMemo, useCallback, useRef, useState } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client/dist/sockjs';
import { useAuth } from './AuthContext';
import { useToast } from './ToastContext';
import { notificationService } from '../services/notificationService';

const NotificationContext = createContext(null);
const USER_NOTIFICATION_DESTINATION = '/user/queue/notifications';
const USER_FALLBACK_DESTINATION = (userId) => `/queue/notifications/user-${userId}`;

function resolveWebSocketUrls() {
  const apiBaseUrl = import.meta.env.VITE_API_BASE_URL;
  if (!apiBaseUrl) return ['/ws'];
  const base = new URL(apiBaseUrl, window.location.origin);
  const candidates = [
    new URL('/ws', base).toString(),
    new URL('/api/ws', base).toString(),
  ];
  return [...new Set(candidates)];
}

/** Backend LocalDateTime / ISO string → ISO string */
function toIsoString(createdAt) {
  if (createdAt == null) return new Date().toISOString();
  if (typeof createdAt === 'string') {
    const d = new Date(createdAt);
    return Number.isNaN(d.getTime()) ? new Date().toISOString() : d.toISOString();
  }
  if (Array.isArray(createdAt) && createdAt.length >= 3) {
    const [y, m, day, h = 0, min = 0, s = 0] = createdAt;
    const d = new Date(y, m - 1, day, h, min, s);
    return Number.isNaN(d.getTime()) ? new Date().toISOString() : d.toISOString();
  }
  return new Date().toISOString();
}

function mapApiToNotification(n) {
  return {
    id: n.id ?? n.notificationId,
    type: n.type ?? 'INFO',
    message: n.message ?? '',
    dataRaw:
      typeof n.data === 'string' ? n.data : n.data != null ? JSON.stringify(n.data) : null,
    read: Boolean(n.read),
    createdAt: toIsoString(n.createdAt),
  };
}

export function NotificationProvider({ children }) {
  const { user, isAuthenticated } = useAuth();
  const { showToast } = useToast();
  const [notifications, setNotifications] = useState([]);
  const [isConnected, setIsConnected] = useState(false);
  const [hasMore, setHasMore] = useState(false);
  const [nextPage, setNextPage] = useState(0);
  const [loadingMore, setLoadingMore] = useState(false);
  const processedMessageKeys = useRef(new Set());
  const currentUserId = user?.userId ?? user?.id;
  const pageSize = 20;

  const fetchNotifications = useCallback(async () => {
    if (!isAuthenticated) return;
    try {
      const { data: rest } = await notificationService.getAll(0, pageSize);
      const pageData = rest?.data;
      const raw = Array.isArray(pageData?.content) ? pageData.content : [];
      const mapped = raw.map(mapApiToNotification);
      mapped.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
      setNotifications(mapped);
      const pageNumber = Number(pageData?.pageNumber ?? 0);
      const totalPages = Number(pageData?.totalPages ?? 0);
      setNextPage(pageNumber + 1);
      setHasMore(pageNumber + 1 < totalPages);
    } catch (error) {
      showToast(error.message || 'Bildirimler yüklenemedi', 'error');
    }
  }, [isAuthenticated, showToast]);

  const loadMoreNotifications = useCallback(async () => {
    if (!isAuthenticated || loadingMore || !hasMore) return;
    setLoadingMore(true);
    try {
      const { data: rest } = await notificationService.getAll(nextPage, pageSize);
      const pageData = rest?.data;
      const raw = Array.isArray(pageData?.content) ? pageData.content : [];
      const mapped = raw.map(mapApiToNotification);
      setNotifications((prev) => {
        const existingIds = new Set(prev.map((n) => n.id));
        const merged = [...prev];
        for (const n of mapped) {
          if (n.id != null && existingIds.has(n.id)) continue;
          merged.push(n);
        }
        return merged.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
      });
      const pageNumber = Number(pageData?.pageNumber ?? nextPage);
      const totalPages = Number(pageData?.totalPages ?? 0);
      setNextPage(pageNumber + 1);
      setHasMore(pageNumber + 1 < totalPages);
    } catch (error) {
      showToast(error.message || 'Daha fazla bildirim yüklenemedi', 'error');
    } finally {
      setLoadingMore(false);
    }
  }, [isAuthenticated, loadingMore, hasMore, nextPage, showToast]);

  useEffect(() => {
    if (!isAuthenticated) {
      setNotifications([]);
      setIsConnected(false);
      setHasMore(false);
      setNextPage(0);
      return;
    }
    fetchNotifications();
  }, [isAuthenticated, fetchNotifications]);

  useEffect(() => {
    if (!isAuthenticated || !currentUserId) {
      setIsConnected(false);
      return undefined;
    }

    const wsUrls = resolveWebSocketUrls();
    let wsIndex = 0;
    const client = new Client({
      webSocketFactory: () => new SockJS(wsUrls[wsIndex]),
      connectHeaders: {
        'X-User-Id': String(currentUserId),
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      debug: () => {},
    });

    client.onConnect = () => {
      setIsConnected(true);
      processedMessageKeys.current.clear();

      const handleIncomingMessage = (frame) => {
        try {
          const payload = JSON.parse(frame.body);
          const mapped = mapApiToNotification(payload);
          const messageKey = `${mapped.id ?? 'no-id'}-${mapped.type}-${mapped.message}-${mapped.createdAt}`;
          if (processedMessageKeys.current.has(messageKey)) return;
          processedMessageKeys.current.add(messageKey);

          setNotifications((prev) => {
            const exists = mapped.id != null && prev.some((n) => n.id === mapped.id);
            const next = exists ? prev : [mapped, ...prev];
            return next
              .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
              .slice(0, 50);
          });

          const msg = payload?.message || 'Yeni bildiriminiz var';
          showToast(msg, 'info', 5000);
        } catch {
          showToast('Yeni bildirim', 'info', 4000);
        }
        fetchNotifications();
      };

      client.subscribe(USER_NOTIFICATION_DESTINATION, handleIncomingMessage);
      client.subscribe(USER_FALLBACK_DESTINATION(currentUserId), handleIncomingMessage);
    };

    client.onWebSocketClose = () => {
      setIsConnected(false);
    };

    client.onWebSocketError = () => {
      if (wsIndex < wsUrls.length - 1) {
        wsIndex += 1;
      }
    };

    client.onStompError = () => {
      showToast('Bildirim bağlantısında hata oluştu', 'error', 4000);
    };

    client.activate();

    return () => {
      setIsConnected(false);
      client.deactivate();
    };
  }, [isAuthenticated, currentUserId, showToast, fetchNotifications]);

  const unreadCount = useMemo(() => notifications.filter((n) => !n.read).length, [notifications]);

  const markAllAsRead = useCallback(async () => {
    if (!isAuthenticated || unreadCount === 0) return;
    try {
      await notificationService.markAllAsRead();
      setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
    } catch (error) {
      showToast(error.message || 'Bildirimler güncellenemedi', 'error');
    }
  }, [isAuthenticated, unreadCount, showToast]);

  const markAsRead = useCallback(
    async (id) => {
      if (!isAuthenticated || id == null) return;
      try {
        await notificationService.markAsRead(id);
        setNotifications((prev) => prev.map((n) => (n.id === id ? { ...n, read: true } : n)));
      } catch (error) {
        showToast(error.message || 'Bildirim güncellenemedi', 'error');
      }
    },
    [isAuthenticated, showToast]
  );

  const value = {
    notifications,
    unreadCount,
    isConnected,
    hasMore,
    loadingMore,
    markAllAsRead,
    markAsRead,
    refreshNotifications: fetchNotifications,
    loadMoreNotifications,
  };

  return <NotificationContext.Provider value={value}>{children}</NotificationContext.Provider>;
}

// eslint-disable-next-line react-refresh/only-export-components
export function useNotifications() {
  const context = useContext(NotificationContext);
  if (!context) {
    throw new Error('useNotifications must be used within a NotificationProvider');
  }
  return context;
}
