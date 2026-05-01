import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import Input from '../ui/Input';
import Button from '../ui/Button';

const defaults = {
  title: '',
  recipientName: '',
  phone: '',
  city: '',
  district: '',
  fullAddress: '',
  postalCode: '',
};

/**
 * AddressRequest ile uyumlu alanlar: title, recipientName, phone, city, district, fullAddress, postalCode
 * Backend farklı isim kullanıyorsa burada mapleyin.
 */
export default function AddressForm({
  initialValues = null,
  onSubmit,
  submitLabel = 'Kaydet',
  loading = false,
  onCancel,
}) {
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm({ defaultValues: defaults });

  useEffect(() => {
    reset(initialValues ? { ...defaults, ...initialValues } : defaults);
  }, [initialValues, reset]);

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
        <Input
          label="Adres başlığı"
          placeholder="Örn: Ev, İş"
          {...register('title', { required: 'Zorunlu' })}
          error={errors.title?.message}
        />
        <Input
          label="Alıcı adı soyadı"
          {...register('recipientName', { required: 'Zorunlu' })}
          error={errors.recipientName?.message}
        />
      </div>
      <Input
        label="Telefon"
        {...register('phone', { required: 'Zorunlu' })}
        error={errors.phone?.message}
        placeholder="05xx xxx xx xx"
      />
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
        <Input
          label="İl"
          {...register('city', { required: 'Zorunlu' })}
          error={errors.city?.message}
        />
        <Input
          label="İlçe"
          {...register('district', { required: 'Zorunlu' })}
          error={errors.district?.message}
        />
      </div>
      <div className="w-full">
        <label className="block text-sm font-medium text-secondary mb-1.5">Açık adres</label>
        <textarea
          rows={3}
          {...register('fullAddress', { required: 'Zorunlu' })}
          className={`w-full px-4 py-2.5 border rounded-lg text-secondary placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:ring-primary/30 focus:border-primary transition-all ${
            errors.fullAddress ? 'border-error ring-1 ring-error/30' : 'border-gray-300'
          }`}
          placeholder="Mahalle, sokak, bina no, daire..."
        />
        {errors.fullAddress && (
          <p className="mt-1 text-sm text-error">{errors.fullAddress.message}</p>
        )}
      </div>
      <Input
        label="Posta kodu"
        {...register('postalCode')}
        error={errors.postalCode?.message}
      />
      <div className="flex flex-wrap gap-2 pt-2">
        <Button type="submit" loading={loading}>
          {submitLabel}
        </Button>
        {onCancel && (
          <Button type="button" variant="outline" onClick={onCancel} disabled={loading}>
            Vazgeç
          </Button>
        )}
      </div>
    </form>
  );
}
