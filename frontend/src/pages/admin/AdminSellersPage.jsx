import { useState, useEffect, useCallback } from 'react';
import { HiMagnifyingGlass, HiFunnel, HiXMark, HiCheckBadge } from 'react-icons/hi2';
import { sellerService } from '../../services/sellerService';
import { useToast } from '../../context/ToastContext';
import Button from '../../components/ui/Button';
import LoadingSpinner from '../../components/ui/LoadingSpinner';
import ErrorMessage from '../../components/ui/ErrorMessage';

/* ── helpers ──────────────────────────────────────────────────────────────── */

const STATUS_OPTIONS = [
  { value: '',          label: 'Tüm durumlar' },
  { value: 'PENDING',   label: 'Bekliyor' },
  { value: 'APPROVED',  label: 'Onaylı' },
  { value: 'REJECTED',  label: 'Reddedildi' },
  { value: 'SUSPENDED', label: 'Askıda' },
];

const VERIFIED_OPTIONS = [
  { value: '',      label: 'Doğrulama (hepsi)' },
  { value: 'true',  label: 'Doğrulanmış' },
  { value: 'false', label: 'Doğrulanmamış' },
];

const STATUS_BADGE = {
  PENDING:   'bg-amber-100 text-amber-700',
  APPROVED:  'bg-success/15 text-success',
  REJECTED:  'bg-error/15 text-error',
  SUSPENDED: 'bg-gray-200 text-gray-600',
};

const STATUS_LABEL = {
  PENDING:   'Bekliyor',
  APPROVED:  'Onaylı',
  REJECTED:  'Reddedildi',
  SUSPENDED: 'Askıda',
};

const PAGE_SIZE = 10;

/* ── component ────────────────────────────────────────────────────────────── */

