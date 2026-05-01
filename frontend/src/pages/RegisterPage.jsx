import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import Input from '../components/ui/Input';
import Button from '../components/ui/Button';

export default function RegisterPage() {
  const navigate = useNavigate();
  const { register: registerUser } = useAuth();
  const { showToast } = useToast();
  const [loading, setLoading] = useState(false);

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors },
  } = useForm();

  const password = watch('password');

  const onSubmit = async (data) => {
    setLoading(true);
    try {
      await registerUser({
        firstName: data.firstName,
        lastName: data.lastName,
        email: data.email,
        password: data.password,
      });
      showToast('Kayıt başarılı! Hoş geldiniz.', 'success');
      navigate('/', { replace: true });
    } catch (error) {
      showToast(error.message || 'Kayıt başarısız. Lütfen tekrar deneyin.', 'error');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-[calc(100vh-200px)] flex items-center justify-center px-4 py-12">
      <div className="w-full max-w-md">
        <div className="bg-white rounded-xl border border-gray-100 p-8">
          {/* Header */}
          <div className="text-center mb-8">
            <Link to="/">
              <span className="text-4xl font-black text-primary">n11</span>
            </Link>
            <h1 className="text-2xl font-bold text-secondary mt-4">Üye Ol</h1>
            <p className="text-gray-500 mt-1">Hemen ücretsiz hesap oluşturun</p>
          </div>

          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <Input
                label="Ad"
                {...register('firstName', { required: 'Ad zorunludur' })}
                error={errors.firstName?.message}
                placeholder="Adınız"
              />
              <Input
                label="Soyad"
                {...register('lastName', { required: 'Soyad zorunludur' })}
                error={errors.lastName?.message}
                placeholder="Soyadınız"
              />
            </div>

            <Input
              label="E-posta"
              type="email"
              {...register('email', {
                required: 'E-posta zorunludur',
                pattern: {
                  value: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
                  message: 'Geçerli bir e-posta adresi girin',
                },
              })}
              error={errors.email?.message}
              placeholder="ornek@email.com"
            />

            <Input
              label="Şifre"
              type="password"
              {...register('password', {
                required: 'Şifre zorunludur',
                minLength: { value: 6, message: 'Şifre en az 6 karakter olmalıdır' },
              })}
              error={errors.password?.message}
              placeholder="••••••••"
            />

            <Input
              label="Şifre Tekrar"
              type="password"
              {...register('confirmPassword', {
                required: 'Şifre tekrarı zorunludur',
                validate: (value) =>
                  value === password || 'Şifreler eşleşmiyor',
              })}
              error={errors.confirmPassword?.message}
              placeholder="••••••••"
            />

            <Button type="submit" fullWidth size="lg" loading={loading}>
              Üye Ol
            </Button>
          </form>

          <p className="text-center text-sm text-gray-500 mt-6">
            Zaten hesabınız var mı?{' '}
            <Link to="/login" className="text-primary font-semibold hover:underline">
              Giriş Yap
            </Link>
            <br />
            <span className="mt-2 inline-block">
              Satıcı mısınız?{' '}
              <Link to="/register/seller" className="text-primary font-semibold hover:underline">
                Satıcı kaydı
              </Link>
            </span>
          </p>
        </div>
      </div>
    </div>
  );
}
