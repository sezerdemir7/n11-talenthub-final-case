import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { HiShoppingBag, HiBuildingStorefront, HiArrowRight } from 'react-icons/hi2';
import { useAuth } from '../../context/AuthContext';
import { sellerService } from '../../services/sellerService';
import LoadingSpinner from '../../components/ui/LoadingSpinner';

function statusLabel(status) {
  const map = {
    PENDING: { text: 'Onay bekliyor', className: 'bg-accent-yellow/40 text-secondary' },
    APPROVED: { text: 'Onaylandı', className: 'bg-success/20 text-success' },
    REJECTED: { text: 'Reddedildi', className: 'bg-error/15 text-error' },
    SUSPENDED: { text: 'Askıda', className: 'bg-gray-200 text-gray-700' },
  };
  return map[status] || { text: status || '—', className: 'bg-gray-100 text-gray-600' };
}

export default function SellerDashboard() {
  const { user } = useAuth();
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const load = async () => {
      if (!user?.userId) {
        setLoading(false);
        return;
      }
      try {
        const { data: rest } = await sellerService.getMyProfile(user.userId);
        setProfile(rest.data);
      } catch {
        setProfile(null);
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [user?.userId]);

  if (loading) return <LoadingSpinner size="lg" />;

  const st = profile ? statusLabel(profile.status) : null;

  return (
    <div>
      <h2 className="text-2xl font-bold text-secondary mb-6">Satıcı Özeti</h2>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div className="bg-white rounded-xl border border-gray-100 p-6">
          <div className="flex items-center gap-3 mb-4">
            <div className="h-12 w-12 rounded-lg bg-primary/10 flex items-center justify-center">
              <HiBuildingStorefront className="h-6 w-6 text-primary" />
            </div>
            {profile && (
              <span className={`text-xs font-bold px-2.5 py-1 rounded ${st.className}`}>{st.text}</span>
            )}
          </div>
          <h3 className="font-semibold text-secondary">Mağaza durumu</h3>
          {profile ? (
            <p className="text-sm text-gray-600 mt-2">
              <strong>{profile.storeName}</strong>
              {profile.status === 'PENDING' && (
                <span className="block mt-2 text-amber-700">
                  Başvurunuz yönetici onayından sonra ürün ekleyebilirsiniz.
                </span>
              )}
              {profile.status === 'APPROVED' && (
                <span className="block mt-2 text-success">Hesabınız aktif — ürün ekleyebilirsiniz.</span>
              )}
            </p>
          ) : (
            <p className="text-sm text-gray-500 mt-2">Profil yüklenemedi veya henüz oluşturulmadı.</p>
          )}
          <Link
            to="/seller/store"
            className="inline-flex items-center gap-1 text-sm text-primary font-medium mt-4 hover:underline"
          >
            Mağaza bilgileri <HiArrowRight className="h-4 w-4" />
          </Link>
        </div>

        <div className="bg-white rounded-xl border border-gray-100 p-6">
          <div className="h-12 w-12 rounded-lg bg-primary/10 flex items-center justify-center mb-4">
            <HiShoppingBag className="h-6 w-6 text-primary" />
          </div>
          <h3 className="font-semibold text-secondary">Ürünler</h3>
          <p className="text-sm text-gray-500 mt-1">Ürün oluşturma ve düzenleme</p>
          <Link
            to="/seller/products"
            className="inline-flex items-center gap-1 text-sm text-primary font-medium mt-4 hover:underline"
          >
            Ürün yönetimi <HiArrowRight className="h-4 w-4" />
          </Link>
        </div>
      </div>
    </div>
  );
}
