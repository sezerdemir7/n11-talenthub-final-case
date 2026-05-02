import { useState, useEffect, useCallback } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { HiShoppingBag } from 'react-icons/hi2';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { orderService } from '../services/orderService';
import { addressService } from '../services/addressService';
import AddressForm from '../components/account/AddressForm';
import Button from '../components/ui/Button';
import LoadingSpinner from '../components/ui/LoadingSpinner';

function formatMoney(value) {
  if (value == null) return '-';
  return `${Number(value).toLocaleString('tr-TR', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })} TL`;
}

function isDefaultAddress(a) {
  return Boolean(a?.defaultAddress ?? a?.default ?? a?.isDefault);
}

function addressSummary(row) {
  const line = row.fullAddress || row.addressLine || '';
  return `${row.district || ''}, ${row.city || ''} - ${line}`.trim();
}

export default function CheckoutPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { isAuthenticated } = useAuth();
  const { cart, fetchCart } = useCart();
  const { showToast } = useToast();
  const [submitting, setSubmitting] = useState(false);

  const [addressesLoading, setAddressesLoading] = useState(true);
  const [addresses, setAddresses] = useState([]);
  const [selectedAddressId, setSelectedAddressId] = useState(null);
  const [showNewAddressForm, setShowNewAddressForm] = useState(false);
  const [addressFormLoading, setAddressFormLoading] = useState(false);

  const routeSelectedProductIds = Array.isArray(location.state?.selectedProductIds)
    ? location.state.selectedProductIds.map(Number).filter((id) => !Number.isNaN(id))
    : null;
  const selectedProductIds = routeSelectedProductIds
    ?? (cart.items || []).filter((item) => item.available !== false).map((item) => item.productId);
  const selectedProductIdSet = new Set(selectedProductIds);
  const selectedItems = (cart.items || []).filter((item) => selectedProductIdSet.has(item.productId));
  const selectedTotalPrice = selectedItems.reduce(
    (sum, item) => sum + Number(item.totalPrice || 0),
    0
  );

  const loadAddresses = useCallback(
    async (preferSelectId) => {
      setAddressesLoading(true);
      try {
        const { data: rest } = await addressService.list();
        const list = rest.data || [];
        setAddresses(list);
        setSelectedAddressId((prev) => {
          const prefer =
            preferSelectId != null && preferSelectId !== ''
              ? Number(preferSelectId)
              : null;
          if (prefer != null && !Number.isNaN(prefer) && list.some((a) => a.id === prefer)) {
            return prefer;
          }
          if (prev && list.some((a) => a.id === prev)) return prev;
          const def = list.find(isDefaultAddress);
          if (def) return def.id;
          return list[0]?.id ?? null;
        });
        if (list.length === 0) {
          setShowNewAddressForm(true);
        }
      } catch {
        showToast('Adresler yuklenemedi', 'error');
        setAddresses([]);
      } finally {
        setAddressesLoading(false);
      }
    },
    [showToast]
  );

  useEffect(() => {
    fetchCart();
  }, [fetchCart]);

  useEffect(() => {
    loadAddresses();
  }, [loadAddresses]);

  const handleInlineAddressCreate = async (data) => {
    setAddressFormLoading(true);
    try {
      const { data: rest } = await addressService.create({
        title: data.title.trim(),
        recipientName: data.recipientName.trim(),
        phone: data.phone.trim(),
        city: data.city.trim(),
        district: data.district.trim(),
        fullAddress: data.fullAddress.trim(),
        postalCode: data.postalCode?.trim() || undefined,
      });
      const created = rest.data;
      showToast('Adres kaydedildi', 'success');
      setShowNewAddressForm(false);
      await loadAddresses(created?.id);
    } catch (err) {
      showToast(err.message || 'Adres eklenemedi', 'error');
    } finally {
      setAddressFormLoading(false);
    }
  };

  const handleCreateOrder = async () => {
    if (!isAuthenticated) {
      showToast('Oturum bilgisi bulunamadi', 'error');
      return;
    }
    if (selectedProductIds.length === 0) {
      showToast('Siparis icin en az bir urun secmelisiniz', 'error');
      return;
    }
    if (selectedAddressId == null) {
      showToast('Lutfen bir teslimat adresi secin veya ekleyin', 'error');
      return;
    }

    setSubmitting(true);
    try {
      const { data: rest } = await orderService.checkout({
        addressId: selectedAddressId,
        selectedProductIds,
      });
      const created = rest.data;
      const oid = created?.id ?? created?.orderId;
      if (oid == null) {
        showToast('Siparis yaniti gecersiz', 'error');
        return;
      }

      showToast(rest.message || 'Siparis olusturuldu. Odeme adimina yonlendiriliyorsunuz.', 'success');

      navigate(`/checkout/pay/${oid}`, {
        replace: true,
        state: { checkoutOrder: created },
      });
    } catch (error) {
      showToast(error.message || 'Siparis olusturulamadi', 'error');
    } finally {
      setSubmitting(false);
    }
  };

  if (!cart.items || cart.items.length === 0) {
    return (
      <div className="max-w-7xl mx-auto px-4 py-16 text-center">
        <HiShoppingBag className="h-16 w-16 text-gray-300 mx-auto mb-4" />
        <p className="text-gray-500 text-lg mb-4">Sepetiniz bos. Once urun ekleyin.</p>
        <Button onClick={() => navigate('/')}>Alisverise Basla</Button>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 py-6">
      <h1 className="text-2xl font-bold text-secondary mb-2">Siparis olustur</h1>
      <p className="text-sm text-gray-500 mb-6">
        Teslimat adresinizi secin ve secili urunlerle siparis olusturun. Adreslerinizi{' '}
        <Link to="/account/addresses" className="text-primary font-medium hover:underline">
          hesabinizdan
        </Link>{' '}
        da yonetebilirsiniz.
      </p>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 space-y-6">
          {selectedProductIds.length === 0 && (
            <div className="rounded-lg border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-900">
              Siparis olusturmak icin sepetten en az bir uygun urun secin.
            </div>
          )}
          <div className="bg-white rounded-lg border border-gray-100 p-6">
            <h2 className="text-lg font-bold text-secondary mb-4">Teslimat adresi</h2>

            {addressesLoading ? (
              <LoadingSpinner />
            ) : addresses.length > 0 ? (
              <>
                <ul className="space-y-3 mb-4">
                  {addresses.map((addr) => (
                    <li key={addr.id}>
                      <label
                        className={`flex gap-3 p-4 rounded-lg border-2 cursor-pointer transition-colors ${
                          selectedAddressId === addr.id
                            ? 'border-primary bg-primary/5'
                            : 'border-gray-100 hover:border-gray-200'
                        }`}
                      >
                        <input
                          type="radio"
                          name="checkoutAddress"
                          className="mt-1"
                          checked={selectedAddressId === addr.id}
                          onChange={() => setSelectedAddressId(addr.id)}
                        />
                        <div className="min-w-0 text-sm">
                          <p className="font-semibold text-secondary">
                            {addr.title || 'Adres'}
                            {isDefaultAddress(addr) && (
                              <span className="ml-2 text-xs font-normal text-primary">(varsayilan)</span>
                            )}
                          </p>
                          <p className="text-gray-600 mt-0.5">
                            {addr.recipientName || addr.fullName} - {addr.phone || addr.phoneNumber}
                          </p>
                          <p className="text-gray-500 mt-1 wrap-break-word">{addressSummary(addr)}</p>
                        </div>
                      </label>
                    </li>
                  ))}
                </ul>
                {!showNewAddressForm ? (
                  <button
                    type="button"
                    className="text-sm font-semibold text-primary hover:underline"
                    onClick={() => setShowNewAddressForm(true)}
                  >
                    + Farkli bir adres ekle
                  </button>
                ) : (
                  <div className="mt-4 pt-4 border-t border-gray-100">
                    <h3 className="text-sm font-semibold text-secondary mb-3">Yeni adres</h3>
                    <AddressForm
                      onSubmit={handleInlineAddressCreate}
                      submitLabel="Adresi kaydet ve kullan"
                      loading={addressFormLoading}
                      onCancel={() => setShowNewAddressForm(false)}
                    />
                  </div>
                )}
              </>
            ) : (
              <div>
                <p className="text-sm text-gray-600 mb-4">
                  Kayitli adresiniz yok. Siparis vermek icin teslimat adresini girin.
                </p>
                <AddressForm
                  onSubmit={handleInlineAddressCreate}
                  submitLabel="Adresi kaydet ve devam et"
                  loading={addressFormLoading}
                />
              </div>
            )}
          </div>

          <Button
            type="button"
            fullWidth
            size="lg"
            loading={submitting}
            disabled={selectedAddressId == null || addressesLoading || selectedProductIds.length === 0}
            onClick={handleCreateOrder}
          >
            Siparisi olustur ve odemeye gec
          </Button>
        </div>

        <div className="lg:col-span-1">
          <div className="bg-white rounded-lg border border-gray-100 p-6 sticky top-24">
            <h2 className="text-lg font-bold text-secondary mb-4">Siparis ozeti</h2>

            <div className="space-y-3 max-h-64 overflow-y-auto mb-4">
              {selectedItems.map((item) => (
                <div key={item.id ?? item.productId} className="flex justify-between text-sm gap-2">
                  <span className="text-gray-600 truncate">
                    {item.productName} x {item.quantity}
                  </span>
                  <span className="font-medium shrink-0">
                    {formatMoney(
                      item.totalPrice != null
                        ? item.totalPrice
                        : Number(item.unitPrice || 0) * Number(item.quantity || 0)
                    )}
                  </span>
                </div>
              ))}
            </div>

            <div className="border-t pt-4 space-y-2">
              <div className="flex justify-between text-sm">
                <span className="text-gray-500">Kargo</span>
                <span className="font-medium text-success">Ucretsiz</span>
              </div>
              <div className="flex justify-between text-lg font-bold">
                <span className="text-secondary">Toplam</span>
                <span className="text-primary">{formatMoney(selectedTotalPrice)}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
