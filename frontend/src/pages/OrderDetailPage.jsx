import { useState, useEffect } from 'react';
import { Link, useParams, useNavigate } from 'react-router-dom';
import { HiArrowLeft, HiMapPin, HiShoppingBag, HiCreditCard } from 'react-icons/hi2';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { orderService } from '../services/orderService';
import { getOrderStatusMeta } from '../utils/orderStatus';
import LoadingSpinner from '../components/ui/LoadingSpinner';
import ErrorMessage from '../components/ui/ErrorMessage';
import Button from '../components/ui/Button';

function formatMoney(value) {
  if (value == null) return '—';
  return `${Number(value).toLocaleString('tr-TR', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })} TL`;
}

export default function OrderDetailPage() {
  const { orderId } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const { showToast } = useToast();
  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [cancelConfirmOpen, setCancelConfirmOpen] = useState(false);
  const [cancelling, setCancelling] = useState(false);

  useEffect(() => {
    setCancelConfirmOpen(false);
  }, [orderId]);

  useEffect(() => {
    let cancelled = false;
    (async () => {
      setLoading(true);
      setError(null);
      try {
        const { data: rest } = await orderService.getById(orderId);
        if (!cancelled) setOrder(rest.data);
      } catch (err) {
        if (!cancelled) setError(err.message || 'Sipariş yüklenemedi');
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [orderId]);

  const handleCancelOrder = async () => {
    const oid = order?.orderId ?? order?.id ?? orderId;
    if (oid == null) {
      showToast('Sipariş bilgisi eksik', 'error');
      return;
    }
    setCancelling(true);
    try {
      const { data: raw } = await orderService.cancelOrder(oid);
      showToast(raw?.message || 'Sipariş iptal edildi', 'success');
      setCancelConfirmOpen(false);
      const { data: rest } = await orderService.getById(orderId);
      setOrder(rest.data);
    } catch (err) {
      showToast(err.message || 'Sipariş iptal edilemedi', 'error');
    } finally {
      setCancelling(false);
    }
  };

  if (loading) return <LoadingSpinner size="lg" />;
  if (error) return <ErrorMessage message={error} onRetry={() => navigate(0)} />;
  if (!order) return <ErrorMessage message="Sipariş bulunamadı" />;

  const addr = order.address;
  const items = order.items || [];
  const oid = order.orderId ?? order.id;
  const statusKey = (order.status || '').toUpperCase();
  const st = getOrderStatusMeta(order.status);
  const needsPayment = statusKey === 'WAITING_PAYMENT';
  const canCancelConfirmed = statusKey === 'CONFIRMED' && Boolean(user);

  return (
    <div className="max-w-3xl mx-auto px-4 py-8">
      <button
        type="button"
        onClick={() => navigate('/account')}
        className="flex items-center gap-2 text-sm text-gray-500 hover:text-primary mb-6 cursor-pointer"
      >
        <HiArrowLeft className="h-4 w-4" />
        Siparişlere dön
      </button>

      <div className="bg-white rounded-xl border border-gray-100 overflow-hidden">
        <div className="p-6 border-b bg-gray-50">
          <div className="flex flex-wrap items-start justify-between gap-4">
            <div>
              <p className="text-xs text-gray-500">Sipariş</p>
              <h1 className="text-2xl font-bold text-secondary font-mono">#{oid}</h1>
              <span
                className={`inline-block mt-2 text-xs font-semibold px-2.5 py-1 rounded ${st.className}`}
              >
                {st.label}
              </span>
            </div>
            <div className="text-right">
              <p className="text-xs text-gray-500">Toplam</p>
              <p className="text-2xl font-black text-primary">{formatMoney(order.totalPrice)}</p>
            </div>
          </div>

          {needsPayment && (
            <div className="mt-4 pt-4 border-t border-gray-200/80">
              <p className="text-sm text-gray-600 mb-3">
                Bu sipariş için ödeme henüz tamamlanmadı. Ödeme yaptıktan sonra durum güncellenir.
              </p>
              <Button
                type="button"
                className="inline-flex items-center gap-2"
                onClick={() =>
                  navigate(`/checkout/pay/${oid}`, { state: { checkoutOrder: order } })
                }
              >
                <HiCreditCard className="h-5 w-5" />
                Ödemeyi tamamla
              </Button>
            </div>
          )}

          {canCancelConfirmed && (
            <div className="mt-4 pt-4 border-t border-gray-200/80">
              {!cancelConfirmOpen ? (
                <Button type="button" variant="outline" onClick={() => setCancelConfirmOpen(true)}>
                  Siparişi iptal et
                </Button>
              ) : (
                <div className="rounded-lg border border-amber-200 bg-amber-50/80 p-4 space-y-3">
                  <p className="text-sm text-amber-950">
                    Onaylanmış bu sipariş iptal edilecek. Devam etmek istiyor musunuz?
                  </p>
                  <div className="flex flex-wrap gap-2">
                    <Button
                      type="button"
                      variant="danger"
                      loading={cancelling}
                      onClick={handleCancelOrder}
                    >
                      Evet, iptal et
                    </Button>
                    <Button
                      type="button"
                      variant="ghost"
                      disabled={cancelling}
                      onClick={() => setCancelConfirmOpen(false)}
                    >
                      Vazgeç
                    </Button>
                  </div>
                </div>
              )}
            </div>
          )}
        </div>

        {addr && (
          <div className="p-6 border-b">
            <h2 className="text-sm font-bold text-secondary uppercase tracking-wide mb-3 flex items-center gap-2">
              <HiMapPin className="h-4 w-4 text-primary" />
              Teslimat adresi
            </h2>
            <div className="text-sm text-gray-700 space-y-1">
              <p>
                <span className="text-gray-500">İl / İlçe:</span>{' '}
                <span className="font-medium">
                  {addr.city} / {addr.district}
                </span>
              </p>
              <p>
                <span className="text-gray-500">Adres:</span> {addr.fullAddress}
              </p>
              {addr.postalCode && (
                <p>
                  <span className="text-gray-500">Posta kodu:</span> {addr.postalCode}
                </p>
              )}
            </div>
          </div>
        )}

        <div className="p-6">
          <h2 className="text-sm font-bold text-secondary uppercase tracking-wide mb-4 flex items-center gap-2">
            <HiShoppingBag className="h-4 w-4 text-primary" />
            Ürünler
          </h2>
          <div className="divide-y divide-gray-100 rounded-lg border border-gray-100 overflow-hidden">
            {items.map((line, idx) => (
              <div
                key={`${line.productId}-${idx}`}
                className="flex flex-wrap items-center justify-between gap-3 p-4 bg-white hover:bg-gray-50/80"
              >
                <div className="min-w-0 flex-1">
                  <Link
                    to={`/product/${line.productId}`}
                    className="font-medium text-secondary hover:text-primary block truncate"
                  >
                    {line.productName}
                  </Link>
                  <p className="text-xs text-gray-500 mt-1">
                    {formatMoney(line.unitPrice)} × {line.quantity}
                  </p>
                </div>
                <div className="text-sm font-semibold text-primary shrink-0">
                  {formatMoney(Number(line.unitPrice) * Number(line.quantity))}
                </div>
              </div>
            ))}
            {items.length === 0 && (
              <p className="p-6 text-sm text-gray-500 text-center">Satır bilgisi yok.</p>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
