import { useEffect, useMemo, useState } from 'react';
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
  const [selectedProductIds, setSelectedProductIds] = useState([]);

  const isEmpty = !cart.items || cart.items.length === 0;
  const hasUnavailableItems = cart.items?.some((item) => item.available === false) || false;
  const availableItems = useMemo(
    () => (cart.items || []).filter((item) => item.available !== false),
    [cart.items]
  );
  const selectedProductIdSet = useMemo(
    () => new Set(selectedProductIds),
    [selectedProductIds]
  );
  const selectedItems = useMemo(
    () => availableItems.filter((item) => selectedProductIdSet.has(item.productId)),
    [availableItems, selectedProductIdSet]
  );
  const selectedItemCount = selectedItems.reduce(
    (sum, item) => sum + Number(item.quantity || 0),
    0
  );
  const selectedTotalPrice = selectedItems.reduce(
    (sum, item) => sum + Number(item.totalPrice || 0),
    0
  );

  useEffect(() => {
    setSelectedProductIds((current) => {
      const availableIds = availableItems.map((item) => item.productId);
      if (current.length === 0) return availableIds;
      return current.filter((productId) => availableIds.includes(productId));
    });
  }, [availableItems]);

  if (loading) return <LoadingSpinner size="lg" />;

  const handleSelectionChange = (productId, checked) => {
    setSelectedProductIds((current) => {
      if (checked) {
        return current.includes(productId) ? current : [...current, productId];
      }
      return current.filter((id) => id !== productId);
    });
  };

  const goToCheckout = () => {
    navigate('/checkout', { state: { selectedProductIds } });
  };

  return (
    <div className="max-w-7xl mx-auto px-4 py-6">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-secondary">
          Sepetim {itemCount > 0 && <span className="text-primary">({itemCount} urun)</span>}
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
          Misafir olarak sepetinizi goruyorsunuz. Giris yaptiginizda bu urunler hesabinizdaki sepete aktarilir.
        </div>
      )}

      {isEmpty ? (
        <div className="text-center py-16">
          <HiShoppingBag className="h-20 w-20 text-gray-300 mx-auto mb-4" />
          <h2 className="text-xl font-semibold text-secondary mb-2">Sepetiniz bos</h2>
          <p className="text-gray-500 mb-6">Hemen alisverise baslayin.</p>
          <Link to="/">
            <Button>Alisverise Basla</Button>
          </Link>
        </div>
      ) : (
        <>
          <p className="text-sm text-gray-500 mb-4">
            Siparis vermek istediginiz urunleri secin. Uygun olmayan urunler secilemez.
          </p>
          {hasUnavailableItems && (
            <div className="mb-4 rounded-lg border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-900">
              Sepetinizde su anda satin almaya uygun olmayan urunler var. Secili degilse siparise dahil edilmezler.
            </div>
          )}
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            <div className="lg:col-span-2 space-y-3">
              {cart.items.map((item) => (
                <CartItem
                  key={item.id ?? `${item.productId}-${item.quantity}`}
                  item={item}
                  selected={selectedProductIdSet.has(item.productId)}
                  onSelectionChange={handleSelectionChange}
                />
              ))}
            </div>

            <div className="lg:col-span-1">
              <div className="bg-white rounded-lg border border-gray-100 p-6 sticky top-24">
                <h2 className="text-lg font-bold text-secondary mb-4">Siparis Ozeti</h2>

                <div className="space-y-3 mb-4">
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-500">Secili urunler ({selectedItemCount})</span>
                    <span className="font-medium">{selectedTotalPrice.toLocaleString('tr-TR')} TL</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-500">Kargo</span>
                    <span className="font-medium text-success">Ucretsiz</span>
                  </div>
                </div>

                <div className="border-t pt-4 mb-6">
                  <div className="flex justify-between">
                    <span className="text-lg font-bold text-secondary">Toplam</span>
                    <span className="text-lg font-black text-primary">
                      {selectedTotalPrice.toLocaleString('tr-TR')} TL
                    </span>
                  </div>
                </div>

                {isAuthenticated ? (
                  <Button fullWidth size="lg" disabled={selectedProductIds.length === 0} onClick={goToCheckout}>
                    Siparisi Tamamla
                  </Button>
                ) : (
                  <Button
                    fullWidth
                    size="lg"
                    disabled={selectedProductIds.length === 0}
                    onClick={() =>
                      navigate('/login', { state: { from: { pathname: '/checkout' }, selectedProductIds } })
                    }
                  >
                    Odeme icin giris yap
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
