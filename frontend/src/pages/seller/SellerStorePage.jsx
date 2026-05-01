import { useState, useEffect, useCallback } from 'react';
import { useForm } from 'react-hook-form';
import { useAuth } from '../../context/AuthContext';
import { useToast } from '../../context/ToastContext';
import { sellerService } from '../../services/sellerService';
import Input from '../../components/ui/Input';
import Button from '../../components/ui/Button';
import LoadingSpinner from '../../components/ui/LoadingSpinner';
import ErrorMessage from '../../components/ui/ErrorMessage';

export default function SellerStorePage() {
  const { user } = useAuth();
  const { showToast } = useToast();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  const { register, handleSubmit, reset, formState: { errors } } = useForm();

  const load = useCallback(async () => {
    if (!user?.userId) return;
    setLoading(true);
    setError(null);
    try {
      const { data: rest } = await sellerService.getMyProfile(user.userId);
      const p = rest.data;
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
  }, [user?.userId, reset]);

  useEffect(() => {
    load();
  }, [load]);

  const onSubmit = async (data) => {
    if (!user?.userId) return;
    setSubmitting(true);
    try {
      await sellerService.updateMyProfile(user.userId, {
        storeName: data.storeName.trim(),
        companyName: data.companyName.trim(),
        storeDescription: data.storeDescription?.trim() || '',
      });
      showToast('Mağaza bilgileri güncellendi', 'success');
      load();
    } catch (err) {
      showToast(err.message || 'Güncelleme başarısız', 'error');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return <LoadingSpinner size="lg" />;
  if (error) return <ErrorMessage message={error} onRetry={load} />;

  return (
    <div>
      <h2 className="text-2xl font-bold text-secondary mb-2">Mağazam</h2>
      <p className="text-sm text-gray-500 mb-6">
        Mağaza adı, şirket ve açıklama bilgilerinizi güncelleyin. Vergi numarası kayıtta sabittir; değişiklik için
        destek ile iletişime geçin.
      </p>

      <form
        onSubmit={handleSubmit(onSubmit)}
        className="max-w-xl space-y-4 bg-white rounded-xl border border-gray-100 p-6 md:p-8"
      >
        <Input
          label="Mağaza adı"
          {...register('storeName', { required: 'Mağaza adı zorunludur' })}
          error={errors.storeName?.message}
        />
        <Input
          label="Şirket ünvanı"
          {...register('companyName', { required: 'Şirket ünvanı zorunludur' })}
          error={errors.companyName?.message}
        />
        <div className="w-full">
          <label className="block text-sm font-medium text-secondary mb-1.5">Mağaza açıklaması</label>
          <textarea
            rows={4}
            {...register('storeDescription')}
            className="w-full px-4 py-2.5 border border-gray-300 rounded-lg text-secondary focus:outline-none focus:ring-2 focus:ring-primary/30 focus:border-primary"
            placeholder="Mağazanızı kısaca tanıtın (isteğe bağlı)"
          />
        </div>
        <Button type="submit" loading={submitting}>
          Kaydet
        </Button>
      </form>
    </div>
  );
}
