/**
 * Sipariş durumu — backend enum ile uyumlu etiket ve badge sınıfları
 */
const STATUS_MAP = {
  WAITING_PAYMENT: {
    label: 'Ödeme Bekleniyor',
    className: 'bg-amber-100 text-amber-900',
  },
  CONFIRMED: {
    label: 'Onaylandı',
    className: 'bg-success/15 text-success',
  },
  CANCELLED: {
    label: 'İptal Edildi',
    className: 'bg-gray-200 text-gray-700',
  },
  EXPIRED: {
    label: 'Süresi Doldu',
    className: 'bg-gray-100 text-gray-600',
  },
};

export function getOrderStatusMeta(status) {
  const key = (status || '').toUpperCase();
  if (STATUS_MAP[key]) return STATUS_MAP[key];
  return {
    label: status || '—',
    className: 'bg-primary/10 text-primary',
  };
}
