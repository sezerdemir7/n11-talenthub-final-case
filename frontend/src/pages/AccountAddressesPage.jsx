import { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { HiArrowLeft, HiMapPin, HiPencilSquare, HiTrash, HiStar } from 'react-icons/hi2';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { addressService } from '../services/addressService';
import AddressForm from '../components/account/AddressForm';
import Button from '../components/ui/Button';
import LoadingSpinner from '../components/ui/LoadingSpinner';
import ErrorMessage from '../components/ui/ErrorMessage';

function isDefaultAddress(a) {
  return Boolean(a?.defaultAddress ?? a?.default ?? a?.isDefault);
}

function toFormValues(row) {
  if (!row) return null;
  return {
    title: row.title ?? '',
    recipientName: row.recipientName ?? row.fullName ?? '',
    phone: row.phone ?? row.phoneNumber ?? '',
    city: row.city ?? '',
    district: row.district ?? '',
    fullAddress: row.fullAddress ?? row.addressLine ?? '',
    postalCode: row.postalCode ?? '',
  };
}

export default function AccountAddressesPage() {
  const { user } = useAuth();
  const { showToast } = useToast();
  const [list, setList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [editingId, setEditingId] = useState(null);
  const [showCreate, setShowCreate] = useState(false);
  const [formLoading, setFormLoading] = useState(false);
  const [acting, setActing] = useState(null);

  const uid = user?.userId;

  const load = useCallback(async () => {
    if (!uid) return;
    setLoading(true);
    setError(null);
    try {
      const { data: rest } = await addressService.list(uid);
      setList(rest.data || []);
    } catch (err) {
      setError(err.message || 'Adresler yüklenemedi');
    } finally {
      setLoading(false);
    }
  }, [uid]);

  useEffect(() => {
    load();
  }, [load]);

  const onCreate = async (data) => {
    if (!uid) return;
    setFormLoading(true);
    try {
      await addressService.create(uid, {
        title: data.title.trim(),
        recipientName: data.recipientName.trim(),
        phone: data.phone.trim(),
        city: data.city.trim(),
        district: data.district.trim(),
        fullAddress: data.fullAddress.trim(),
        postalCode: data.postalCode?.trim() || undefined,
      });
      showToast('Adres eklendi', 'success');
      setShowCreate(false);
      await load();
    } catch (err) {
      showToast(err.message || 'Adres eklenemedi', 'error');
    } finally {
      setFormLoading(false);
    }
  };

  const onUpdate = async (data) => {
    if (!uid || !editingId) return;
    setFormLoading(true);
    try {
      await addressService.update(uid, editingId, {
        title: data.title.trim(),
        recipientName: data.recipientName.trim(),
        phone: data.phone.trim(),
        city: data.city.trim(),
        district: data.district.trim(),
        fullAddress: data.fullAddress.trim(),
        postalCode: data.postalCode?.trim() || undefined,
      });
      showToast('Adres güncellendi', 'success');
      setEditingId(null);
      await load();
    } catch (err) {
      showToast(err.message || 'Güncellenemedi', 'error');
    } finally {
      setFormLoading(false);
    }
  };

  const handleDelete = async (addressId) => {
    if (!uid || !window.confirm('Bu adresi silmek istediğinize emin misiniz?')) return;
    setActing({ id: addressId, kind: 'delete' });
    try {
      await addressService.remove(uid, addressId);
      showToast('Adres silindi', 'success');
      if (editingId === addressId) setEditingId(null);
      await load();
    } catch (err) {
      showToast(err.message || 'Silinemedi', 'error');
    } finally {
      setActing(null);
    }
  };

  const handleSetDefault = async (addressId) => {
    if (!uid) return;
    setActing({ id: addressId, kind: 'default' });
    try {
      await addressService.setDefault(uid, addressId);
      showToast('Varsayılan adres güncellendi', 'success');
      await load();
    } catch (err) {
      showToast(err.message || 'İşlem başarısız', 'error');
    } finally {
      setActing(null);
    }
  };

  if (!uid) {
    return (
      <div className="max-w-3xl mx-auto px-4 py-12 text-center text-gray-500">
        Oturum bilgisi bulunamadı.
      </div>
    );
  }

  const editingRow = editingId ? list.find((a) => a.id === editingId) : null;

  return (
    <div className="max-w-3xl mx-auto px-4 py-8">
      <Link
        to="/account"
        className="inline-flex items-center gap-2 text-sm text-gray-500 hover:text-primary mb-6"
      >
        <HiArrowLeft className="h-4 w-4" />
        Hesabıma dön
      </Link>

      <div className="flex items-center gap-3 mb-2">
        <HiMapPin className="h-8 w-8 text-primary" />
        <h1 className="text-2xl font-bold text-secondary">Adreslerim</h1>
      </div>
      <p className="text-sm text-gray-500 mb-6">
        Siparişlerinizde kullanacağınız teslimat adreslerini buradan yönetin.
      </p>

      {!showCreate && !editingId && (
        <Button type="button" className="mb-6" onClick={() => setShowCreate(true)}>
          Yeni adres ekle
        </Button>
      )}

      {(showCreate || editingId) && (
        <div className="bg-white rounded-xl border border-gray-100 p-6 mb-8">
          <h2 className="text-lg font-semibold text-secondary mb-4">
            {editingId ? 'Adresi düzenle' : 'Yeni adres'}
          </h2>
          <AddressForm
            key={editingId || 'new'}
            initialValues={editingId ? toFormValues(editingRow) : null}
            onSubmit={editingId ? onUpdate : onCreate}
            submitLabel={editingId ? 'Güncelle' : 'Kaydet'}
            loading={formLoading}
            onCancel={() => {
              setShowCreate(false);
              setEditingId(null);
            }}
          />
        </div>
      )}

      {loading ? (
        <LoadingSpinner />
      ) : error ? (
        <ErrorMessage message={error} onRetry={load} />
      ) : list.length === 0 && !showCreate ? (
        <div className="bg-white rounded-xl border border-gray-100 p-10 text-center text-gray-500">
          <p className="mb-4">Kayıtlı adresiniz yok.</p>
          <Button type="button" onClick={() => setShowCreate(true)}>
            İlk adresinizi ekleyin
          </Button>
        </div>
      ) : (
        <ul className="space-y-4">
          {list.map((row) => (
            <li
              key={row.id}
              className="bg-white rounded-xl border border-gray-100 p-5 flex flex-col gap-3"
            >
              <div className="flex flex-wrap items-start justify-between gap-2">
                <div>
                  <div className="flex flex-wrap items-center gap-2">
                    <span className="font-bold text-secondary">{row.title || 'Adres'}</span>
                    {isDefaultAddress(row) && (
                      <span className="text-xs font-semibold bg-primary/15 text-primary px-2 py-0.5 rounded">
                        Varsayılan
                      </span>
                    )}
                  </div>
                  <p className="text-sm text-gray-600 mt-1">
                    {row.recipientName || row.fullName} — {row.phone || row.phoneNumber}
                  </p>
                  <p className="text-sm text-gray-700 mt-2">
                    {row.district}, {row.city}
                  </p>
                  <p className="text-sm text-gray-600 mt-1">{row.fullAddress || row.addressLine}</p>
                  {row.postalCode && (
                    <p className="text-xs text-gray-500 mt-1">PK: {row.postalCode}</p>
                  )}
                </div>
                <div className="flex flex-wrap gap-2 shrink-0">
                  {!isDefaultAddress(row) && (
                    <Button
                      type="button"
                      size="sm"
                      variant="outline"
                      disabled={acting != null}
                      loading={acting?.id === row.id && acting?.kind === 'default'}
                      onClick={() => handleSetDefault(row.id)}
                      title="Varsayılan yap"
                    >
                      <HiStar className="h-4 w-4" />
                    </Button>
                  )}
                  <Button
                    type="button"
                    size="sm"
                    variant="outline"
                    disabled={acting != null || editingId === row.id}
                    onClick={() => {
                      setShowCreate(false);
                      setEditingId(row.id);
                    }}
                  >
                    <HiPencilSquare className="h-4 w-4" />
                  </Button>
                  <Button
                    type="button"
                    size="sm"
                    variant="danger"
                    disabled={acting != null}
                    loading={acting?.id === row.id && acting?.kind === 'delete'}
                    onClick={() => handleDelete(row.id)}
                  >
                    <HiTrash className="h-4 w-4" />
                  </Button>
                </div>
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
