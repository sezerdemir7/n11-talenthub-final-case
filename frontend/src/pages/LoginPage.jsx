import { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { hasRole } from '../utils/jwt';
import Input from '../components/ui/Input';
import Button from '../components/ui/Button';

export default function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuth();
  const { showToast } = useToast();
  const [loading, setLoading] = useState(false);

  const from = location.state?.from?.pathname;

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm();

  const onSubmit = async (data) => {
    setLoading(true);
    try {
      const result = await login(data);
      showToast('Giriş başarılı!', 'success');

      if (from) {
        navigate(from, { replace: true });
      } else if (hasRole(result.user, 'ADMIN')) {
        navigate('/admin', { replace: true });
      } else if (hasRole(result.user, 'SELLER')) {
        navigate('/seller', { replace: true });
      } else {
        navigate('/', { replace: true });
      }
    } catch (error) {
      showToast(error.message || 'Giriş başarısız. Bilgilerinizi kontrol edin.', 'error');
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
            <h1 className="text-2xl font-bold text-secondary mt-4">Giriş Yap</h1>
            <p className="text-gray-500 mt-1">Hesabınıza giriş yapın</p>
          </div>

          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
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

            <Button type="submit" fullWidth size="lg" loading={loading}>
              Giriş Yap
            </Button>
          </form>

          <p className="text-center text-sm text-gray-500 mt-6">
            Hesabınız yok mu?{' '}
            <Link to="/register" className="text-primary font-semibold hover:underline">
              Üye Ol
            </Link>
            {' · '}
            <Link to="/register/seller" className="text-primary font-semibold hover:underline">
              Satıcı kaydı
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}
