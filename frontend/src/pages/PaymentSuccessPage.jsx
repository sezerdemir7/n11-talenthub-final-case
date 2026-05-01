import { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { HiCheckCircle } from 'react-icons/hi2';
import Button from '../components/ui/Button';
import { orderService } from '../services/orderService';
import { getOrderStatusMeta } from '../utils/orderStatus';
import LoadingSpinner from '../components/ui/LoadingSpinner';
import { useCart } from '../context/CartContext';

export default function PaymentSuccessPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const { fetchCart } = useCart();
  const orderId = location.state?.orderId;
  const [status, setStatus] = useState(null);
  const [loading, setLoading] = useState(Boolean(orderId));

  useEffect(() => {
    const timer = setTimeout(() => {
      fetchCart();
    }, 5000);
    return () => clearTimeout(timer);
  }, [fetchCart]);

  useEffect(() => {
    if (!orderId) {
      setLoading(false);
      return;
    }
    let cancelled = false;
    (async () => {
      try {
        const { data: rest } = await orderService.getById(orderId);
        if (!cancelled) setStatus(rest.data?.status ?? null);
      } catch {
        if (!cancelled) setStatus(null);
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [orderId]);

  if (!orderId) {
    return (
      <div className="max-w-lg mx-auto px-4 py-16 text-center">
        <p className="text-gray-600 mb-4">Geçersiz yönlendirme.</p>
        <Link to="/" className="text-primary font-semibold hover:underline">
          Ana sayfa
        </Link>
      </div>
    );
  }

  const meta = getOrderStatusMeta(status);

  return (
    <div className="max-w-lg mx-auto px-4 py-16 text-center">
      <HiCheckCircle className="h-20 w-20 text-success mx-auto mb-4" />
      <h1 className="text-2xl font-bold text-secondary mb-2">Ödemeniz alındı</h1>
      <p className="text-sm text-gray-600 mb-1">
        Sipariş numarası: <span className="font-mono font-semibold text-primary">#{orderId}</span>
      </p>
      {loading ? (
        <div className="flex justify-center my-6">
          <LoadingSpinner />
        </div>
      ) : (
        <p className={`text-sm font-medium mb-6 inline-block px-3 py-1 rounded ${meta.className}`}>
          Sipariş durumu: {meta.label}
        </p>
      )}
      <div className="flex flex-col sm:flex-row gap-3 justify-center">
        <Link to={`/account/orders/${orderId}`}>
          <Button>Sipariş detayı</Button>
        </Link>
        <Button variant="outline" onClick={() => navigate('/')}>
          Alışverişe devam
        </Button>
      </div>
    </div>
  );
}
