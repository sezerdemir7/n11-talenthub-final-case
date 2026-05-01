import { useState, useEffect, useCallback } from 'react';
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

export default function AdminSellerApplications() {
  const { showToast } = useToast();
  const [list, setList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [acting, setActing] = useState(null);

  const fetchList = useCallback(async ({ showSpinner = true } = {}) => {
    if (showSpinner) {
      setLoading(true);
    }
    setError(null);
    try {
      const { data: rest } = await sellerService.getPendingApplications();
      setList(rest.data || []);
    } catch (err) {
      setError(err.message || 'Liste yüklenemedi');
    } finally {
      if (showSpinner) {
        setLoading(false);
      }
    }
  }, []);

  useEffect(() => {
    fetchList({ showSpinner: true });
  }, [fetchList]);

  const handleStatus = async (sellerProfileId, status) => {
    setActing({ id: sellerProfileId, status });
    try {
      await sellerService.updateApplicationStatus(sellerProfileId, status);
      showToast(
        status === 'APPROVED' ? 'Başvuru onaylandı' : 'Başvuru reddedildi',
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
  if (error) return <ErrorMessage message={error} onRetry={fetchList} />;

  return (
    <div>
      <h2 className="text-2xl font-bold text-secondary mb-2">Satıcı başvuruları</h2>
      <p className="text-sm text-gray-500 mb-6">
        Bekleyen başvuruları onaylayın veya reddedin. Durum güncellemesi için{' '}
        <code className="text-xs bg-gray-100 px-1 rounded">PATCH /v1/sellers/&#123;id&#125;/status</code>
      </p>

      {list.length === 0 ? (
        <div className="bg-white rounded-xl border border-gray-100 p-10 text-center text-gray-500">
          Bekleyen başvuru yok.
        </div>
      ) : (
        <div className="space-y-4">
          {list.map((row) => (
            <div
              key={row.id}
              className="bg-white rounded-xl border border-gray-100 p-6 flex flex-col lg:flex-row lg:items-center lg:justify-between gap-4"
            >
              <div className="min-w-0">
                <div className="flex flex-wrap items-center gap-2 mb-1">
                  <span className="font-bold text-secondary text-lg">{row.storeName}</span>
                  <span className={`text-xs font-semibold px-2 py-0.5 rounded ${statusBadge(row.status)}`}>
                    {row.status}
                  </span>
                </div>
                <p className="text-sm text-gray-600">{row.companyName}</p>
                <p className="text-xs text-gray-500 mt-1 font-mono">VKN: {row.taxNumber}</p>
                {row.storeDescription && (
                  <p className="text-sm text-gray-500 mt-2 line-clamp-2">{row.storeDescription}</p>
                )}
                <p className="text-xs text-gray-400 mt-2">Kullanıcı ID: {row.userId}</p>
              </div>
              <div className="flex flex-wrap gap-2 shrink-0">
                <Button
                  size="sm"
                  variant="success"
                  loading={acting?.id === row.id && acting?.status === 'APPROVED'}
                  disabled={acting != null}
                  onClick={() => handleStatus(row.id, 'APPROVED')}
                >
                  Onayla
                </Button>
                <Button
                  size="sm"
                  variant="danger"
                  loading={acting?.id === row.id && acting?.status === 'REJECTED'}
                  disabled={acting != null}
                  onClick={() => handleStatus(row.id, 'REJECTED')}
                >
                  Reddet
                </Button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
