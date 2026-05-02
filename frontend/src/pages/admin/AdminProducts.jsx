import { useState, useEffect, useCallback, useRef } from 'react';
import { useForm } from 'react-hook-form';
import {
  HiPencilSquare,
  HiPhoto,
  HiExclamationCircle,
  HiCube,
  HiPlus,
  HiChevronLeft,
} from 'react-icons/hi2';
import { useAuth } from '../../context/AuthContext';
import { useToast } from '../../context/ToastContext';
import { categoryService } from '../../services/categoryService';
import { productService } from '../../services/productService';
import Input from '../../components/ui/Input';
import Button from '../../components/ui/Button';
import LoadingSpinner from '../../components/ui/LoadingSpinner';

/* ─── Image Uploader ─────────────────────────────────────────────────────── */

const MAX_FILE_SIZE = 5 * 1024 * 1024;

function formatFileSize(bytes) {
  if (!bytes) return '';
  return bytes < 1024 * 1024
    ? `${(bytes / 1024).toFixed(0)} KB`
    : `${(bytes / 1024 / 1024).toFixed(1)} MB`;
}

function ImageUploader({ file, existingUrl, onChange, error }) {
  const inputRef = useRef(null);
  const [dragging, setDragging] = useState(false);
  const [blobUrl, setBlobUrl] = useState(null);

  useEffect(() => {
    if (!file) { setBlobUrl(null); return; }
    const url = URL.createObjectURL(file);
    setBlobUrl(url);
    return () => URL.revokeObjectURL(url);
  }, [file]);

  const previewSrc = blobUrl || existingUrl || null;

  const processFile = (f) => {
    if (!f) return;
    if (!f.type.startsWith('image/')) {
      onChange(null, 'Yalnızca görsel dosyası yüklenebilir (JPG, PNG, WEBP)');
      return;
    }
    if (f.size > MAX_FILE_SIZE) {
      onChange(null, "Dosya boyutu 5 MB'ı geçemez");
      return;
    }
    onChange(f, '');
  };

  const handleDrop = (e) => {
    e.preventDefault();
    setDragging(false);
    processFile(e.dataTransfer.files?.[0]);
  };

  return (
    <div>
      <input
        ref={inputRef}
        type="file"
        accept="image/jpeg,image/png,image/webp"
        className="hidden"
        onClick={(e) => { e.target.value = ''; }}
        onChange={(e) => processFile(e.target.files?.[0])}
      />

      {previewSrc ? (
        <div className="rounded-xl border border-gray-200 bg-gray-50 p-4">
          <div className="flex justify-center mb-4">
            <img
              src={previewSrc}
              alt="Ürün görseli önizleme"
              className="h-44 w-44 object-contain rounded-xl border border-gray-100 bg-white p-1"
            />
          </div>
          {file ? (
            <div className="text-center mb-4">
              <p className="text-xs font-semibold text-secondary truncate px-2">{file.name}</p>
              <p className="text-xs text-gray-400 mt-0.5">{formatFileSize(file.size)}</p>
            </div>
          ) : (
            <p className="text-center text-xs text-gray-400 mb-4">Mevcut görsel</p>
          )}
          <div className="flex gap-2">
            <button
              type="button"
              onClick={() => inputRef.current?.click()}
              className="flex-1 text-sm font-semibold text-primary border border-primary/40 rounded-lg py-2 hover:bg-primary/5 transition-colors"
            >
              Değiştir
            </button>
            {file && (
              <button
                type="button"
                onClick={() => onChange(null, '')}
                className="flex-1 text-sm font-semibold text-gray-500 border border-gray-200 rounded-lg py-2 hover:bg-gray-100 transition-colors"
              >
                Kaldır
              </button>
            )}
          </div>
        </div>
      ) : (
        <div
          role="button"
          tabIndex={0}
          onDragOver={(e) => { e.preventDefault(); setDragging(true); }}
          onDragLeave={() => setDragging(false)}
          onDrop={handleDrop}
          onClick={() => inputRef.current?.click()}
          onKeyDown={(e) => e.key === 'Enter' && inputRef.current?.click()}
          className={`rounded-xl border-2 border-dashed cursor-pointer py-10 px-6 text-center transition-all select-none ${
            dragging
              ? 'border-primary bg-primary/5 scale-[1.01]'
              : error
              ? 'border-error/50 bg-red-50/30'
              : 'border-gray-200 hover:border-primary/50 hover:bg-gray-50/60'
          }`}
        >
          <div className="h-14 w-14 rounded-2xl bg-gray-100 flex items-center justify-center mx-auto mb-3">
            <HiPhoto className="h-7 w-7 text-gray-400" />
          </div>
          <p className="text-sm font-semibold text-secondary">Görsel seç veya sürükle bırak</p>
          <p className="text-xs text-gray-400 mt-1.5">JPG, PNG, WEBP</p>
          <p className="text-xs text-gray-400 mt-0.5">Maks. 5 MB · 800 × 800 px önerilen</p>
        </div>
      )}

      {error && (
        <p className="mt-2 text-sm text-error flex items-center gap-1.5">
          <HiExclamationCircle className="h-4 w-4 shrink-0" />
          {error}
        </p>
      )}
    </div>
  );
}

