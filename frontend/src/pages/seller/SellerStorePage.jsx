import { useState, useEffect, useCallback } from 'react';
import { useForm } from 'react-hook-form';
import {
  HiBuildingStorefront,
  HiCheckCircle,
  HiClock,
  HiNoSymbol,
  HiPauseCircle,
  HiPencilSquare,
} from 'react-icons/hi2';
import { useToast } from '../../context/ToastContext';
import { sellerService } from '../../services/sellerService';
import Input from '../../components/ui/Input';
import Button from '../../components/ui/Button';
import LoadingSpinner from '../../components/ui/LoadingSpinner';
import ErrorMessage from '../../components/ui/ErrorMessage';

const STATUS_CONFIG = {
  APPROVED:  { label: 'Aktif',           icon: HiCheckCircle, cls: 'bg-success/15 text-success' },
  PENDING:   { label: 'Onay Bekliyor',   icon: HiClock,       cls: 'bg-amber-100 text-amber-700' },
  REJECTED:  { label: 'Reddedildi',      icon: HiNoSymbol,    cls: 'bg-error/15 text-error' },
  SUSPENDED: { label: 'Askıya Alındı',   icon: HiPauseCircle, cls: 'bg-gray-200 text-gray-600' },
};

const DESC_MAX = 300;

export default function SellerStorePage() {
  const { showToast } = useToast();
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [saved, setSaved] = useState(false);

  const {
    register,
    handleSubmit,
    reset,
    watch,
    formState: { errors, isDirty },
  } = useForm();

  const descValue = watch('storeDescription') || '';

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const { data: rest } = await sellerService.getMyProfile();
      const p = rest.data;
      setProfile(p);
      reset({
        storeName: p.storeName || '',
        companyName: p.companyName || '',
        storeDescription: p.storeDescription || '',
      });
    } catch (err) {
      setError(err.message || 'Profil yüklenemedi');
    } finally {
      setLoading(false);
    }
  }, [reset]);

  useEffect(() => { load(); }, [load]);

  const onSubmit = async (data) => {
    setSubmitting(true);
    setSaved(false);
    try {
      await sellerService.updateMyProfile({
        storeName: data.storeName.trim(),
        companyName: data.companyName.trim(),
        storeDescription: data.storeDescription?.trim() || '',
      });
      showToast('Mağaza bilgileri güncellendi', 'success');
      setSaved(true);
      load();
    } catch (err) {
      showToast(err.message || 'Güncelleme başarısız', 'error');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return <LoadingSpinner size="lg" />;
  if (error) return <ErrorMessage message={error} onRetry={load} />;

  const cfg = profile ? (STATUS_CONFIG[profile.status] ?? STATUS_CONFIG.PENDING) : null;
  const StatusIcon = cfg?.icon;
  const initials = (profile?.storeName || profile?.companyName || 'M').charAt(0).toUpperCase();

  return (
    <div className="max-w-2xl space-y-5">

      {/* Store preview card */}
      <div className="bg-white rounded-xl border border-gray-100 p-6">
        <div className="flex items-start gap-4">
          <div className="h-16 w-16 rounded-2xl bg-primary/10 flex items-center justify-center shrink-0 text-2xl font-black text-primary">
            {initials}
          </div>
          <div className="flex-1 min-w-0">
            <div className="flex flex-wrap items-center gap-2 mb-0.5">
              <h2 className="text-lg font-bold text-secondary truncate">
                {profile?.storeName || '—'}
              </h2>
              {cfg && (
                <span className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-semibold shrink-0 ${cfg.cls}`}>
                  <StatusIcon className="h-3 w-3" />
                  {cfg.label}
                </span>
              )}
            </div>
            <p className="text-sm text-gray-500 truncate">{profile?.companyName || '—'}</p>
            {profile?.storeDescription && (
              <p className="text-sm text-gray-400 mt-2 line-clamp-2">{profile.storeDescription}</p>
            )}
          </div>
          <div className="shrink-0">
            <span className="inline-flex items-center gap-1 text-xs text-gray-400">
              <HiPencilSquare className="h-3.5 w-3.5" />
              Düzenleniyor
            </span>
          </div>
        </div>
      </div>

      {/* Form */}
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">

        {/* Mağaza Bilgileri */}
        <div className="bg-white rounded-xl border border-gray-100 p-6 space-y-4">
          <div className="flex items-center gap-2 mb-1">
            <HiBuildingStorefront className="h-4 w-4 text-gray-400" />
            <h3 className="text-xs font-semibold text-gray-400 uppercase tracking-wide">Mağaza Bilgileri</h3>
          </div>

          <Input
            label="Mağaza adı"
            {...register('storeName', { required: 'Mağaza adı zorunludur' })}
            error={errors.storeName?.message}
            placeholder="Örn: TechStore Türkiye"
          />

          <div>
            <div className="flex items-center justify-between mb-1.5">
              <label className="block text-sm font-medium text-secondary">
                Mağaza açıklaması
                <span className="text-gray-400 font-normal ml-1">(isteğe bağlı)</span>
              </label>
              <span className={`text-xs ${descValue.length > DESC_MAX ? 'text-error font-semibold' : 'text-gray-400'}`}>
                {descValue.length} / {DESC_MAX}
              </span>
            </div>
            <textarea
              rows={4}
              {...register('storeDescription', {
                maxLength: { value: DESC_MAX, message: `En fazla ${DESC_MAX} karakter` },
              })}
              className={`w-full px-4 py-2.5 border rounded-lg text-secondary placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:ring-primary/30 focus:border-primary resize-none ${
                errors.storeDescription ? 'border-error' : 'border-gray-300'
              }`}
              placeholder="Mağazanızı ve sattığınız ürünleri kısaca tanıtın..."
            />
            {errors.storeDescription && (
              <p className="mt-1 text-sm text-error">{errors.storeDescription.message}</p>
            )}
          </div>
        </div>

        {/* Şirket Bilgileri */}
        <div className="bg-white rounded-xl border border-gray-100 p-6 space-y-4">
          <h3 className="text-xs font-semibold text-gray-400 uppercase tracking-wide mb-1">Şirket Bilgileri</h3>

          <Input
            label="Şirket ünvanı"
            {...register('companyName', { required: 'Şirket ünvanı zorunludur' })}
            error={errors.companyName?.message}
            placeholder="Örn: Teknoloji A.Ş."
          />

          {profile?.taxNumber && (
            <div>
              <label className="block text-sm font-medium text-secondary mb-1.5">Vergi numarası</label>
              <div className="px-4 py-2.5 border border-gray-200 rounded-lg bg-gray-50 text-sm text-gray-500 font-mono select-all">
                {profile.taxNumber}
              </div>
              <p className="mt-1 text-xs text-gray-400">
                Vergi numarası değişikliği için destek ekibiyle iletişime geçin.
              </p>
            </div>
          )}
        </div>

        {/* Actions */}
        <div className="bg-white rounded-xl border border-gray-100 px-6 py-4 flex items-center justify-between">
          {saved && !isDirty ? (
            <span className="inline-flex items-center gap-1.5 text-sm text-success font-medium">
              <HiCheckCircle className="h-4 w-4" />
              Değişiklikler kaydedildi
            </span>
          ) : (
            <span className="text-xs text-gray-400">
              {isDirty ? 'Kaydedilmemiş değişiklikler var.' : 'Tüm değişiklikler kaydedildi.'}
            </span>
          )}
          <Button
            type="submit"
            loading={submitting}
            disabled={!isDirty || descValue.length > DESC_MAX}
          >
            Kaydet
          </Button>
        </div>
      </form>
    </div>
  );
}
