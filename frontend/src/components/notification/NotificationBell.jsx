import { useMemo, useState } from 'react';
import { HiArrowPath, HiBell, HiSignal, HiSignalSlash } from 'react-icons/hi2';
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
  const { notifications, unreadCount, isConnected, markAllAsRead, refreshNotifications } = useNotifications();
  const [open, setOpen] = useState(false);
  const [markingRead, setMarkingRead] = useState(false);

  const recentNotifications = useMemo(() => notifications.slice(0, 8), [notifications]);

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
    <div className="relative">
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
        <div className="absolute right-0 mt-2 w-80 max-h-96 bg-white border border-gray-200 rounded-xl shadow-lg overflow-hidden z-50">
          <div className="flex items-center justify-between px-4 py-3 border-b border-gray-100">
            <div className="flex items-center gap-2">
              <span className="text-sm font-semibold text-secondary">Bildirimler</span>
              <span
                className={`text-xs px-2 py-0.5 rounded-full ${isConnected ? 'bg-success/10 text-success' : 'bg-error/10 text-error'}`}
              >
                {isConnected ? (
                  <span className="inline-flex items-center gap-1">
                    <HiSignal className="h-3.5 w-3.5" />
                    Canlı
                  </span>
                ) : (
                  <span className="inline-flex items-center gap-1">
                    <HiSignalSlash className="h-3.5 w-3.5" />
                    Kapalı
                  </span>
                )}
              </span>
            </div>
            <button
              type="button"
              onClick={() => refreshNotifications()}
              className="text-xs font-medium text-gray-500 hover:text-primary transition-colors cursor-pointer inline-flex items-center gap-1"
              title="Listeyi yenile"
            >
              <HiArrowPath className="h-3.5 w-3.5" />
              Yenile
            </button>
          </div>

          <div className="max-h-80 overflow-y-auto">
            {recentNotifications.length === 0 ? (
              <p className="px-4 py-6 text-sm text-gray-500 text-center">Henüz bildiriminiz yok.</p>
            ) : (
              recentNotifications.map((notification) => {
                const sub = dataSubtitle(notification.dataRaw);
                return (
                  <div
                    key={notification.id}
                    className={`px-4 py-3 border-b border-gray-50 ${notification.read ? 'bg-white' : 'bg-primary/5'}`}
                  >
                    {notification.type && (
                      <p className="text-[10px] font-semibold uppercase tracking-wide text-gray-400 mb-0.5">
                        {notification.type}
                      </p>
                    )}
                    <p className="text-sm text-secondary">{notification.message}</p>
                    {sub && <p className="text-xs text-gray-500 mt-1">{sub}</p>}
                    <p className="text-xs text-gray-400 mt-1">{formatNotificationTime(notification.createdAt)}</p>
                  </div>
                );
              })
            )}
          </div>
        </div>
      )}
    </div>
  );
}
