import { useState, useEffect } from 'react';
import { useParams, useNavigate, useLocation, Link } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { HiCreditCard, HiArrowLeft } from 'react-icons/hi2';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { paymentService } from '../services/paymentService';
import { orderService } from '../services/orderService';
import Button from '../components/ui/Button';
import Input from '../components/ui/Input';
import LoadingSpinner from '../components/ui/LoadingSpinner';
import ErrorMessage from '../components/ui/ErrorMessage';

/** İyzico sandbox test kart numaraları (yalnızca UI; saklanmaz) */
const IYZICO_TEST_CARD_NUMBERS = [
  '5890040000000016',
  '5526080000000006',
  '9792072000017956',
  '4766620000000001',
  '4603450000000000',
  '9792023757123604',
  '4987490000000002',
  '5311570000000005',
  '9792020000000001',
  '9792030000000000',
  '5170410000000004',
  '5400360000000003',
  '374427000000003',
  '4475050000000003',
  '5528790000000008',
  '4059030000000009',
  '5504720000000003',
  '5892830000000000',
  '4543590000000006',
  '4910050000000006',
  '4157920000000002',
  '6500528865390837',
  '6501700194147183',
  '5168880000000002',
  '5451030000000000',
];

function formatCardDigitsForInput(digits) {
  const d = String(digits).replace(/\D/g, '');
  return d.replace(/(\d{4})(?=\d)/g, '$1 ').trim();
}

function formatMoney(value) {
  if (value == null) return '—';
  return `${Number(value).toLocaleString('tr-TR', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })} TL`;
}

/**
 * Axios `response.data` (= raw). Dış `raw.success` / `timestamp` API sarmalayıcısıdır.
 * Ödeme sonucu: `raw.data.success` ve hata için `raw.data.message` (transactionId genelde null, kullanılmaz).
 */
function getPaymentOutcome(raw) {
  if (!raw || typeof raw !== 'object') {
    return { ok: false, message: 'Geçersiz yanıt' };
  }
  const paymentNode = raw.data;
  if (!paymentNode || typeof paymentNode !== 'object' || !('success' in paymentNode)) {
    return {
      ok: false,
      message: raw.message || 'Ödeme sonucu alınamadı',
    };
  }
  const ok = paymentNode.success === true;
  return {
    ok,
    message: paymentNode.message ?? null,
  };
}

