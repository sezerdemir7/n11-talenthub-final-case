import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import Input from '../components/ui/Input';
import Button from '../components/ui/Button';

export default function RegisterSellerPage() {
  const navigate = useNavigate();
  const { registerSeller } = useAuth();
  const { showToast } = useToast();
  const [loading, setLoading] = useState(false);

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors },
  } = useForm();

  const password = watch('password');
  const storeDescription = watch('storeDescription') || '';

  const onSubmit = async (data) => {
    setLoading(true);
    try {
      await registerSeller({
        firstName: data.firstName,
        lastName: data.lastName,
        email: data.email,
        password: data.password,
        storeName: data.storeName,
        companyName: data.companyName,
        taxNumber: data.taxNumber,
        storeDescription: data.storeDescription?.trim() || undefined,
      });
      showToast('Satıcı başvurunuz alındı. Hoş geldiniz!', 'success');
      navigate('/seller', { replace: true });
    } catch (error) {
      showToast(error.message || 'Kayıt başarısız', 'error');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-[calc(100vh-200px)] flex items-center justify-center px-4 py-12">
      <div className="w-full max-w-lg">
        <div className="bg-white rounded-xl border border-gray-100 p-8">
          <div className="text-center mb-8">
            <Link to="/">
              <span className="text-4xl font-black text-primary">n11</span>
            </Link>
            <h1 className="text-2xl font-bold text-secondary mt-4">Satıcı Kaydı</h1>
            <p className="text-gray-500 mt-1">Mağazanızı açın; onay sonrası satışa başlayın</p>
          </div>

          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <Input
                label="Ad"
                {...register('firstName', { required: 'Zorunlu' })}
                error={errors.firstName?.message}
              />
              <Input
                label="Soyad"
                {...register('lastName', { required: 'Zorunlu' })}
                error={errors.lastName?.message}
              />
            </div>
            <Input
              label="E-posta"
              type="email"
              {...register('email', {
                required: 'Zorunlu',
                pattern: { value: /^[^\s@]+@[^\s@]+\.[^\s@]+$/, message: 'Geçerli e-posta' },
              })}
              error={errors.email?.message}
            />
            <Input
              label="Şifre"
              type="password"
              {...register('password', {
                required: 'Zorunlu',
                minLength: { value: 6, message: 'En az 6 karakter' },
              })}
              error={errors.password?.message}
            />
            <Input
              label="Şifre tekrar"
              type="password"
              {...register('confirmPassword', {
                required: 'Zorunlu',
                validate: (v) => v === password || 'Şifreler eşleşmiyor',
              })}
              error={errors.confirmPassword?.message}
            />
            <Input
              label="Mağaza adı"
              {...register('storeName', { required: 'Zorunlu' })}
              error={errors.storeName?.message}
            />
            <Input
              label="Şirket ünvanı"
              {...register('companyName', { required: 'Zorunlu' })}
              error={errors.companyName?.message}
            />
            <Input
              label="Vergi numarası"
              {...register('taxNumber', { required: 'Zorunlu' })}
              error={errors.taxNumber?.message}
              placeholder="1234567890"
            />
            <div className="w-full">
              <label className="block text-sm font-medium text-secondary mb-1.5">Mağaza açıklaması</label>
              <textarea
                rows={3}
                maxLength={250}
                {...register('storeDescription', {
                  maxLength: { value: 250, message: 'Açıklama en fazla 250 karakter olabilir' },
                })}
                className="w-full px-4 py-2.5 border border-gray-300 rounded-lg text-secondary focus:outline-none focus:ring-2 focus:ring-primary/30"
                placeholder="İsteğe bağlı"
              />
              <div className="mt-1 flex items-center justify-between text-xs">
                <span className="text-error">{errors.storeDescription?.message}</span>
                <span className="text-gray-400">{storeDescription.length}/250</span>
              </div>
            </div>
            <Button type="submit" fullWidth size="lg" loading={loading}>
              Başvuruyu Gönder
            </Button>
          </form>

          <p className="text-center text-sm text-gray-500 mt-6">
            <Link to="/register" className="text-primary font-semibold hover:underline">
              Müşteri olarak kayıt
            </Link>
            {' · '}
            <Link to="/login" className="text-primary font-semibold hover:underline">
              Giriş
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}
