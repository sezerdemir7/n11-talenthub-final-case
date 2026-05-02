import { useState, useEffect, useCallback } from 'react';
import { useSearchParams } from 'react-router-dom';
import { HiTruck, HiShieldCheck, HiCreditCard } from 'react-icons/hi2';
import { productService } from '../services/productService';
import ProductGrid from '../components/product/ProductGrid';
import Pagination from '../components/product/Pagination';
import CategoryBar from '../components/layout/CategoryBar';

const VALUE_PROPS = [
  { icon: HiTruck, title: 'Ücretsiz Kargo', desc: '150 TL ve üzeri siparişlerde' },
  { icon: HiShieldCheck, title: 'Güvenli Alışveriş', desc: '256-bit SSL ile korumalı' },
  { icon: HiCreditCard, title: 'Kolay Ödeme', desc: 'Kredi/banka kartı desteği' },
];

export default function HomePage() {
  const [searchParams] = useSearchParams();
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const keyword = searchParams.get('keyword') || '';
  const categoryId = searchParams.get('categoryId') || '';
  const isFiltered = keyword || categoryId;

  const fetchProducts = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const { data: res } = await productService.getAll({
        page,
        size: 12,
        keyword: keyword || undefined,
        categoryId: categoryId || undefined,
      });
      const pageData = res.data;
      setProducts(pageData.content || []);
      setTotalPages(pageData.totalPages || 0);
    } catch (err) {
      setError(err.message || 'Ürünler yüklenirken bir hata oluştu');
    } finally {
      setLoading(false);
    }
  }, [page, keyword, categoryId]);

  useEffect(() => {
    fetchProducts();
  }, [fetchProducts]);

  useEffect(() => {
    setPage(0);
  }, [keyword, categoryId]);

  return (
    <>
      <CategoryBar />

      {!isFiltered && (
        <div className="bg-secondary">
          <div className="max-w-7xl mx-auto px-4 py-8">
            <div className="grid grid-cols-1 sm:grid-cols-3 divide-y sm:divide-y-0 sm:divide-x divide-white/10">
              {VALUE_PROPS.map(({ icon: Icon, title, desc }) => (
                <div key={title} className="flex items-center gap-4 py-4 sm:py-0 sm:px-8 first:pl-0 last:pr-0">
                  <div className="h-11 w-11 rounded-full bg-primary/20 flex items-center justify-center shrink-0">
                    <Icon className="h-5 w-5 text-primary" />
                  </div>
                  <div>
                    <p className="text-sm font-semibold text-white">{title}</p>
                    <p className="text-xs text-gray-400 mt-0.5">{desc}</p>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}

      <div className="max-w-7xl mx-auto px-4 py-8">
        <div className="flex items-center justify-between mb-6">
          <div>
            <h1 className="text-xl font-bold text-secondary">
              {keyword
                ? `"${keyword}" için arama sonuçları`
                : categoryId
                ? 'Kategori Ürünleri'
                : 'Tüm Ürünler'}
            </h1>
            {!loading && !error && products.length > 0 && (
              <p className="text-sm text-gray-500 mt-0.5">{products.length} ürün listeleniyor</p>
            )}
          </div>
        </div>

        <ProductGrid products={products} loading={loading} error={error} onRetry={fetchProducts} />

        <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
      </div>
    </>
  );
}
