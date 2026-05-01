import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { HiTag, HiArrowRight, HiClipboardDocumentList, HiUsers } from 'react-icons/hi2';
import { categoryService } from '../../services/categoryService';

export default function AdminDashboard() {
  const [categoryCount, setCategoryCount] = useState(0);

  useEffect(() => {
    const fetchStats = async () => {
      try {
        const { data: restResponse } = await categoryService.getAll();
        setCategoryCount(restResponse.data?.length || 0);
      } catch {
        // ignore
      }
    };
    fetchStats();
  }, []);

  return (
    <div>
      <h2 className="text-2xl font-bold text-secondary mb-6">Dashboard</h2>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
        {/* Seller applications */}
        <div className="bg-white rounded-xl border border-gray-100 p-6">
          <div className="flex items-center justify-between mb-4">
            <div className="h-12 w-12 rounded-lg bg-primary/10 flex items-center justify-center">
              <HiClipboardDocumentList className="h-6 w-6 text-primary" />
            </div>
          </div>
          <h3 className="font-semibold text-secondary">Satıcı başvuruları</h3>
          <p className="text-sm text-gray-500 mt-1">Bekleyen başvuruları onaylayın veya reddedin</p>
          <Link
            to="/admin/seller-applications"
            className="inline-flex items-center gap-1 text-sm text-primary font-medium mt-4 hover:underline"
          >
            Başvuruları aç <HiArrowRight className="h-4 w-4" />
          </Link>
        </div>

        <div className="bg-white rounded-xl border border-gray-100 p-6">
          <div className="flex items-center justify-between mb-4">
            <div className="h-12 w-12 rounded-lg bg-primary/10 flex items-center justify-center">
              <HiUsers className="h-6 w-6 text-primary" />
            </div>
          </div>
          <h3 className="font-semibold text-secondary">Satıcılar</h3>
          <p className="text-sm text-gray-500 mt-1">Onaylı satıcıları askıya alın veya askıdakileri tekrar onaylayın</p>
          <Link
            to="/admin/sellers"
            className="inline-flex items-center gap-1 text-sm text-primary font-medium mt-4 hover:underline"
          >
            Listeyi aç <HiArrowRight className="h-4 w-4" />
          </Link>
        </div>

        {/* Category Card */}
        <div className="bg-white rounded-xl border border-gray-100 p-6">
          <div className="flex items-center justify-between mb-4">
            <div className="h-12 w-12 rounded-lg bg-primary/10 flex items-center justify-center">
              <HiTag className="h-6 w-6 text-primary" />
            </div>
            <span className="text-3xl font-black text-secondary">{categoryCount}</span>
          </div>
          <h3 className="font-semibold text-secondary">Kategoriler</h3>
          <p className="text-sm text-gray-500 mt-1">Toplam kategori sayısı</p>
          <Link
            to="/admin/categories"
            className="inline-flex items-center gap-1 text-sm text-primary font-medium mt-4 hover:underline"
          >
            Yönet <HiArrowRight className="h-4 w-4" />
          </Link>
        </div>
      </div>
    </div>
  );
}
