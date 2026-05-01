import { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { sellerService } from '../../services/sellerService';
import { useToast } from '../../context/ToastContext';
import Button from '../../components/ui/Button';
import LoadingSpinner from '../../components/ui/LoadingSpinner';
import ErrorMessage from '../../components/ui/ErrorMessage';

function statusBadge(status) {
  const s = (status || '').toUpperCase();
  if (s === 'PENDING') return 'bg-accent-yellow/40 text-secondary';
  if (s === 'APPROVED') return 'bg-success/20 text-success';
  if (s === 'REJECTED') return 'bg-error/15 text-error';
  if (s === 'SUSPENDED') return 'bg-gray-200 text-gray-700';
  return 'bg-gray-100 text-gray-600';
}

export default function AdminSellersPage() {
  const { showToast } = useToast();
  const [list, setList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [acting, setActing] = useState(null);

  const fetchList = useCallback(async ({ showSpinner = true } = {}) => {
    if (showSpinner) setLoading(true);
    setError(null);
    try {
      const { data: rest } = await sellerService.getAllSellerProfiles();
      setList(rest.data || []);
    } catch (err) {
      setError(err.message || 'Satıcı listesi yüklenemedi');
    } finally {
      if (showSpinner) setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchList({ showSpinner: true });
  }, [fetchList]);

  const setStatus = async (sellerProfileId, status) => {
    setActing({ id: sellerProfileId, status });
    try {
      await sellerService.updateApplicationStatus(sellerProfileId, status);
      showToast(
        status === 'APPROVED' ? 'Satıcı onaylı (aktif) olarak güncellendi' : 'Satıcı askıya alındı',
        'success'
      );
      await fetchList({ showSpinner: false });
    } catch (err) {
      showToast(err.message || 'İşlem başarısız', 'error');
    } finally {
      setActing(null);
    }
  };

  if (loading) return <LoadingSpinner size="lg" />;
  if (error) return <ErrorMessage message={error} onRetry={() => fetchList({ showSpinner: true })} />;

  return (
    <div>
      <h2 className="text-2xl font-bold text-secondary mb-2">Satıcılar</h2>
      <p className="text-sm text-gray-500 mb-2">
        Kayıtlı satıcı profilleri. Bu ekranda yalnızca durum{' '}
        <strong className="text-secondary">APPROVED</strong> (onaylı) veya{' '}
        <strong className="text-secondary">SUSPENDED</strong> (askıda) olarak güncellenir; reddetme veya bekleyen
        başvuru işlemleri için{' '}
        <Link to="/admin/seller-applications" className="text-primary font-medium hover:underline">
          Satıcı başvuruları
        </Link>{' '}
        sayfasını kullanın.
      </p>

      {list.length === 0 ? (
        <div className="bg-white rounded-xl border border-gray-100 p-10 text-center text-gray-500 mt-6">
          Satıcı kaydı bulunamadı.
        </div>
      ) : (
        <div className="mt-6 overflow-x-auto rounded-xl border border-gray-100 bg-white">
          <table className="min-w-full text-sm">
            <thead className="bg-gray-50 text-left text-xs font-semibold text-gray-600 uppercase tracking-wide">
              <tr>
                <th className="px-4 py-3">Mağaza</th>
                <th className="px-4 py-3 hidden md:table-cell">Şirket</th>
                <th className="px-4 py-3">Durum</th>
                <th className="px-4 py-3 text-right">İşlem</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {list.map((row) => {
                const st = (row.status || '').toUpperCase();
                const showSuspend = st === 'APPROVED';
                const showApprove = st === 'SUSPENDED' || st === 'REJECTED';
                const pendingHint = st === 'PENDING';

                return (
                  <tr key={row.id} className="hover:bg-gray-50/80">
                    <td className="px-4 py-3">
                      <p className="font-semibold text-secondary">{row.storeName}</p>
                      <p className="text-xs text-gray-500 font-mono mt-0.5">ID: {row.id} · Kullanıcı: {row.userId}</p>
                    </td>
                    <td className="px-4 py-3 text-gray-600 hidden md:table-cell max-w-xs truncate">
                      {row.companyName}
                    </td>
                    <td className="px-4 py-3">
                      <span className={`text-xs font-semibold px-2 py-0.5 rounded ${statusBadge(row.status)}`}>
                        {row.status}
                      </span>
                    </td>
                    <td className="px-4 py-3 text-right">
                      <div className="flex flex-wrap items-center justify-end gap-2">
                        {pendingHint && (
                          <span className="text-xs text-gray-500">Başvurular sayfasında</span>
                        )}
                        {showSuspend && (
                          <Button
                            size="sm"
                            variant="secondary"
                            disabled={acting != null}
                            loading={acting?.id === row.id && acting?.status === 'SUSPENDED'}
                            onClick={() => setStatus(row.id, 'SUSPENDED')}
                          >
                            Askıya al
                          </Button>
                        )}
                        {showApprove && (
                          <Button
                            size="sm"
                            variant="success"
                            disabled={acting != null}
                            loading={acting?.id === row.id && acting?.status === 'APPROVED'}
                            onClick={() => setStatus(row.id, 'APPROVED')}
                          >
                            Onaylı yap
                          </Button>
                        )}
                        {!showSuspend && !showApprove && !pendingHint && (
                          <span className="text-xs text-gray-400">—</span>
                        )}
                      </div>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