export default function PaymentPage() {
  const { orderId } = useParams();
  const navigate = useNavigate();
  const location = useLocation();
  const { user } = useAuth();
  const { showToast } = useToast();

  const [order, setOrder] = useState(location.state?.checkoutOrder ?? null);
  const [loadError, setLoadError] = useState(null);
  const [loadingOrder, setLoadingOrder] = useState(!location.state?.checkoutOrder);

  const {
    register,
    handleSubmit,
    setValue,
    clearErrors,
    formState: { errors },
  } = useForm({
    defaultValues: {
      cardHolderName: '',
      cardNumber: '',
      expireMonth: '',
      expireYear: '',
      cvc: '',
    },
  });

  const [paying, setPaying] = useState(false);
  const [testCardsOpen, setTestCardsOpen] = useState(false);

  useEffect(() => {
    if (order) {
      setLoadingOrder(false);
      return;
    }
    let cancelled = false;
    const load = async () => {
      setLoadingOrder(true);
      setLoadError(null);
      try {
        const { data: rest } = await orderService.getById(orderId);
        if (!cancelled) setOrder(rest.data);
      } catch (err) {
        if (!cancelled) setLoadError(err.message || 'Sipariş yüklenemedi');
      } finally {
        if (!cancelled) setLoadingOrder(false);
      }
    };
    load();
    return () => {
      cancelled = true;
    };
  }, [orderId, order]);

  const amount = order?.totalPrice != null ? Number(order.totalPrice) : null;
  const uid = order?.userId != null ? Number(order.userId) : user?.userId != null ? Number(user.userId) : null;

  const onPay = async (values) => {
    if (amount == null || Number.isNaN(amount) || uid == null || Number.isNaN(uid)) {
      showToast('Sipariş tutarı veya kullanıcı bilgisi eksik', 'error');
      return;
    }

    setPaying(true);
    try {
      const { data: raw } = await paymentService.pay({
        orderId: Number(orderId),
        userId: uid,
        amount,
        card: {
          cardHolderName: values.cardHolderName.trim(),
          cardNumber: String(values.cardNumber).replace(/\s/g, ''),
          expireMonth: String(values.expireMonth).trim().padStart(2, '0'),
          expireYear: String(values.expireYear).trim(),
          cvc: String(values.cvc).trim(),
        },
      });

      const outcome = getPaymentOutcome(raw);

      if (!outcome.ok) {
        showToast(outcome.message || 'Ödeme başarısız', 'error');
        return;
      }

      showToast('Ödeme başarılı', 'success');
      navigate('/checkout/success', {
        replace: true,
        state: {
          orderId: Number(orderId),
        },
      });
    } catch (err) {
      showToast(err.message || 'Ödeme başarısız', 'error');
    } finally {
      setPaying(false);
    }
  };

  if (loadingOrder) return <LoadingSpinner size="lg" />;
  if (loadError) return <ErrorMessage message={loadError} onRetry={() => navigate('/account')} />;

  if (!order || amount == null || Number.isNaN(amount)) {
    return (
      <div className="max-w-lg mx-auto px-4 py-16 text-center">
        <p className="text-gray-600 mb-4">Sipariş bilgisi bulunamadı.</p>
        <Link to="/account" className="text-primary font-semibold hover:underline">
          Hesabıma dön
        </Link>
      </div>
    );
  }

  return (
    <div className="max-w-2xl mx-auto px-4 py-8">
      <button
        type="button"
        onClick={() => navigate(-1)}
        className="flex items-center gap-2 text-sm text-gray-500 hover:text-primary mb-6 cursor-pointer"
      >
        <HiArrowLeft className="h-4 w-4" />
        Geri
      </button>

      <div className="bg-white rounded-xl border border-gray-100 p-6 mb-6">
        <h1 className="text-2xl font-bold text-secondary mb-2 flex items-center gap-2">
          <HiCreditCard className="h-7 w-7 text-primary" />
          Ödeme
        </h1>
        <p className="text-sm text-gray-500">
          Sipariş <span className="font-mono font-semibold">#{orderId}</span> — Tutar:{' '}
          <span className="font-bold text-primary">{formatMoney(amount)}</span>
        </p>
        <p className="text-xs text-gray-400 mt-2">
          Kart bilgileri yalnızca bu istekle gönderilir; tarayıcıda saklanmaz.
        </p>
      </div>

      <div className="bg-white rounded-xl border border-gray-100 p-6">
        <h2 className="text-lg font-semibold text-secondary mb-4">Kart bilgileri</h2>
        <form onSubmit={handleSubmit(onPay)} className="space-y-4">
          <div className="flex flex-col gap-2">
            <Button
              type="button"
              variant="outline"
              fullWidth
              onClick={() => setTestCardsOpen((o) => !o)}
            >
              {testCardsOpen ? 'Test kart listesini gizle' : 'İyzico test kartları'}
            </Button>
            {testCardsOpen && (
              <div className="rounded-lg border border-gray-200 bg-gray-50 p-2 max-h-52 overflow-y-auto space-y-1">
                <p className="text-xs text-gray-500 px-2 py-1">Bir numaraya tıklayın — kart numarası alanı dolar.</p>
                {IYZICO_TEST_CARD_NUMBERS.map((num) => (
                  <button
                    key={num}
                    type="button"
                    className="w-full text-left font-mono text-sm px-3 py-2 rounded-md hover:bg-white hover:text-primary border border-transparent hover:border-primary/30 transition-colors cursor-pointer"
                    onClick={() => {
                      setValue('cardNumber', formatCardDigitsForInput(num), { shouldValidate: true, shouldDirty: true });
                      clearErrors('cardNumber');
                    }}
                  >
                    {formatCardDigitsForInput(num)}
                  </button>
                ))}
              </div>
            )}
          </div>
          <Input
            label="Kart üzerindeki isim"
            {...register('cardHolderName', { required: 'Zorunlu' })}
            error={errors.cardHolderName?.message}
            autoComplete="cc-name"
          />
          <Input
            label="Kart numarası"
            {...register('cardNumber', {
              required: 'Zorunlu',
              validate: (v) => {
                const d = String(v || '').replace(/\D/g, '');
                return (d.length >= 12 && d.length <= 19) || 'Geçerli kart numarası girin';
              },
            })}
            error={errors.cardNumber?.message}
            placeholder="5528 7900 0000 0008"
            autoComplete="cc-number"
          />
          <div className="grid grid-cols-2 sm:grid-cols-3 gap-4">
            <Input
              label="Ay (MM)"
              {...register('expireMonth', {
                required: 'Zorunlu',
                validate: (v) => {
                  const n = parseInt(String(v).trim(), 10);
                  return (!Number.isNaN(n) && n >= 1 && n <= 12) || '1-12 arası ay girin';
                },
              })}
              error={errors.expireMonth?.message}
              placeholder="12"
              autoComplete="cc-exp-month"
            />
            <Input
              label="Yıl (YYYY)"
              {...register('expireYear', {
                required: 'Zorunlu',
                minLength: { value: 4, message: '4 haneli yıl' },
              })}
              error={errors.expireYear?.message}
              placeholder="2030"
              autoComplete="cc-exp-year"
            />
            <Input
              label="CVC"
              type="password"
              {...register('cvc', {
                required: 'Zorunlu',
                minLength: { value: 3, message: 'En az 3 karakter' },
                maxLength: { value: 4, message: 'En fazla 4 karakter' },
              })}
              error={errors.cvc?.message}
              placeholder="123"
              autoComplete="cc-csc"
            />
          </div>
          <Button type="submit" fullWidth size="lg" loading={paying}>
            Ödemeyi tamamla
          </Button>
        </form>
      </div>
    </div>
  );
}