/* ─── Form defaults ──────────────────────────────────────────────────────── */

const emptyForm = {
  name: '', price: '', stock: 0, active: true, categoryId: '',
  shortDescription: '', longDescription: '',
  brand: '', model: '', warrantyPeriod: '', specifications: '',
};

/* ─── Main component ─────────────────────────────────────────────────────── */

export default function AdminProducts() {
  const { user } = useAuth();
  const { showToast } = useToast();

  // 'list' | 'form'
  const [view, setView] = useState('list');

  const [categories, setCategories] = useState([]);
  const [categoriesLoading, setCategoriesLoading] = useState(true);
  const [myProducts, setMyProducts] = useState([]);
  const [listLoading, setListLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [imageFile, setImageFile] = useState(null);
  const [imageError, setImageError] = useState('');
  const [editingId, setEditingId] = useState(null);
  const [editingImageUrl, setEditingImageUrl] = useState(null);

  const { register, handleSubmit, reset, formState: { errors } } = useForm({ defaultValues: emptyForm });

  /* fetch */
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
    (async () => {
      setCategoriesLoading(true);
      try {
        const { data: rest } = await categoryService.getAll();
        setCategories(rest.data || []);
      } catch {
        showToast('Kategoriler yüklenemedi', 'error');
      } finally {
        setCategoriesLoading(false);
      }
    })();
  }, [showToast]);

  useEffect(() => {
    if (!categoriesLoading && user?.userId) fetchMyProducts();
  }, [categoriesLoading, user?.userId, fetchMyProducts]);

  /* navigation helpers */
  const openNew = () => {
    setEditingId(null);
    setEditingImageUrl(null);
    reset(emptyForm);
    setImageFile(null);
    setImageError('');
    setView('form');
  };

  const backToList = () => {
    setEditingId(null);
    setEditingImageUrl(null);
    reset(emptyForm);
    setImageFile(null);
    setImageError('');
    setView('list');
  };

  const startEdit = async (productId) => {
    try {
      const { data: rest } = await productService.getById(productId);
      const p = rest.data;
      setEditingId(p.id);
      setEditingImageUrl(p.imageUrl || null);
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
      setView('form');
    } catch (err) {
      showToast(err.message || 'Ürün yüklenemedi', 'error');
    }
  };

  /* submit */
  const onSubmit = async (data) => {
    const isEdit = editingId != null;
    if (!isEdit && !imageFile) {
      setImageError('Ürün görseli zorunludur');
      return;
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
        await productService.update(editingId, requestBody, imageFile || undefined);
        showToast('Ürün güncellendi', 'success');
      } else {
        await productService.create(requestBody, imageFile);
        showToast('Ürün oluşturuldu', 'success');
      }
      fetchMyProducts();
      backToList();
    } catch (err) {
      showToast(err.message || (editingId ? 'Güncelleme başarısız' : 'Ürün oluşturulamadı'), 'error');
    } finally {
      setSubmitting(false);
    }
  };

  const categoryName = (id) => categories.find((c) => String(c.id) === String(id))?.name;

  if (categoriesLoading) return <LoadingSpinner size="lg" />;

  /* ── LIST VIEW ─────────────────────────────────────────────────────────── */
  if (view === 'list') {
    return (
      <div>
        <div className="flex items-center justify-between mb-6">
          <div>
            <h2 className="text-2xl font-bold text-secondary">Ürünlerim</h2>
            <p className="text-sm text-gray-500 mt-0.5">{myProducts.length} ürün listeleniyor</p>
          </div>
          <Button onClick={openNew} disabled={categories.length === 0}>
            <HiPlus className="h-4 w-4 mr-1.5" />
            Yeni Ürün Ekle
          </Button>
        </div>

        {categories.length === 0 && (
          <div className="mb-4 bg-amber-50 border border-amber-200 rounded-xl px-4 py-3 text-sm text-amber-800">
            Ürün ekleyebilmek için önce en az bir kategori oluşturulmalıdır.
          </div>
        )}

        {listLoading ? (
          <LoadingSpinner />
        ) : myProducts.length === 0 ? (
          <div className="bg-white rounded-xl border border-gray-100 py-16 text-center">
            <div className="h-16 w-16 rounded-2xl bg-gray-100 flex items-center justify-center mx-auto mb-4">
              <HiCube className="h-8 w-8 text-gray-300" />
            </div>
            <p className="text-gray-600 font-semibold">Henüz ürün eklemediniz</p>
            <p className="text-sm text-gray-400 mt-1 mb-6">İlk ürününüzü oluşturmak için aşağıdaki butona tıklayın.</p>
            <Button onClick={openNew} disabled={categories.length === 0}>
              <HiPlus className="h-4 w-4 mr-1.5" />
              İlk Ürünü Ekle
            </Button>
          </div>
        ) : (
          <div className="bg-white rounded-xl border border-gray-100 overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full min-w-160 text-sm">
                <thead>
                  <tr className="bg-gray-50 border-b border-gray-100">
                    <th className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wide">Görsel</th>
                    <th className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wide">Ürün</th>
                    <th className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wide">Fiyat</th>
                    <th className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wide">Stok</th>
                    <th className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wide">Durum</th>
                    <th className="px-4 py-3 text-right text-xs font-semibold text-gray-500 uppercase tracking-wide">İşlem</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-50">
                  {myProducts.map((row) => (
                    <tr key={row.id} className="hover:bg-gray-50/60 transition-colors">
                      <td className="px-4 py-3">
                        {row.imageUrl ? (
                          <img
                            src={row.imageUrl}
                            alt=""
                            className="h-12 w-12 object-contain rounded-lg border border-gray-100 bg-gray-50"
                            onError={(e) => { e.target.style.display = 'none'; }}
                          />
                        ) : (
                          <div className="h-12 w-12 rounded-lg border border-gray-100 bg-gray-50 flex items-center justify-center">
                            <HiPhoto className="h-5 w-5 text-gray-300" />
                          </div>
                        )}
                      </td>
                      <td className="px-4 py-3 max-w-55">
                        <p className="font-semibold text-secondary truncate">{row.name}</p>
                        {categoryName(row.categoryId) && (
                          <p className="text-xs text-gray-400 mt-0.5">{categoryName(row.categoryId)}</p>
                        )}
                      </td>
                      <td className="px-4 py-3 font-semibold text-secondary">
                        {row.price != null
                          ? `${Number(row.price).toLocaleString('tr-TR', { minimumFractionDigits: 2 })} TL`
                          : '—'}
                      </td>
                      <td className="px-4 py-3">
                        <span className={`font-semibold ${row.stock === 0 ? 'text-error' : 'text-secondary'}`}>
                          {row.stock}
                        </span>
                      </td>
                      <td className="px-4 py-3">
                        <span className={`inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-semibold ${
                          row.active ? 'bg-success/15 text-success' : 'bg-gray-100 text-gray-500'
                        }`}>
                          <span className={`h-1.5 w-1.5 rounded-full ${row.active ? 'bg-success' : 'bg-gray-400'}`} />
                          {row.active ? 'Aktif' : 'Pasif'}
                        </span>
                      </td>
                      <td className="px-4 py-3 text-right">
                        <button
                          type="button"
                          onClick={() => startEdit(row.id)}
                          className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold text-primary border border-primary/30 rounded-lg hover:bg-primary hover:text-white hover:border-primary transition-colors cursor-pointer"
                        >
                          <HiPencilSquare className="h-3.5 w-3.5" />
                          Düzenle
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </div>
    );
  }

  /* ── FORM VIEW ─────────────────────────────────────────────────────────── */
  const isEdit = editingId != null;

  return (
    <div>
      {/* Breadcrumb / back */}
      <div className="flex items-center gap-2 mb-6">
        <button
          type="button"
          onClick={backToList}
          className="inline-flex items-center gap-1.5 text-sm font-medium text-gray-500 hover:text-primary transition-colors"
        >
          <HiChevronLeft className="h-4 w-4" />
          Ürünlerim
        </button>
        <span className="text-gray-300">/</span>
        <span className="text-sm font-semibold text-secondary">
          {isEdit ? 'Ürünü Düzenle' : 'Yeni Ürün Ekle'}
        </span>
      </div>

      <form onSubmit={handleSubmit(onSubmit)}>
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">

          {/* Left: fields */}
          <div className="lg:col-span-2 space-y-6">

            {/* Temel Bilgiler */}
            <div className="bg-white rounded-xl border border-gray-100 p-6 space-y-4">
              <h3 className="text-xs font-semibold text-gray-400 uppercase tracking-wide">Temel Bilgiler</h3>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <Input
                  label="Ürün adı"
                  {...register('name', { required: 'Ürün adı zorunludur' })}
                  error={errors.name?.message}
                  placeholder="örn: iPhone 15 Pro"
                />
                <div>
                  <label className="block text-sm font-medium text-secondary mb-1.5">Kategori</label>
                  <select
                    {...register('categoryId', { required: 'Kategori seçin' })}
                    className="w-full px-4 py-2.5 border rounded-lg text-secondary border-gray-300 focus:outline-none focus:ring-2 focus:ring-primary/30 focus:border-primary bg-white"
                  >
                    <option value="">Kategori seçin</option>
                    {categories.map((c) => (
                      <option key={c.id} value={c.id}>{c.name}</option>
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
                  label="Stok adedi"
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
              <label className="flex items-center gap-3 cursor-pointer w-fit">
                <input
                  type="checkbox"
                  {...register('active', { valueAsBoolean: true })}
                  className="h-4 w-4 rounded border-gray-300 text-primary focus:ring-primary"
                />
                <span className="text-sm font-medium text-secondary">Ürünü satışa aç</span>
              </label>
            </div>

            {/* Ürün Detayı */}
            <div className="bg-white rounded-xl border border-gray-100 p-6 space-y-4">
              <h3 className="text-xs font-semibold text-gray-400 uppercase tracking-wide">Ürün Detayı</h3>
              <Input
                label="Kısa açıklama"
                {...register('shortDescription', { required: 'Kısa açıklama zorunludur' })}
                error={errors.shortDescription?.message}
                placeholder="Yeni nesil Pro kamera sistemi"
              />
              <div>
                <label className="block text-sm font-medium text-secondary mb-1.5">Uzun açıklama</label>
                <textarea
                  rows={4}
                  {...register('longDescription', { required: 'Uzun açıklama zorunludur' })}
                  className={`w-full px-4 py-2.5 border rounded-lg text-secondary placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:ring-primary/30 focus:border-primary resize-none ${
                    errors.longDescription ? 'border-error' : 'border-gray-300'
                  }`}
                  placeholder="Apple A17 Pro işlemci, titanyum tasarım..."
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
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <Input
                  label="Garanti süresi"
                  {...register('warrantyPeriod')}
                  placeholder="24 Ay (isteğe bağlı)"
                />
                <div>
                  <label className="block text-sm font-medium text-secondary mb-1.5">
                    Teknik özellikler
                    <span className="text-gray-400 font-normal ml-1">(isteğe bağlı)</span>
                  </label>
                  <textarea
                    rows={2}
                    {...register('specifications')}
                    className="w-full px-4 py-2.5 border border-gray-300 rounded-lg text-secondary placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:ring-primary/30 focus:border-primary resize-none"
                    placeholder="256 GB, 8 GB RAM"
                  />
                </div>
              </div>
            </div>
          </div>

          {/* Right: image + actions */}
          <div className="space-y-5">
            <div className="bg-white rounded-xl border border-gray-100 p-6">
              <h3 className="text-xs font-semibold text-gray-400 uppercase tracking-wide mb-4">
                Ürün Görseli
                {!isEdit && <span className="text-error ml-1">*</span>}
              </h3>
              <ImageUploader
                file={imageFile}
                existingUrl={editingImageUrl}
                onChange={(f, err) => { setImageFile(f); setImageError(err || ''); }}
                error={imageError}
              />
              {isEdit && (
                <p className="text-xs text-gray-400 mt-3 text-center">
                  Yeni görsel seçmezseniz mevcut görsel korunur.
                </p>
              )}
            </div>

            <div className="bg-white rounded-xl border border-gray-100 p-6 space-y-3">
              <Button
                type="submit"
                size="lg"
                loading={submitting}
                className="w-full"
              >
                {isEdit ? 'Değişiklikleri Kaydet' : 'Ürünü Oluştur'}
              </Button>
              <button
                type="button"
                onClick={backToList}
                className="w-full text-sm font-medium text-gray-500 py-2.5 rounded-lg hover:bg-gray-100 transition-colors"
              >
                İptal
              </button>
            </div>
          </div>
        </div>
      </form>
    </div>
  );
}
