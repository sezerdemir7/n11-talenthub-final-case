import { useState, useEffect, useCallback } from 'react';
import { useSearchParams } from 'react-router-dom';
import { productService } from '../services/productService';
import ProductGrid from '../components/product/ProductGrid';
import Pagination from '../components/product/Pagination';
import HeroSlider from '../components/layout/HeroSlider';
import CategoryBar from '../components/layout/CategoryBar';

export default function HomePage() {
  const [searchParams] = useSearchParams();
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const keyword = searchParams.get('keyword') || '';
  const categoryId = searchParams.get('categoryId') || '';

  const fetchProducts = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const { data: restResponse } = await productService.getAll({
        page,
        size: 12,
        keyword: keyword || undefined,
        categoryId: categoryId || undefined,
      });

      const pageData = restResponse.data;
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

      <div className="max-w-7xl mx-auto px-4 py-6 space-y-8">
        {!keyword && !categoryId && <HeroSlider />}

        {/* Title */}
        <div>
          {keyword ? (
            <h1 className="text-2xl font-bold text-secondary">
              &quot;{keyword}&quot; için arama sonuçları
            </h1>
          ) : categoryId ? (
            <h1 className="text-2xl font-bold text-secondary">
              Kategori Ürünleri
            </h1>
          ) : (
            <h1 className="text-2xl font-bold text-secondary">
              Tüm Ürünler
            </h1>
          )}
        </div>

        {/* Product Grid */}
        <ProductGrid
          products={products}
          loading={loading}
          error={error}
          onRetry={fetchProducts}
        />

        {/* Pagination */}
        <Pagination
          currentPage={page}
          totalPages={totalPages}
          onPageChange={setPage}
        />
      </div>
    </>
  );
}
