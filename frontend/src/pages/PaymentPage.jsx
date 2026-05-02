import { useState, useEffect } from 'react';
import { useParams, useNavigate, useLocation, Link } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import {
  HiArrowLeft,
  HiLockClosed,
  HiShieldCheck,
  HiChevronDown,
  HiChevronUp,
  HiShoppingBag,
} from 'react-icons/hi2';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { paymentService } from '../services/paymentService';
import { orderService } from '../services/orderService';
import Button from '../components/ui/Button';
import LoadingSpinner from '../components/ui/LoadingSpinner';
import ErrorMessage from '../components/ui/ErrorMessage';

/* ─── Test kartları (Iyzico sandbox) ─────────────────── */
const TEST_CARDS = [
  { brand: 'VISA',       number: '4766620000000001' },
  { brand: 'VISA',       number: '4603450000000000' },
  { brand: 'VISA',       number: '4059030000000009' },
  { brand: 'MC',         number: '5526080000000006' },
  { brand: 'MC',         number: '5528790000000008' },
  { brand: 'MC',         number: '5311570000000005' },
  { brand: 'TROY',       number: '9792072000017956' },
  { brand: 'TROY',       number: '6500528865390837' },
  { brand: 'AMEX',       number: '374427000000003'  },
];

/* ─── Yardımcılar ────────────────────────────────────── */
function formatMoney(value) {
  if (value == null) return '—';
  return `${Number(value).toLocaleString('tr-TR', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })} TL`;
}

function formatCardDisplay(raw) {
  const digits = (raw || '').replace(/\D/g, '');
  return digits.replace(/(.{4})(?=.)/g, '$1 ').trim();
}

function detectBrand(raw) {
  const n = (raw || '').replace(/\D/g, '');
  if (/^4/.test(n)) return 'VISA';
  if (/^5[1-5]|^2[2-7]/.test(n)) return 'MC';
  if (/^3[47]/.test(n)) return 'AMEX';
  if (/^9792|^6500|^6501/.test(n)) return 'TROY';
  return null;
}

function getPaymentOutcome(raw) {
  if (!raw || typeof raw !== 'object') return { ok: false, message: 'Geçersiz yanıt' };
  const node = raw.data;
  if (!node || typeof node !== 'object' || !('success' in node)) {
    return { ok: false, message: raw.message || 'Ödeme sonucu alınamadı' };
  }
  return { ok: node.success === true, message: node.message ?? null };
}