export default function AdminSellersPage() {
  const { showToast } = useToast();

  // filter state
  const [storeName, setStoreName]   = useState('');
  const [status, setStatus]         = useState('');
  const [verified, setVerified]     = useState('');

  // applied filters (committed on search)
  const [applied, setApplied] = useState({ storeName: '', status: '', verified: '' });

  // data state
  const [content, setContent]       = useState([]);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [page, setPage]             = useState(0);
  const [loading, setLoading]       = useState(true);
  const [error, setError]           = useState(null);
  const [acting, setActing]         = useState(null);

  /* fetch */
  const fetchList = useCallback(async (filters, pageNum, showSpinner = true) => {
    if (showSpinner) setLoading(true);
    setError(null);
    try {
      const { data: rest } = await sellerService.getAllSellers({
        storeName: filters.storeName || undefined,
        status:    filters.status    || undefined,
        verified:  filters.verified !== '' ? filters.verified === 'true' : undefined,
        page: pageNum,
        size: PAGE_SIZE,
      });
      const pg = rest.data;
      setContent(pg?.content || []);
      setTotalPages(pg?.totalPages ?? 0);
      setTotalElements(pg?.totalElements ?? 0);
    } catch (err) {
      setError(err.message || 'Satıcı listesi yüklenemedi');
    } finally {
      if (showSpinner) setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchList(applied, page);
  }, [fetchList, applied, page]);

  /* search */
  const handleSearch = (e) => {
    e?.preventDefault();
    const next = { storeName, status, verified };
    setApplied(next);
    setPage(0);
  };

  const handleReset = () => {
    setStoreName('');
    setStatus('');
    setVerified('');
    setApplied({ storeName: '', status: '', verified: '' });
    setPage(0);
  };

  const hasActiveFilter = applied.storeName || applied.status || applied.verified !== '';

  /* status update */
  const updateStatus = async (userId, newStatus) => {
    setActing({ id: userId, status: newStatus });
    try {
      await sellerService.updateApplicationStatus(userId, newStatus);
      const label = newStatus === 'APPROVED' ? 'Onaylandı' : newStatus === 'SUSPENDED' ? 'Askıya alındı' : 'Güncellendi';
      showToast(label, 'success');
      fetchList(applied, page, false);
    } catch (err) {
      showToast(err.message || 'İşlem başarısız', 'error');
    } finally {
      setActing(null);
    }
  };

  /* render */
  if (loading) return <LoadingSpinner size="lg" />;
  if (error)   return <ErrorMessage message={error} onRetry={() => fetchList(applied, page)} />;

  return (
    <div className="space-y-5">

      {/* Header */}
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h2 className="text-2xl font-bold text-secondary">Satıcılar</h2>
          <p className="text-sm text-gray-500 mt-0.5">
            {totalElements} satıcı kayıtlı
            {hasActiveFilter && <span className="ml-1 text-primary font-medium">· filtre uygulandı</span>}
          </p>
        </div>
      </div>

      {/* Filter bar */}
      <form
        onSubmit={handleSearch}
        className="bg-white rounded-xl border border-gray-100 p-4 flex flex-wrap gap-3 items-end"
      >
        {/* Store name search */}
        <div className="flex-1 min-w-48">
          <label className="block text-xs font-medium text-gray-500 mb-1.5">Mağaza adı</label>
          <div className="relative">
            <HiMagnifyingGlass className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-400 pointer-events-none" />
            <input
              type="text"
              value={storeName}
              onChange={(e) => setStoreName(e.target.value)}
              placeholder="Mağaza ara..."
              className="w-full pl-9 pr-3 py-2.5 border border-gray-200 rounded-lg text-sm text-secondary placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:ring-primary/30 focus:border-primary"
            />
          </div>
        </div>

        {/* Status filter */}
        <div className="min-w-40">
          <label className="block text-xs font-medium text-gray-500 mb-1.5">Durum</label>
          <select
            value={status}
            onChange={(e) => setStatus(e.target.value)}
            className="w-full px-3 py-2.5 border border-gray-200 rounded-lg text-sm text-secondary focus:outline-none focus:ring-2 focus:ring-primary/30 focus:border-primary bg-white"
          >
            {STATUS_OPTIONS.map((o) => (
              <option key={o.value} value={o.value}>{o.label}</option>
            ))}
          </select>
        </div>

        {/* Verified filter */}
        <div className="min-w-44">
          <label className="block text-xs font-medium text-gray-500 mb-1.5">Doğrulama</label>
          <select
            value={verified}
            onChange={(e) => setVerified(e.target.value)}
            className="w-full px-3 py-2.5 border border-gray-200 rounded-lg text-sm text-secondary focus:outline-none focus:ring-2 focus:ring-primary/30 focus:border-primary bg-white"
          >
            {VERIFIED_OPTIONS.map((o) => (
              <option key={o.value} value={o.value}>{o.label}</option>
            ))}
          </select>
        </div>

        <div className="flex gap-2">
          <Button type="submit" size="sm">
            <HiFunnel className="h-4 w-4 mr-1.5" />
            Filtrele
          </Button>
          {hasActiveFilter && (
            <Button type="button" size="sm" variant="outline" onClick={handleReset}>
              <HiXMark className="h-4 w-4 mr-1" />
              Temizle
            </Button>
          )}
        </div>
      </form>

      {/* Table */}
      {content.length === 0 ? (
        <div className="bg-white rounded-xl border border-gray-100 p-12 text-center text-gray-500">
          {hasActiveFilter
            ? 'Bu filtrelere uyan satıcı bulunamadı.'
            : 'Henüz kayıtlı satıcı yok.'}
        </div>
      ) : (
        <div className="bg-white rounded-xl border border-gray-100 overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="bg-gray-50 border-b border-gray-100">
                  <th className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wide">Mağaza</th>
                  <th className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wide hidden md:table-cell">Şirket</th>
                  <th className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wide">Durum</th>
                  <th className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wide hidden sm:table-cell">Doğrulama</th>
                  <th className="px-4 py-3 text-right text-xs font-semibold text-gray-500 uppercase tracking-wide">İşlem</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-50">
                {content.map((row) => {
                  const st = (row.status || '').toUpperCase();
                  return (
                    <tr key={row.userId} className="hover:bg-gray-50/60 transition-colors">
                      <td className="px-4 py-3">
                        <p className="font-semibold text-secondary">{row.storeName}</p>
                        <p className="text-xs text-gray-400 font-mono mt-0.5">UID: {row.userId}</p>
                      </td>
                      <td className="px-4 py-3 text-sm text-gray-600 hidden md:table-cell max-w-48 truncate">
                        {row.companyName || '—'}
                      </td>
                      <td className="px-4 py-3">
                        <span className={`inline-flex px-2.5 py-1 rounded-full text-xs font-semibold ${STATUS_BADGE[st] || 'bg-gray-100 text-gray-500'}`}>
                          {STATUS_LABEL[st] || st}
                        </span>
                      </td>
                      <td className="px-4 py-3 hidden sm:table-cell">
                        {row.verified ? (
                          <span className="inline-flex items-center gap-1 text-xs font-semibold text-success">
                            <HiCheckBadge className="h-4 w-4" />
                            Doğrulandı
                          </span>
                        ) : (
                          <span className="text-xs text-gray-400">—</span>
                        )}
                      </td>
                      <td className="px-4 py-3 text-right">
                        <div className="flex flex-wrap items-center justify-end gap-2">
                          {st === 'APPROVED' && (
                            <Button
                              size="sm"
                              variant="secondary"
                              disabled={acting != null}
                              loading={acting?.id === row.userId && acting?.status === 'SUSPENDED'}
                              onClick={() => updateStatus(row.userId, 'SUSPENDED')}
                            >
                              Askıya Al
                            </Button>
                          )}
                          {(st === 'SUSPENDED' || st === 'REJECTED' || st === 'PENDING') && (
                            <Button
                              size="sm"
                              variant="success"
                              disabled={acting != null}
                              loading={acting?.id === row.userId && acting?.status === 'APPROVED'}
                              onClick={() => updateStatus(row.userId, 'APPROVED')}
                            >
                              Onayla
                            </Button>
                          )}
                          {st === 'APPROVED' && (
                            <Button
                              size="sm"
                              variant="danger"
                              disabled={acting != null}
                              loading={acting?.id === row.userId && acting?.status === 'REJECTED'}
                              onClick={() => updateStatus(row.userId, 'REJECTED')}
                            >
                              Reddet
                            </Button>
                          )}
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>

          {/* Pagination */}
          {totalPages > 1 && (
            <div className="px-4 py-3 border-t border-gray-100 flex items-center justify-between gap-4">
              <p className="text-xs text-gray-500">
                Sayfa {page + 1} / {totalPages}
                <span className="ml-2 text-gray-400">({totalElements} kayıt)</span>
              </p>
              <div className="flex gap-1.5">
                <button
                  type="button"
                  onClick={() => setPage((p) => Math.max(0, p - 1))}
                  disabled={page === 0}
                  className="px-3 py-1.5 text-xs font-medium rounded-lg border border-gray-200 text-gray-600 disabled:opacity-40 disabled:cursor-not-allowed hover:bg-gray-50 transition-colors"
                >
                  ← Önceki
                </button>
                {Array.from({ length: totalPages }, (_, i) => i)
                  .filter((i) => Math.abs(i - page) <= 2)
                  .map((i) => (
                    <button
                      key={i}
                      type="button"
                      onClick={() => setPage(i)}
                      className={`px-3 py-1.5 text-xs font-semibold rounded-lg border transition-colors ${
                        i === page
                          ? 'border-primary bg-primary text-white'
                          : 'border-gray-200 text-gray-600 hover:bg-gray-50'
                      }`}
                    >
                      {i + 1}
                    </button>
                  ))}
                <button
                  type="button"
                  onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
                  disabled={page >= totalPages - 1}
                  className="px-3 py-1.5 text-xs font-medium rounded-lg border border-gray-200 text-gray-600 disabled:opacity-40 disabled:cursor-not-allowed hover:bg-gray-50 transition-colors"
                >
                  Sonraki →
                </button>
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
