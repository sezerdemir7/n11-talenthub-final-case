import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import {
  HiShoppingBag,
  HiBuildingStorefront,
  HiArrowRight,
  HiCheckCircle,
  HiClock,
  HiNoSymbol,
  HiPauseCircle,
} from 'react-icons/hi2';
import { sellerService } from '../../services/sellerService';
import LoadingSpinner from '../../components/ui/LoadingSpinner';

const STATUS_CONFIG = {
  APPROVED: {
    label: 'Aktif',
    icon: HiCheckCircle,
    badge: 'bg-success/15 text-success',
    hint: 'Mağazanız aktif — ürün ekleyip satış yapabilirsiniz.',
    hintColor: 'text-success',
  },
  PENDING: {
    label: 'Onay Bekliyor',
    icon: HiClock,
    badge: 'bg-amber-100 text-amber-700',
    hint: 'Başvurunuz inceleniyor. Onaydan sonra ürün ekleyebilirsiniz.',
    hintColor: 'text-amber-700',
  },
  REJECTED: {
    label: 'Reddedildi',
    icon: HiNoSymbol,
    badge: 'bg-error/15 text-error',
    hint: 'Başvurunuz reddedildi. Destek ekibiyle iletişime geçin.',
    hintColor: 'text-error',
  },
  SUSPENDED: {
    label: 'Askıya Alındı',
    icon: HiPauseCircle,
    badge: 'bg-gray-200 text-gray-600',
    hint: 'Hesabınız askıya alınmış. Destek ekibiyle iletişime geçin.',
    hintColor: 'text-gray-600',
  },
};

export default function SellerDashboard() {
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      try {
        const { data: rest } = await sellerService.getMyProfile();
        setProfile(rest.data);
      } catch {
        setProfile(null);
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  if (loading) return <LoadingSpinner size="lg" />;

  const cfg = profile ? (STATUS_CONFIG[profile.status] ?? STATUS_CONFIG.PENDING) : null;
  const StatusIcon = cfg?.icon;
  const isApproved = profile?.status === 'APPROVED';

  return (
    <div className="space-y-6">
      {/* Welcome bar */}
      <div className="bg-white rounded-xl border border-gray-100 p-6">
        <div className="flex flex-wrap items-center justify-between gap-4">
          <div>
            <h2 className="text-xl font-bold text-secondary">
              {profile?.storeName ? `Merhaba, ${profile.storeName}` : 'Satıcı Paneline Hoş Geldiniz'}
            </h2>
            {profile?.companyName && (
              <p className="text-sm text-gray-500 mt-0.5">{profile.companyName}</p>
            )}
          </div>
          {cfg && (
            <span className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full text-sm font-semibold ${cfg.badge}`}>
              <StatusIcon className="h-4 w-4" />
              {cfg.label}
            </span>
          )}
        </div>
        {cfg && (
          <p className={`text-sm mt-3 ${cfg.hintColor}`}>{cfg.hint}</p>
        )}
      </div>

      {/* Quick action cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
        {/* Mağaza */}
        <div className="bg-white rounded-xl border border-gray-100 p-6 flex flex-col">
          <div className="h-11 w-11 rounded-xl bg-primary/10 flex items-center justify-center mb-4">
            <HiBuildingStorefront className="h-6 w-6 text-primary" />
          </div>
          <h3 className="font-semibold text-secondary">Mağaza Bilgileri</h3>
          <p className="text-sm text-gray-500 mt-1.5 flex-1">
            Mağaza adı, şirket ünvanı ve açıklamanızı güncelleyin.
          </p>
          <Link
            to="/seller/store"
            className="mt-5 inline-flex items-center gap-1.5 text-sm font-semibold text-primary hover:gap-2.5 transition-all"
          >
            Mağazamı Düzenle
            <HiArrowRight className="h-4 w-4" />
          </Link>
        </div>

        {/* Ürünler */}
        <div className={`bg-white rounded-xl border p-6 flex flex-col ${isApproved ? 'border-gray-100' : 'border-gray-100 opacity-70'}`}>
          <div className="h-11 w-11 rounded-xl bg-primary/10 flex items-center justify-center mb-4">
            <HiShoppingBag className="h-6 w-6 text-primary" />
          </div>
          <h3 className="font-semibold text-secondary">Ürün Yönetimi</h3>
          <p className="text-sm text-gray-500 mt-1.5 flex-1">
            {isApproved
              ? 'Yeni ürün ekleyin, mevcut ürünlerinizi düzenleyin.'
              : 'Mağazanız onaylandıktan sonra ürün ekleyebilirsiniz.'}
          </p>
          {isApproved ? (
            <Link
              to="/seller/products"
              className="mt-5 inline-flex items-center gap-1.5 text-sm font-semibold text-primary hover:gap-2.5 transition-all"
            >
              Ürünleri Yönet
              <HiArrowRight className="h-4 w-4" />
            </Link>
          ) : (
            <span className="mt-5 text-sm text-gray-400">Kullanılamıyor</span>
          )}
        </div>
      </div>

      {/* Info strip */}
      {!profile && (
        <div className="bg-amber-50 border border-amber-200 rounded-xl p-5">
          <p className="text-sm text-amber-800 font-medium">
            Satıcı profili yüklenemedi. Sayfayı yenilemeyi deneyin.
          </p>
        </div>
      )}
    </div>
  );
}
