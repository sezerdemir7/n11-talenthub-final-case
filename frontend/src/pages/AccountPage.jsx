import { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { HiArrowRight, HiClipboardDocumentList, HiMapPin } from 'react-icons/hi2';
import { useAuth } from '../context/AuthContext';
import { orderService } from '../services/orderService';
import { parsePageResponse } from '../utils/pageResponse';
import Pagination from '../components/product/Pagination';
import LoadingSpinner from '../components/ui/LoadingSpinner';
import ErrorMessage from '../components/ui/ErrorMessage';
import { getOrderStatusMeta } from '../utils/orderStatus';

function formatMoney(value) {
  if (value == null) return '—';
  return `${Number(value).toLocaleString('tr-TR', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })} TL`;
}

export default function AccountPage() {
  const { user } = useAuth();
  const [orders, setOrders] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchOrders = useCallback(async () => {
    const uid = user?.userId;
    if (!uid) {
      setLoading(false);
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const { data: rest } = await orderService.getByUserId(uid, { page, size: 10 });
      const parsed = parsePageResponse(rest.data);
      setOrders(parsed.content);
      setTotalPages(parsed.totalPages);
    } catch (err) {
      setError(err.message || 'Siparişler yüklenemedi');
    } finally {
      setLoading(false);
    }
  }, [user?.userId, page]);

  useEffect(() => {
    fetchOrders();
  }, [fetchOrders]);

  if (!user?.userId) {
    return (
      <div className="max-w-3xl mx-auto px-4 py-12 text-center text-gray-500">
        Oturum bilgisi bulunamadı.
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <div className="flex items-center gap-3 mb-2">
        <HiClipboardDocumentList className="h-8 w-8 text-primary" />
        <h1 className="text-2xl font-bold text-secondary">Hesabım</h1>
      </div>
      <p className="text-sm text-gray-500 mb-6">
        E-posta: <span className="font-medium text-secondary">{user.email}</span>
      </p>

      <div className="flex flex-wrap gap-3 mb-8">
        <Link
          to="/account/addresses"
          className="inline-flex items-center gap-2 px-4 py-2.5 rounded-lg border-2 border-gray-200 text-secondary font-medium hover:border-primary hover:text-primary transition-colors"
        >
          <HiMapPin className="h-5 w-5 text-primary" />
          Adreslerim
        </Link>
      </div>

      <h2 className="text-lg font-semibold text-secondary mb-4">Siparişlerim</h2>

      {loading ? (
        <LoadingSpinner />
      ) : error ? (
        <ErrorMessage message={error} onRetry={fetchOrders} />
      ) : orders.length === 0 ? (
        <div className="bg-white rounded-xl border border-gray-100 p-10 text-center text-gray-500">
          Henüz siparişiniz yok.
        </div>
      ) : (
        <div className="space-y-3">
          {orders.map((order) => {
            const st = getOrderStatusMeta(order.status);
            return (
              <div
                key={order.orderId ?? order.id}
                className="bg-white rounded-xl border border-gray-100 p-4 flex flex-wrap items-center justify-between gap-4 hover:shadow-md transition-shadow"
              >
                <div>
                  <p className="text-xs text-gray-500">Sipariş no</p>
                  <p className="font-mono font-semibold text-secondary">
                    #{order.orderId ?? order.id}
                  </p>
                  <span
                    className={`inline-block mt-2 text-xs font-semibold px-2 py-0.5 rounded ${st.className}`}
                  >
                    {st.label}
                  </span>
                </div>
                <div className="text-right">
                  <p className="text-xs text-gray-500">Toplam</p>
                  <p className="text-lg font-bold text-primary">{formatMoney(order.totalPrice)}</p>
                </div>
                <Link
                  to={`/account/orders/${order.orderId ?? order.id}`}
                  className="inline-flex items-center gap-1 text-sm font-semibold text-primary hover:underline ml-auto"
                >
                  Detay <HiArrowRight className="h-4 w-4" />
                </Link>
              </div>
            );
          })}
          <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
        </div>
      )}
    </div>
  );
}
