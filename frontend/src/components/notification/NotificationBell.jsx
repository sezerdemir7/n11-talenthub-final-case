import { useEffect, useMemo, useRef, useState } from 'react';
import { HiArrowPath, HiBell, HiSignal, HiSignalSlash, HiXMark } from 'react-icons/hi2';
import { useNotifications } from '../../context/NotificationContext';

function formatNotificationTime(isoDate) {
  if (!isoDate) return '';
  return new Date(isoDate).toLocaleString('tr-TR', {
    day: '2-digit',
    month: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  });
}

function dataSubtitle(dataRaw) {
  if (!dataRaw) return null;
  try {
    const o = JSON.parse(dataRaw);
    if (o?.orderId != null) return `Sipariş No: #${o.orderId}`;
    if (o?.totalPrice != null) return `Tutar: ${Number(o.totalPrice).toLocaleString('tr-TR')} TL`;
  } catch {
    return null;
  }
  return null;
}

export default function NotificationBell() {
  const {
    notifications,
    unreadCount,
    isConnected,
    hasMore,
    loadingMore,
    markAllAsRead,
    refreshNotifications,
    loadMoreNotifications,
  } = useNotifications();
  const [open, setOpen] = useState(false);
  const [markingRead, setMarkingRead] = useState(false);
  const containerRef = useRef(null);

  const recentNotifications = useMemo(() => notifications, [notifications]);

  useEffect(() => {
    if (!open) return;
    const handleClickOutside = (e) => {
      if (containerRef.current && !containerRef.current.contains(e.target)) {
        setOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [open]);

  useEffect(() => {
    if (!open) return;
    const handleKeyDown = (e) => {
      if (e.key === 'Escape') setOpen(false);
    };
    document.addEventListener('keydown', handleKeyDown);
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, [open]);

  const handleToggle = async () => {
    const nextOpen = !open;
    setOpen(nextOpen);
    if (nextOpen && unreadCount > 0) {
      setMarkingRead(true);
      try {
        await markAllAsRead();
      } finally {
        setMarkingRead(false);
      }
    }
  };

  return (
    <div className="relative" ref={containerRef}>
      <button
        onClick={handleToggle}
        disabled={markingRead}
        className="relative flex items-center justify-center p-2 rounded-lg text-gray-600 hover:text-primary hover:bg-gray-50 transition-colors cursor-pointer disabled:opacity-60"
        title="Bildirimler"
      >
        <HiBell className="h-5 w-5" />
        {unreadCount > 0 && (
          <span className="absolute -top-1 -right-1 bg-primary text-white text-[10px] font-bold rounded-full h-4 min-w-4 px-1 flex items-center justify-center leading-none">
            {unreadCount > 9 ? '9+' : unreadCount}
          </span>
        )}
      </button>

      {open && (
        <div
          className="absolute right-0 mt-2 w-80 bg-white border border-gray-200 rounded-xl shadow-xl z-50 flex flex-col"
          style={{ maxHeight: 'min(480px, calc(100vh - 80px))' }}
        >
          {/* Header */}
          <div className="flex items-center justify-between px-4 py-3 border-b border-gray-100 shrink-0">
            <div className="flex items-center gap-2">
              <span className="text-sm font-semibold text-secondary">Bildirimler</span>
              <span
                className={`text-xs px-2 py-0.5 rounded-full inline-flex items-center gap-1 font-medium ${
                  isConnected ? 'bg-green-50 text-green-600' : 'bg-red-50 text-red-500'
                }`}
              >
                {isConnected ? (
                  <>
                    <HiSignal className="h-3.5 w-3.5" />
                    Canlı
                  </>
                ) : (
                  <>
                    <HiSignalSlash className="h-3.5 w-3.5" />
                    Kapalı
                  </>
                )}
              </span>
            </div>
            <div className="flex items-center gap-1">
              <button
                type="button"
                onClick={() => refreshNotifications()}
                className="p-1.5 rounded-md text-gray-400 hover:text-primary hover:bg-gray-100 transition-colors cursor-pointer"
                title="Listeyi yenile"
              >
                <HiArrowPath className="h-4 w-4" />
              </button>
              <button
                type="button"
                onClick={() => setOpen(false)}
                className="p-1.5 rounded-md text-gray-400 hover:text-gray-700 hover:bg-gray-100 transition-colors cursor-pointer"
                title="Kapat"
              >
                <HiXMark className="h-4 w-4" />
              </button>
            </div>
          </div>

          {/* Notification list */}
          <div className="overflow-y-auto flex-1">
            {recentNotifications.length === 0 ? (
              <div className="flex flex-col items-center justify-center px-4 py-10 gap-2">
                <HiBell className="h-8 w-8 text-gray-200" />
                <p className="text-sm text-gray-400">Henüz bildiriminiz yok.</p>
              </div>
            ) : (
              recentNotifications.map((notification) => {
                const sub = dataSubtitle(notification.dataRaw);
                return (
                  <div
                    key={notification.id}
                    className={`px-4 py-3 border-b border-gray-50 last:border-b-0 ${
                      notification.read ? 'bg-white' : 'bg-primary/5'
                    }`}
                  >
                    <div className="flex items-start justify-between gap-2">
                      <div className="flex-1 min-w-0">
                        {notification.type && (
                          <p className="text-[10px] font-semibold uppercase tracking-wide text-gray-400 mb-0.5">
                            {notification.type}
                          </p>
                        )}
                        <p className="text-sm text-secondary leading-snug">{notification.message}</p>
                        {sub && <p className="text-xs text-gray-500 mt-1">{sub}</p>}
                        <p className="text-xs text-gray-400 mt-1">{formatNotificationTime(notification.createdAt)}</p>
                      </div>
                      {!notification.read && (
                        <span className="mt-1.5 h-2 w-2 shrink-0 rounded-full bg-primary" />
                      )}
                    </div>
                  </div>
                );
              })
            )}
          </div>
          {hasMore && (
            <div className="border-t border-gray-100 p-2 shrink-0">
              <button
                type="button"
                onClick={loadMoreNotifications}
                disabled={loadingMore}
                className="w-full rounded-lg border border-gray-200 py-2 text-xs font-semibold text-gray-600 hover:bg-gray-50 transition-colors cursor-pointer disabled:opacity-60"
              >
                {loadingMore ? 'Yukleniyor...' : 'Daha Fazla Goster'}
              </button>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