/* ─── Kart önizleme bileşeni ─────────────────────────── */
function CardPreview({ number, holderName, expiry, cvcFocused }) {
  const raw = (number || '').replace(/\D/g, '');
  const padded = raw.padEnd(16, '•');
  const groups = padded.match(/.{1,4}/g) || [];
  const displayNumber = groups.join(' ');
  const displayName = holderName?.trim().toUpperCase() || 'AD SOYAD';
  const displayExpiry = expiry || 'AA/YY';
  const brand = detectBrand(raw);

  const brandLabel = {
    VISA: (
      <span className="text-lg font-black italic tracking-tight text-white/90">VISA</span>
    ),
    MC: (
      <div className="flex items-center">
        <div className="h-6 w-6 rounded-full bg-red-500 opacity-90" />
        <div className="h-6 w-6 rounded-full bg-yellow-400 -ml-3 opacity-90" />
      </div>
    ),
    TROY: (
      <span className="text-sm font-black text-white/90">TROY</span>
    ),
    AMEX: (
      <span className="text-sm font-black text-blue-300">AMEX</span>
    ),
  };

  if (cvcFocused) {
    return (
      <div className="h-48 w-full rounded-2xl shadow-2xl overflow-hidden relative"
        style={{ background: 'linear-gradient(135deg, #1a1a2e 0%, #0f3460 100%)' }}>
        <div className="absolute inset-x-0 top-10 h-10 bg-black/70" />
        <div className="absolute inset-x-0 top-24 px-6">
          <div className="bg-gray-100 rounded h-9 flex items-center justify-end pr-4 shadow-inner">
            <span className="font-mono text-gray-500 tracking-widest text-sm">• • •</span>
          </div>
          <p className="text-white/50 text-xs text-right mt-1">CVC / CVV</p>
        </div>
        <div className="absolute bottom-5 right-6">
          {brand && brandLabel[brand]}
        </div>
      </div>
    );
  }

  return (
    <div
      className="h-48 w-full rounded-2xl shadow-2xl overflow-hidden relative text-white select-none"
      style={{ background: 'linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%)' }}
    >
      {/* Dekoratif daireler */}
      <div className="absolute -right-10 -top-10 h-52 w-52 rounded-full bg-white/5" />
      <div className="absolute -right-4 top-4 h-28 w-28 rounded-full bg-white/5" />

      <div className="relative h-full p-6 flex flex-col justify-between">
        {/* Üst: çip + marka */}
        <div className="flex items-start justify-between">
          {/* EMV çip */}
          <div className="h-8 w-11 rounded bg-gradient-to-br from-amber-300 to-amber-500 flex items-center justify-center shadow">
            <div className="grid grid-cols-2 gap-0.5 p-1">
              {[...Array(4)].map((_, i) => (
                <div key={i} className="h-1.5 w-1.5 rounded-sm bg-amber-700/50" />
              ))}
            </div>
          </div>
          {brand ? brandLabel[brand] : <span className="text-white/30 text-xs">•••</span>}
        </div>

        {/* Kart numarası */}
        <p className="font-mono text-xl tracking-[0.2em] text-white/90">
          {displayNumber}
        </p>

        {/* Alt: isim + son kullanım */}
        <div className="flex items-end justify-between">
          <div>
            <p className="text-[9px] text-white/40 uppercase tracking-widest mb-0.5">Kart Sahibi</p>
            <p className="text-sm font-semibold tracking-wide text-white/90 truncate max-w-[160px]">
              {displayName}
            </p>
          </div>
          <div className="text-right">
            <p className="text-[9px] text-white/40 uppercase tracking-widest mb-0.5">Son Kullanım</p>
            <p className="text-sm font-semibold text-white/90">{displayExpiry}</p>
          </div>
        </div>
      </div>
    </div>
  );
}

/* ─── Giriş alanı (label + input + hata) ────────────── */
function Field({ label, error, children }) {
  return (
    <div>
      <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wide mb-1.5">
        {label}
      </label>
      {children}
      {error && <p className="mt-1 text-xs text-error">{error}</p>}
    </div>
  );
}

function StyledInput({ error, className = '', ...props }) {
  return (
    <input
      className={`w-full px-4 py-3 rounded-xl border text-sm text-secondary placeholder:text-gray-300
        focus:outline-none focus:ring-2 focus:ring-primary/30 focus:border-primary transition-all
        ${error ? 'border-error' : 'border-gray-200'}
        ${className}`}
      {...props}
    />
  );
}

