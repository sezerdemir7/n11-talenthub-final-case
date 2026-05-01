import { useState, useEffect, useCallback } from 'react';
import { HiPlus, HiPencilSquare, HiTrash, HiXMark } from 'react-icons/hi2';
import { useForm } from 'react-hook-form';
import { categoryService } from '../../services/categoryService';
import { useToast } from '../../context/ToastContext';
import Button from '../../components/ui/Button';
import Input from '../../components/ui/Input';
import LoadingSpinner from '../../components/ui/LoadingSpinner';
import ErrorMessage from '../../components/ui/ErrorMessage';

export default function AdminCategories() {
  const { showToast } = useToast();
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingCategory, setEditingCategory] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [deleteConfirm, setDeleteConfirm] = useState(null);

  const {
    register,
    handleSubmit,
    reset,
    setValue,
    formState: { errors },
  } = useForm();

  const fetchCategories = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const { data: restResponse } = await categoryService.getAll();
      setCategories(restResponse.data || []);
    } catch (err) {
      setError(err.message || 'Kategoriler yüklenemedi');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchCategories();
  }, [fetchCategories]);

  const openCreateModal = () => {
    setEditingCategory(null);
    reset({ name: '', parentId: '', active: true, sortOrder: 0 });
    setModalOpen(true);
  };

  const openEditModal = (category) => {
    setEditingCategory(category);
    setValue('name', category.name);
    setValue('parentId', category.parentId || '');
    setValue('active', category.active);
    setValue('sortOrder', category.sortOrder || 0);
    setModalOpen(true);
  };

  const closeModal = () => {
    setModalOpen(false);
    setEditingCategory(null);
    reset();
  };

  const onSubmit = async (formData) => {
    setSubmitting(true);
    try {
      const payload = {
        name: formData.name,
        parentId: formData.parentId ? Number(formData.parentId) : null,
        active: formData.active,
        sortOrder: Number(formData.sortOrder) || 0,
      };

      if (editingCategory) {
        await categoryService.update(editingCategory.id, payload);
        showToast('Kategori güncellendi', 'success');
      } else {
        await categoryService.create(payload);
        showToast('Kategori oluşturuldu', 'success');
      }

      closeModal();
      fetchCategories();
    } catch (err) {
      showToast(err.message || 'İşlem başarısız', 'error');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (id) => {
    try {
      await categoryService.delete(id);
      showToast('Kategori silindi', 'success');
      setDeleteConfirm(null);
      fetchCategories();
    } catch (err) {
      showToast(err.message || 'Silme işlemi başarısız', 'error');
    }
  };

  const parentOptions = categories.filter((c) => c.id !== editingCategory?.id);

  if (loading) return <LoadingSpinner size="lg" />;
  if (error) return <ErrorMessage message={error} onRetry={fetchCategories} />;

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div>
          <h2 className="text-2xl font-bold text-secondary">Kategori Yönetimi</h2>
          <p className="text-sm text-gray-500 mt-1">{categories.length} kategori</p>
        </div>
        <Button onClick={openCreateModal}>
          <HiPlus className="h-4 w-4 mr-1.5" />
          Yeni Kategori
        </Button>
      </div>

      {/* Table */}
      <div className="bg-white rounded-xl border border-gray-100 overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="bg-gray-50 border-b">
                <th className="text-left px-6 py-3 text-xs font-semibold text-gray-500 uppercase">ID</th>
                <th className="text-left px-6 py-3 text-xs font-semibold text-gray-500 uppercase">Ad</th>
                <th className="text-left px-6 py-3 text-xs font-semibold text-gray-500 uppercase">Slug</th>
                <th className="text-left px-6 py-3 text-xs font-semibold text-gray-500 uppercase">Üst Kategori</th>
                <th className="text-left px-6 py-3 text-xs font-semibold text-gray-500 uppercase">Sıra</th>
                <th className="text-left px-6 py-3 text-xs font-semibold text-gray-500 uppercase">Durum</th>
                <th className="text-right px-6 py-3 text-xs font-semibold text-gray-500 uppercase">İşlemler</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {categories.map((cat) => (
                <tr key={cat.id} className="hover:bg-gray-50 transition-colors">
                  <td className="px-6 py-4 text-sm text-gray-500 font-mono">{cat.id}</td>
                  <td className="px-6 py-4">
                    <span className="text-sm font-medium text-secondary">{cat.name}</span>
                  </td>
                  <td className="px-6 py-4 text-sm text-gray-500 font-mono">{cat.slug}</td>
                  <td className="px-6 py-4 text-sm text-gray-500">
                    {cat.parentName || <span className="text-gray-300">—</span>}
                  </td>
                  <td className="px-6 py-4 text-sm text-gray-500">{cat.sortOrder}</td>
                  <td className="px-6 py-4">
                    <span
                      className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-semibold ${
                        cat.active
                          ? 'bg-success/10 text-success'
                          : 'bg-gray-100 text-gray-500'
                      }`}
                    >
                      {cat.active ? 'Aktif' : 'Pasif'}
                    </span>
                  </td>
                  <td className="px-6 py-4 text-right">
                    <div className="flex items-center justify-end gap-2">
                      <button
                        onClick={() => openEditModal(cat)}
                        className="p-1.5 text-gray-400 hover:text-primary rounded-lg hover:bg-primary/5 transition-colors cursor-pointer"
                        title="Düzenle"
                      >
                        <HiPencilSquare className="h-4 w-4" />
                      </button>
                      <button
                        onClick={() => setDeleteConfirm(cat)}
                        className="p-1.5 text-gray-400 hover:text-error rounded-lg hover:bg-error/5 transition-colors cursor-pointer"
                        title="Sil"
                      >
                        <HiTrash className="h-4 w-4" />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
              {categories.length === 0 && (
                <tr>
                  <td colSpan={7} className="px-6 py-12 text-center text-gray-400">
                    Henüz kategori yok
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Create / Edit Modal */}
      {modalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div className="absolute inset-0 bg-black/50" onClick={closeModal} />
          <div className="relative bg-white rounded-xl w-full max-w-md p-6 shadow-2xl">
            <div className="flex items-center justify-between mb-6">
              <h3 className="text-lg font-bold text-secondary">
                {editingCategory ? 'Kategori Düzenle' : 'Yeni Kategori'}
              </h3>
              <button
                onClick={closeModal}
                className="p-1 text-gray-400 hover:text-secondary cursor-pointer"
              >
                <HiXMark className="h-5 w-5" />
              </button>
            </div>

            <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
              <Input
                label="Kategori Adı"
                {...register('name', { required: 'Kategori adı zorunludur' })}
                error={errors.name?.message}
                placeholder="örn: Elektronik"
              />

              <div className="w-full">
                <label className="block text-sm font-medium text-secondary mb-1.5">
                  Üst Kategori
                </label>
                <select
                  {...register('parentId')}
                  className="w-full px-4 py-2.5 border border-gray-300 rounded-lg text-secondary focus:outline-none focus:ring-2 focus:ring-primary/30 focus:border-primary transition-all"
                >
                  <option value="">Yok (Ana Kategori)</option>
                  {parentOptions.map((c) => (
                    <option key={c.id} value={c.id}>
                      {c.name}
                    </option>
                  ))}
                </select>
              </div>

              <Input
                label="Sıralama"
                type="number"
                {...register('sortOrder', { min: { value: 0, message: 'Sıralama 0 veya üzeri olmalı' } })}
                error={errors.sortOrder?.message}
                placeholder="0"
              />

              <div className="flex items-center gap-3">
                <input
                  type="checkbox"
                  id="active"
                  {...register('active')}
                  className="h-4 w-4 rounded border-gray-300 text-primary focus:ring-primary"
                />
                <label htmlFor="active" className="text-sm font-medium text-secondary">
                  Aktif
                </label>
              </div>

              <div className="flex gap-3 pt-2">
                <Button type="button" variant="ghost" onClick={closeModal} fullWidth>
                  İptal
                </Button>
                <Button type="submit" loading={submitting} fullWidth>
                  {editingCategory ? 'Güncelle' : 'Oluştur'}
                </Button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Delete Confirmation */}
      {deleteConfirm && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div className="absolute inset-0 bg-black/50" onClick={() => setDeleteConfirm(null)} />
          <div className="relative bg-white rounded-xl w-full max-w-sm p-6 shadow-2xl text-center">
            <HiTrash className="h-12 w-12 text-error mx-auto mb-4" />
            <h3 className="text-lg font-bold text-secondary mb-2">Kategoriyi Sil</h3>
            <p className="text-sm text-gray-500 mb-6">
              <strong>&quot;{deleteConfirm.name}&quot;</strong> kategorisini silmek istediğinize emin misiniz?
              Bu işlem geri alınamaz.
            </p>
            <div className="flex gap-3">
              <Button variant="ghost" onClick={() => setDeleteConfirm(null)} fullWidth>
                İptal
              </Button>
              <Button variant="danger" onClick={() => handleDelete(deleteConfirm.id)} fullWidth>
                Sil
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
