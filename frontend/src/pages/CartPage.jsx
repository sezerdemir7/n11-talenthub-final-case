import { Link, useNavigate } from 'react-router-dom';
import { HiShoppingBag, HiTrash } from 'react-icons/hi2';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';
import CartItem from '../components/cart/CartItem';
import Button from '../components/ui/Button';
import LoadingSpinner from '../components/ui/LoadingSpinner';

export default function CartPage() {
  const { cart, loading, clearCart, itemCount } = useCart();
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();

  if (loading) return <LoadingSpinner size="lg" />;

  const isEmpty = !cart.items || cart.items.length === 0;

  return (
    <div className="max-w-7xl mx-auto px-4 py-6">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-secondary">
          Sepetim {itemCount > 0 && <span className="text-primary">({itemCount} ürün)</span>}
        </h1>
        {!isEmpty && (
          <button
            onClick={clearCart}
            className="flex items-center gap-1.5 text-sm text-gray-500 hover:text-error transition-colors cursor-pointer"
          >
            <HiTrash className="h-4 w-4" />
            Sepeti Temizle
          </button>
        )}
      </div>

      {!isAuthenticated && !isEmpty && (
        <div className="mb-6 rounded-lg border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-900">
          Misafir olarak sepetinizi görüyorsunuz. <strong>Giriş yaptığınızda</strong> bu ürünler hesabınızdaki
          sepete aktarılır. Ödeme için giriş yapmanız gerekir.
        </div>
      )}

      {isEmpty ? (
        <div className="text-center py-16">
          <HiShoppingBag className="h-20 w-20 text-gray-300 mx-auto mb-4" />
          <h2 className="text-xl font-semibold text-secondary mb-2">Sepetiniz boş</h2>
          <p className="text-gray-500 mb-6">Hemen alışverişe başlayın!</p>
          <Link to="/">
            <Button>Alışverişe Başla</Button>
          </Link>
        </div>
      ) : (
        <>
          <p className="text-sm text-gray-500 mb-4">
            Ürün görseline veya ada tıklayarak ürün detay sayfasına gidebilirsiniz.
          </p>
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            <div className="lg:col-span-2 space-y-3">
              {cart.items.map((item) => (
                <CartItem key={item.id ?? `${item.productId}-${item.quantity}`} item={item} />
              ))}
            </div>

            <div className="lg:col-span-1">
              <div className="bg-white rounded-lg border border-gray-100 p-6 sticky top-24">
                <h2 className="text-lg font-bold text-secondary mb-4">Sipariş Özeti</h2>

                <div className="space-y-3 mb-4">
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-500">Ürünler ({itemCount})</span>
                    <span className="font-medium">{cart.totalPrice?.toLocaleString('tr-TR')} TL</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-500">Kargo</span>
                    <span className="font-medium text-success">Ücretsiz</span>
                  </div>
                </div>

                <div className="border-t pt-4 mb-6">
                  <div className="flex justify-between">
                    <span className="text-lg font-bold text-secondary">Toplam</span>
                    <span className="text-lg font-black text-primary">
                      {cart.totalPrice?.toLocaleString('tr-TR')} TL
                    </span>
                  </div>
                </div>

                {isAuthenticated ? (
                  <Button fullWidth size="lg" onClick={() => navigate('/checkout')}>
                    Siparişi Tamamla
                  </Button>
                ) : (
                  <Button
                    fullWidth
                    size="lg"
                    onClick={() =>
                      navigate('/login', { state: { from: { pathname: '/checkout' } } })
                    }
                  >
                    Ödeme için giriş yap
                  </Button>
                )}
              </div>
            </div>
          </div>
        </>
      )}
    </div>
  );
}