/* ─── Ana sayfa ──────────────────────────────────────── */
export default function PaymentPage() {
  const { orderId } = useParams();
  const navigate = useNavigate();
  const location = useLocation();
  const { user } = useAuth();
  const { showToast } = useToast();

  const [order, setOrder] = useState(location.state?.checkoutOrder ?? null);
  const [loadError, setLoadError] = useState(null);
  const [loadingOrder, setLoadingOrder] = useState(!location.state?.checkoutOrder);
  const [paying, setPaying] = useState(false);
  const [testCardsOpen, setTestCardsOpen] = useState(false);
  const [cvcFocused, setCvcFocused] = useState(false);

  const {
    register,
    handleSubmit,
    setValue,
    watch,
    formState: { errors },
  } = useForm({
    defaultValues: {
      cardHolderName: '',
      cardNumber: '',
      expiry: '',
      cvc: '',
    },
  });

  const watchedCard = watch(['cardNumber', 'cardHolderName', 'expiry']);
  const [cardNumber, cardHolderName, expiry] = watchedCard;

  /* sipariş yükleme */
  useEffect(() => {
    if (order) { setLoadingOrder(false); return; }
    let cancelled = false;
    (async () => {
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
    })();
    return () => { cancelled = true; };
  }, [orderId, order]);

  const amount = order?.totalPrice != null ? Number(order.totalPrice) : null;
  const uid = order?.userId != null ? Number(order.userId) : user?.userId != null ? Number(user.userId) : null;

  /* kart numarası auto-format */
  const handleCardNumberChange = (e) => {
    const raw = e.target.value.replace(/\D/g, '').slice(0, 16);
    const formatted = raw.replace(/(.{4})(?=.)/g, '$1 ');
    setValue('cardNumber', formatted, { shouldValidate: true, shouldDirty: true });
  };

  /* son kullanım MM/YY auto-format */
  const handleExpiryChange = (e) => {
    const raw = e.target.value.replace(/\D/g, '').slice(0, 4);
    const formatted = raw.length > 2 ? `${raw.slice(0, 2)}/${raw.slice(2)}` : raw;
    setValue('expiry', formatted, { shouldValidate: true, shouldDirty: true });
  };

  /* test kart seç */
  const selectTestCard = (num) => {
    setValue('cardNumber', formatCardDisplay(num), { shouldValidate: true, shouldDirty: true });
    setTestCardsOpen(false);
  };

  const onPay = async (values) => {
    if (amount == null || Number.isNaN(amount) || uid == null || Number.isNaN(uid)) {
      showToast('Sipariş tutarı veya kullanıcı bilgisi eksik', 'error');
      return;
    }

    const expiryParts = (values.expiry || '').split('/');
    const expireMonth = expiryParts[0]?.trim().padStart(2, '0') || '';
    const expireYear = expiryParts[1]?.trim().length === 2
      ? `20${expiryParts[1].trim()}`
      : expiryParts[1]?.trim() || '';

    setPaying(true);
    try {
      const { data: raw } = await paymentService.pay({
        orderId: Number(orderId),
        userId: uid,
        amount,
        card: {
          cardHolderName: values.cardHolderName.trim(),
          cardNumber: String(values.cardNumber).replace(/\s/g, ''),
          expireMonth,
          expireYear,
          cvc: String(values.cvc).trim(),
        },
      });

      const outcome = getPaymentOutcome(raw);
      if (!outcome.ok) {
        showToast(outcome.message || 'Ödeme başarısız', 'error');
        return;
      }

      showToast('Ödeme başarılı!', 'success');
      navigate('/checkout/success', { replace: true, state: { orderId: Number(orderId) } });
    } catch (err) {
      showToast(err.message || 'Ödeme işlemi başarısız', 'error');
    } finally {
      setPaying(false);
    }
  };

  /* yükleme / hata durumları */
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

  const brandColor = {
    VISA: 'text-blue-600',
    MC: 'text-red-500',
    TROY: 'text-red-600',
    AMEX: 'text-blue-500',
  };

  return (
    <div className="max-w-5xl mx-auto px-4 py-8">
      {/* Geri butonu */}
      <button
        type="button"
        onClick={() => navigate(-1)}
        className="flex items-center gap-2 text-sm text-gray-500 hover:text-primary mb-8 transition-colors cursor-pointer"
      >
        <HiArrowLeft className="h-4 w-4" />
        Geri
      </button>

      <div className="grid grid-cols-1 lg:grid-cols-5 gap-8">

        {/* ── Sol: Ödeme formu ─────────────────────────────── */}
        <div className="lg:col-span-3 space-y-6">

          {/* Kart önizleme */}
          <CardPreview
            number={cardNumber}
            holderName={cardHolderName}
            expiry={expiry}
            cvcFocused={cvcFocused}
          />

          {/* Test kartları */}
          <div className="bg-white rounded-2xl border border-gray-100 overflow-hidden">
            <button
              type="button"
              onClick={() => setTestCardsOpen(!testCardsOpen)}
              className="w-full flex items-center justify-between px-5 py-4 text-sm font-medium text-gray-700 hover:bg-gray-50 transition-colors cursor-pointer"
            >
              <span className="flex items-center gap-2">
                <span className="text-base">🧪</span>
                Iyzico Test Kartları
              </span>
              {testCardsOpen ? (
                <HiChevronUp className="h-4 w-4 text-gray-400" />
              ) : (
                <HiChevronDown className="h-4 w-4 text-gray-400" />
              )}
            </button>

            {testCardsOpen && (
              <div className="px-5 pb-5 border-t border-gray-100">
                <p className="text-xs text-gray-500 mt-3 mb-3">
                  Tıklayın → kart numarası otomatik dolar. Son kullanım: 12/26, CVC: 000
                </p>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
                  {TEST_CARDS.map(({ brand, number }) => (
                    <button
                      key={number}
                      type="button"
                      onClick={() => selectTestCard(number)}
                      className="flex items-center justify-between px-3 py-2.5 rounded-xl border border-gray-100 hover:border-primary/40 hover:bg-primary/5 transition-colors cursor-pointer group"
                    >
                      <span className={`text-xs font-bold w-10 ${brandColor[brand] || 'text-gray-500'}`}>
                        {brand}
                      </span>
                      <span className="font-mono text-xs text-gray-500 group-hover:text-secondary transition-colors">
                        {formatCardDisplay(number)}
                      </span>
                    </button>
                  ))}
                </div>
              </div>
            )}
          </div>

          {/* Kart formu */}
          <div className="bg-white rounded-2xl border border-gray-100 p-6 space-y-5">
            <h2 className="text-base font-bold text-secondary">Kart Bilgileri</h2>

            <Field label="Kart Sahibinin Adı" error={errors.cardHolderName?.message}>
              <StyledInput
                {...register('cardHolderName', { required: 'Kart üzerindeki isim zorunludur' })}
                error={errors.cardHolderName?.message}
                placeholder="Ad Soyad"
                autoComplete="cc-name"
              />
            </Field>

            <Field label="Kart Numarası" error={errors.cardNumber?.message}>
              <div className="relative">
                <StyledInput
                  {...register('cardNumber', {
                    required: 'Kart numarası zorunludur',
                    validate: (v) => {
                      const d = String(v || '').replace(/\D/g, '');
                      return (d.length >= 12 && d.length <= 19) || 'Geçerli kart numarası girin';
                    },
                  })}
                  onChange={handleCardNumberChange}
                  error={errors.cardNumber?.message}
                  placeholder="0000 0000 0000 0000"
                  autoComplete="cc-number"
                  inputMode="numeric"
                  className="pr-16"
                />
                {detectBrand((cardNumber || '').replace(/\D/g, '')) && (
                  <span className={`absolute right-4 top-1/2 -translate-y-1/2 text-xs font-black ${brandColor[detectBrand((cardNumber || '').replace(/\D/g, ''))] || ''}`}>
                    {detectBrand((cardNumber || '').replace(/\D/g, ''))}
                  </span>
                )}
              </div>
            </Field>

            <div className="grid grid-cols-2 gap-4">
              <Field label="Son Kullanım (AA/YY)" error={errors.expiry?.message}>
                <StyledInput
                  {...register('expiry', {
                    required: 'Son kullanım tarihi zorunludur',
                    validate: (v) => {
                      const [mm, yy] = (v || '').split('/');
                      const month = parseInt(mm, 10);
                      const year = parseInt(yy, 10);
                      if (Number.isNaN(month) || month < 1 || month > 12) return 'Geçerli ay girin';
                      if (Number.isNaN(year) || yy?.length !== 2) return 'Geçerli yıl girin';
                      return true;
                    },
                  })}
                  onChange={handleExpiryChange}
                  error={errors.expiry?.message}
                  placeholder="AA/YY"
                  autoComplete="cc-exp"
                  inputMode="numeric"
                />
              </Field>

              <Field label="CVC / CVV" error={errors.cvc?.message}>
                <StyledInput
                  {...register('cvc', {
                    required: 'CVC zorunludur',
                    minLength: { value: 3, message: 'En az 3 karakter' },
                    maxLength: { value: 4, message: 'En fazla 4 karakter' },
                  })}
                  type="password"
                  error={errors.cvc?.message}
                  placeholder="•••"
                  autoComplete="cc-csc"
                  inputMode="numeric"
                  onFocus={() => setCvcFocused(true)}
                  onBlur={() => setCvcFocused(false)}
                />
              </Field>
            </div>

            <Button type="button" fullWidth size="lg" loading={paying} onClick={handleSubmit(onPay)}>
              <HiLockClosed className="h-4 w-4 mr-2" />
              {formatMoney(amount)} Öde
            </Button>

            {/* Güvenlik rozetleri */}
            <div className="flex items-center justify-center gap-6 pt-2 border-t border-gray-100">
              <div className="flex items-center gap-1.5 text-xs text-gray-400">
                <HiLockClosed className="h-3.5 w-3.5" />
                256-bit SSL
              </div>
              <div className="flex items-center gap-1.5 text-xs text-gray-400">
                <HiShieldCheck className="h-3.5 w-3.5" />
                3D Secure
              </div>
              <div className="flex items-center gap-1.5 text-xs text-gray-400">
                <span className="font-bold text-primary/60">iyzico</span>
              </div>
            </div>
          </div>
        </div>

        {/* ── Sağ: Sipariş özeti ────────────────────────────── */}
        <div className="lg:col-span-2">
          <div className="bg-white rounded-2xl border border-gray-100 p-6 lg:sticky lg:top-24">
            <div className="flex items-center gap-2 mb-5">
              <HiShoppingBag className="h-5 w-5 text-primary" />
              <h2 className="text-base font-bold text-secondary">Sipariş Özeti</h2>
            </div>

            <div className="space-y-1 mb-5">
              <div className="flex justify-between text-sm">
                <span className="text-gray-500">Sipariş no</span>
                <span className="font-mono font-semibold text-secondary">#{orderId}</span>
              </div>
              {order?.items?.length > 0 && (
                <div className="flex justify-between text-sm">
                  <span className="text-gray-500">Ürün sayısı</span>
                  <span className="font-medium text-secondary">
                    {order.items.reduce((s, i) => s + (i.quantity || 1), 0)} adet
                  </span>
                </div>
              )}
              <div className="flex justify-between text-sm">
                <span className="text-gray-500">Kargo</span>
                <span className="font-medium text-success text-xs">Ücretsiz</span>
              </div>
            </div>

            {/* Ürün listesi */}
            {order?.items?.length > 0 && (
              <div className="divide-y divide-gray-50 mb-5 max-h-48 overflow-y-auto -mx-1 px-1">
                {order.items.map((item, idx) => (
                  <div key={`${item.productId}-${idx}`} className="flex gap-3 py-3">
                    <div className="flex-1 min-w-0">
                      <p className="text-xs font-medium text-secondary truncate">{item.productName}</p>
                      <p className="text-xs text-gray-400 mt-0.5">
                        {formatMoney(item.unitPrice)} × {item.quantity}
                      </p>
                    </div>
                    <span className="text-xs font-semibold text-secondary shrink-0">
                      {formatMoney(Number(item.unitPrice) * Number(item.quantity))}
                    </span>
                  </div>
                ))}
              </div>
            )}

            <div className="border-t border-gray-100 pt-4">
              <div className="flex justify-between items-center">
                <span className="text-sm font-semibold text-secondary">Toplam</span>
                <span className="text-2xl font-black text-primary">{formatMoney(amount)}</span>
              </div>
            </div>

            <p className="text-[11px] text-gray-400 mt-4 text-center leading-relaxed">
              Kart bilgileriniz yalnızca bu işlem için iletilir ve tarayıcınızda saklanmaz.
            </p>
          </div>
        </div>

      </div>
    </div>
  );
}
