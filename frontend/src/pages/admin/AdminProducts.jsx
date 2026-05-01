import { useState, useEffect, useCallback } from 'react';
import { useForm } from 'react-hook-form';
import { HiPencilSquare, HiXMark } from 'react-icons/hi2';
import { useAuth } from '../../context/AuthContext';
import { useToast } from '../../context/ToastContext';
import { categoryService } from '../../services/categoryService';
import { productService } from '../../services/productService';
import Input from '../../components/ui/Input';
import Button from '../../components/ui/Button';
import LoadingSpinner from '../../components/ui/LoadingSpinner';

const emptyForm = {
  name: '',
  price: '',
  stock: 0,
  active: true,
  categoryId: '',
  shortDescription: '',
  longDescription: '',
  brand: '',
  model: '',
  warrantyPeriod: '',
  specifications: '',
};

export default function AdminProducts() {
  const { user } = useAuth();
  const { showToast } = useToast();
  const [categories, setCategories] = useState([]);
  const [categoriesLoading, setCategoriesLoading] = useState(true);
  const [myProducts, setMyProducts] = useState([]);
  const [listLoading, setListLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [imageFile, setImageFile] = useState(null);
  const [imageError, setImageError] = useState('');
  const [fileInputKey, setFileInputKey] = useState(0);
  const [editingId, setEditingId] = useState(null);
  const [editSellerId, setEditSellerId] = useState(null);

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm({ defaultValues: emptyForm });

  const fetchMyProducts = useCallback(async () => {
    const uid = user?.userId;
    if (!uid) return;
    setListLoading(true);
    try {
      const { data: rest } = await productService.getBySellerId(uid, { page: 0, size: 100 });
      setMyProducts(rest.data?.content || []);
    } catch {
      showToast('Ürün listesi yüklenemedi', 'error');
    } finally {
      setListLoading(false);
    }
  }, [user?.userId, showToast]);

  useEffect(() => {
    const load = async () => {
      setCategoriesLoading(true);
      try {
        const { data: rest } = await categoryService.getAll();
        setCategories(rest.data || []);
      } catch {
        showToast('Kategoriler yüklenemedi', 'error');
      } finally {
        setCategoriesLoading(false);
      }
    };
    load();
  }, [showToast]);

  useEffect(() => {
    if (!categoriesLoading && user?.userId) {
      fetchMyProducts();
    }
  }, [categoriesLoading, user?.userId, fetchMyProducts]);

  const cancelEdit = () => {
    setEditingId(null);
    setEditSellerId(null);
    reset(emptyForm);
    setImageFile(null);
    setImageError('');
    setFileInputKey((k) => k + 1);
  };

  const startEdit = async (productId) => {
    try {
      const { data: rest } = await productService.getById(productId);
      const p = rest.data;
      setEditingId(p.id);
      setEditSellerId(p.sellerId ?? user?.userId);
      reset({
        name: p.name ?? '',
        price: p.price != null ? String(p.price) : '',
        stock: p.stock ?? 0,
        active: p.active !== false,
        categoryId: p.categoryId != null ? String(p.categoryId) : '',
        shortDescription: p.detail?.shortDescription ?? '',
        longDescription: p.detail?.longDescription ?? '',
        brand: p.detail?.brand ?? '',
        model: p.detail?.model ?? '',
        warrantyPeriod: p.detail?.warrantyPeriod ?? '',
        specifications: p.detail?.specifications ?? '',
      });
      setImageFile(null);
      setImageError('');
      setFileInputKey((k) => k + 1);
      window.scrollTo({ top: 0, behavior: 'smooth' });
    } catch (err) {
      showToast(err.message || 'Ürün yüklenemedi', 'error');
    }
  };

  const onSubmit = async (data) => {
    const userId = user?.userId;
    if (!userId) {
      showToast('Kullanıcı kimliği bulunamadı. Lütfen tekrar giriş yapın.', 'error');
      return;
    }

    const isEdit = editingId != null;

    if (!isEdit && !imageFile) {
      setImageError('Ürün görseli zorunludur');
      showToast('Lütfen bir ürün görseli seçin', 'error');
      return;
    }
    if (!isEdit) {
      setImageError('');
    }

    setSubmitting(true);
    try {
      const requestBody = {
        name: data.name.trim(),
        price: Number(data.price),
        stock: Number(data.stock),
        active: Boolean(data.active),
        categoryId: Number(data.categoryId),
        detail: {
          shortDescription: data.shortDescription.trim(),
          longDescription: data.longDescription.trim(),
          brand: data.brand.trim(),
          model: data.model.trim(),
          warrantyPeriod: data.warrantyPeriod?.trim() || '',
          specifications: data.specifications?.trim() || '',
        },
      };

      if (isEdit) {
        const sid = editSellerId ?? userId;
        await productService.update(sid, editingId, requestBody, imageFile || undefined);
        showToast('Ürün güncellendi', 'success');
        cancelEdit();
      } else {
        await productService.create(userId, requestBody, imageFile);
        showToast('Ürün başarıyla oluşturuldu', 'success');
        reset(emptyForm);
        setImageFile(null);
        setFileInputKey((k) => k + 1);
      }
      fetchMyProducts();
    } catch (err) {
      showToast(err.message || (isEdit ? 'Güncelleme başarısız' : 'Ürün oluşturulamadı'), 'error');
    } finally {
      setSubmitting(false);
    }
  };

  if (categoriesLoading) return <LoadingSpinner size="lg" />;

  const isEdit = editingId != null;

  return (
    <div className="space-y-10">
      <div>
        <div className="flex flex-wrap items-center justify-between gap-3 mb-2">
          <h2 className="text-2xl font-bold text-secondary">
            {isEdit ? 'Ürün düzenle' : 'Ürün ekle'}
          </h2>
          {isEdit && (
            <Button type="button" variant="outline" size="sm" onClick={cancelEdit}>
              <HiXMark className="h-4 w-4 mr-1" />
              İptal
            </Button>
          )}
        </div>
        <p className="text-sm text-gray-500 mb-6">
          {isEdit ? (
            <>
              Ürün bilgilerini güncelleyin. Yeni görsel seçmezseniz mevcut görsel korunur. Güncelleme için{' '}
              <code className="text-xs bg-gray-100 px-1 rounded">sellerId</code> backend ile eşleşmelidir.
            </>
          ) : (
            <>
              Yeni ürün oluşturun. Ürün görseli zorunludur. Oluşturma için{' '}
              <code className="text-xs bg-gray-100 px-1 rounded">X-User-Id</code> JWT&apos;deki kullanıcı id&apos;niz
              gönderilir.
            </>
          )}
        </p>

        <form
          onSubmit={handleSubmit(onSubmit)}
          className="max-w-3xl space-y-6 bg-white rounded-xl border border-gray-100 p-6 md:p-8"
        >
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <Input
              label="Ürün adı"
              {...register('name', { required: 'Ürün adı zorunludur' })}
              error={errors.name?.message}
              placeholder="örn: iPhone 15 Pro"
            />
            <div className="w-full">
              <label className="block text-sm font-medium text-secondary mb-1.5">Kategori</label>
              <select
                {...register('categoryId', { required: 'Kategori seçin' })}
                className="w-full px-4 py-2.5 border rounded-lg text-secondary border-gray-300 focus:outline-none focus:ring-2 focus:ring-primary/30 focus:border-primary"
              >
                <option value="">Seçin</option>
                {categories.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.name}
                  </option>
                ))}
              </select>
              {errors.categoryId && (
                <p className="mt-1 text-sm text-error">{errors.categoryId.message}</p>
              )}
            </div>
            <Input
              label="Fiyat (TL)"
              type="number"
              step="0.01"
              min="0.01"
              {...register('price', {
                required: 'Fiyat zorunludur',
                min: { value: 0.01, message: "Fiyat 0'dan büyük olmalı" },
              })}
              error={errors.price?.message}
              placeholder="49999.99"
            />
            <Input
              label="Stok"
              type="number"
              min="0"
              {...register('stock', {
                required: 'Stok zorunludur',
                min: { value: 0, message: 'Stok 0 veya üzeri olmalı' },
              })}
              error={errors.stock?.message}
              placeholder="100"
            />
          </div>

          <div className="flex items-center gap-3">
            <input
              type="checkbox"
              id="active"
              {...register('active', { valueAsBoolean: true })}
              className="h-4 w-4 rounded border-gray-300 text-primary focus:ring-primary"
            />
            <label htmlFor="active" className="text-sm font-medium text-secondary">
              Ürün aktif (satışta)
            </label>
          </div>

          <div className="border-t pt-6 space-y-4">
            <h3 className="text-lg font-semibold text-secondary">Ürün detayı</h3>
            <Input
              label="Kısa açıklama"
              {...register('shortDescription', { required: 'Kısa açıklama zorunludur' })}
              error={errors.shortDescription?.message}
              placeholder="Yeni nesil iPhone"
            />
            <div className="w-full">
              <label className="block text-sm font-medium text-secondary mb-1.5">Uzun açıklama</label>
              <textarea
                rows={4}
                {...register('longDescription', { required: 'Uzun açıklama zorunludur' })}
                className={`w-full px-4 py-2.5 border rounded-lg text-secondary placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:ring-primary/30 focus:border-primary ${
                  errors.longDescription ? 'border-error' : 'border-gray-300'
                }`}
                placeholder="Apple A17 Pro işlemcili..."
              />
              {errors.longDescription && (
                <p className="mt-1 text-sm text-error">{errors.longDescription.message}</p>
              )}
            </div>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <Input
                label="Marka"
                {...register('brand', { required: 'Marka zorunludur' })}
                error={errors.brand?.message}
                placeholder="Apple"
              />
              <Input
                label="Model"
                {...register('model', { required: 'Model zorunludur' })}
                error={errors.model?.message}
                placeholder="15 Pro"
              />
            </div>
            <Input
              label="Garanti süresi (isteğe bağlı)"
              {...register('warrantyPeriod')}
              error={errors.warrantyPeriod?.message}
              placeholder="24 Ay"
            />
            <div className="w-full">
              <label className="block text-sm font-medium text-secondary mb-1.5">
                Teknik özellikler (isteğe bağlı)
              </label>
              <textarea
                rows={3}
                {...register('specifications')}
                className="w-full px-4 py-2.5 border border-gray-300 rounded-lg text-secondary focus:outline-none focus:ring-2 focus:ring-primary/30 focus:border-primary"
                placeholder="256GB, 8GB RAM"
              />
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium text-secondary mb-1.5">
              Ürün görseli
              {!isEdit ? (
                <span className="text-error"> *</span>
              ) : (
                <span className="text-gray-400 font-normal"> (yeni görsel isteğe bağlı)</span>
              )}
            </label>
            <input
              key={fileInputKey}
              type="file"
              accept="image/*"
              onChange={(e) => {
                const file = e.target.files?.[0] || null;
                setImageFile(file);
                if (!isEdit) {
                  setImageError(file ? '' : 'Ürün görseli zorunludur');
                } else {
                  setImageError('');
                }
              }}
              className={`block w-full text-sm text-gray-500 file:mr-4 file:py-2 file:px-4 file:rounded-lg file:border-0 file:text-sm file:font-semibold file:bg-primary file:text-white hover:file:bg-primary/90 cursor-pointer ${
                imageError ? 'ring-2 ring-error/40 rounded-lg' : ''
              }`}
            />
            {imageError && <p className="mt-1 text-sm text-error">{imageError}</p>}
          </div>

          <div className="flex flex-wrap gap-3">
            <Button type="submit" size="lg" loading={submitting} disabled={categories.length === 0}>
              {isEdit ? 'Değişiklikleri kaydet' : 'Ürünü oluştur'}
            </Button>
          </div>
          {categories.length === 0 && (
            <p className="text-sm text-error">Önce en az bir kategori oluşturmalısınız.</p>
          )}
        </form>
      </div>

      {/* Ürünlerim listesi */}
      <div>
        <h3 className="text-xl font-bold text-secondary mb-4">Ürünlerim</h3>

        {listLoading ? (
          <LoadingSpinner />
        ) : (
          <div className="bg-white rounded-xl border border-gray-100 overflow-hidden overflow-x-auto">
            <table className="w-full min-w-[640px]">
              <thead>
                <tr className="bg-gray-50 border-b text-left text-xs font-semibold text-gray-500 uppercase">
                  <th className="px-4 py-3">Görsel</th>
                  <th className="px-4 py-3">Ad</th>
                  <th className="px-4 py-3">Fiyat</th>
                  <th className="px-4 py-3">Stok</th>
                  <th className="px-4 py-3">Durum</th>
                  <th className="px-4 py-3 text-right">İşlem</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {myProducts.map((row) => (
                  <tr key={row.id} className="hover:bg-gray-50/80">
                    <td className="px-4 py-3">
                      <img
                        src={row.imageUrl || 'https://via.placeholder.com/48?text=–'}
                        alt=""
                        className="h-12 w-12 object-contain rounded border bg-gray-50"
                        onError={(e) => {
                          e.target.src = 'https://via.placeholder.com/48?text=–';
                        }}
                      />
                    </td>
                    <td className="px-4 py-3 text-sm font-medium text-secondary max-w-[200px] truncate">
                      {row.name}
                    </td>
                    <td className="px-4 py-3 text-sm">
                      {row.price != null
                        ? `${Number(row.price).toLocaleString('tr-TR', { minimumFractionDigits: 2 })} TL`
                        : '—'}
                    </td>
                    <td className="px-4 py-3 text-sm text-gray-600">{row.stock}</td>
                    <td className="px-4 py-3">
                      <span
                        className={`inline-flex px-2 py-0.5 rounded-full text-xs font-semibold ${
                          row.active ? 'bg-success/15 text-success' : 'bg-gray-100 text-gray-500'
                        }`}
                      >
                        {row.active ? 'Aktif' : 'Pasif'}
                      </span>
                    </td>
                    <td className="px-4 py-3 text-right">
                      <button
                        type="button"
                        onClick={() => startEdit(row.id)}
                        className="inline-flex items-center gap-1 px-3 py-1.5 text-sm font-medium text-primary border border-primary rounded-lg hover:bg-primary hover:text-white transition-colors cursor-pointer"
                      >
                        <HiPencilSquare className="h-4 w-4" />
                        Düzenle
                      </button>
                    </td>
                  </tr>
                ))}
                {myProducts.length === 0 && (
                  <tr>
                    <td colSpan={6} className="px-4 py-12 text-center text-gray-500 text-sm">
                      Henüz bu hesaba ait ürün yok veya liste yüklenemedi.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
