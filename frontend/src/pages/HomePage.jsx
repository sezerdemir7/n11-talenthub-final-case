import { useState, useEffect, useCallback } from 'react';
import { useSearchParams } from 'react-router-dom';
import { HiAdjustmentsHorizontal, HiXMark } from 'react-icons/hi2';
import { productService } from '../services/productService';
import { categoryService } from '../services/categoryService';
import ProductGrid from '../components/product/ProductGrid';
import Pagination from '../components/product/Pagination';
import CategoryBar from '../components/layout/CategoryBar';

export default function HomePage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [categories, setCategories] = useState([]);
  const [mobileFiltersOpen, setMobileFiltersOpen] = useState(false);

  const keyword = searchParams.get('keyword') || '';
  const categoryId = searchParams.get('categoryId') || '';
  const brand = searchParams.get('brand') || '';
  const minPrice = searchParams.get('minPrice') || '';
  const maxPrice = searchParams.get('maxPrice') || '';
  const sortBy = searchParams.get('sortBy') || '';
  const isFiltered = keyword || categoryId || brand || minPrice || maxPrice || sortBy;
  const showFilterPanel = Boolean(isFiltered);

  const [filterForm, setFilterForm] = useState({
    categoryId,
    brand,
    minPrice,
    maxPrice,
    sortBy,
  });

  useEffect(() => {
    setFilterForm({
      categoryId,
      brand,
      minPrice,
      maxPrice,
      sortBy,
    });
  }, [categoryId, brand, minPrice, maxPrice, sortBy]);

  useEffect(() => {
    const fetchCategories = async () => {
      try {
        const { data: res } = await categoryService.getFilters();
        setCategories(res.data || []);
      } catch {
        setCategories([]);
      }
    };
    fetchCategories();
  }, []);

  const fetchProducts = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const { data: res } = await productService.getAll({
        page,
        size: 12,
        keyword: keyword || undefined,
        categoryId: categoryId || undefined,
        brand: brand || undefined,
        minPrice: minPrice || undefined,
        maxPrice: maxPrice || undefined,
        sortBy: sortBy || undefined,
        active: true,
      });
      const pageData = res.data;
      setProducts(pageData.content || []);
      setTotalPages(pageData.totalPages || 0);
    } catch (err) {
      setError(err.message || 'Ürünler yüklenirken bir hata oluştu');
    } finally {
      setLoading(false);
    }
  }, [page, keyword, categoryId, brand, minPrice, maxPrice, sortBy]);

  useEffect(() => {
    fetchProducts();
  }, [fetchProducts]);

  useEffect(() => {
    setPage(0);
  }, [keyword, categoryId, brand, minPrice, maxPrice, sortBy]);

  const handleFilterChange = (field, value) => {
    setFilterForm((prev) => ({ ...prev, [field]: value }));
  };

  const applyFilters = (e) => {
    e.preventDefault();
    const next = new URLSearchParams(searchParams);

    if (filterForm.categoryId) next.set('categoryId', filterForm.categoryId);
    else next.delete('categoryId');

    if (filterForm.brand.trim()) next.set('brand', filterForm.brand.trim());
    else next.delete('brand');

    if (filterForm.minPrice) next.set('minPrice', filterForm.minPrice);
    else next.delete('minPrice');

    if (filterForm.maxPrice) next.set('maxPrice', filterForm.maxPrice);
    else next.delete('maxPrice');

    if (filterForm.sortBy) next.set('sortBy', filterForm.sortBy);
    else next.delete('sortBy');

    setSearchParams(next);
    setPage(0);
    setMobileFiltersOpen(false);
  };

  const clearFilters = () => {
    const next = new URLSearchParams(searchParams);
    next.delete('categoryId');
    next.delete('brand');
    next.delete('minPrice');
    next.delete('maxPrice');
    next.delete('sortBy');
    setSearchParams(next);
    setPage(0);
    setMobileFiltersOpen(false);
  };

  const renderFilterFormFields = () => (
    <>
      <div>
        <label className="block text-xs font-semibold text-gray-500 mb-1">Kategori</label>
        <select
          value={filterForm.categoryId}
          onChange={(e) => handleFilterChange('categoryId', e.target.value)}
          className="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-primary"
        >
          <option value="">Tümü</option>
          {categories.map((cat) => (
            <option key={cat.id} value={cat.id}>
              {cat.name}
            </option>
          ))}
        </select>
      </div>

      <div>
        <label className="block text-xs font-semibold text-gray-500 mb-1">Marka</label>
        <input
          type="text"
          value={filterForm.brand}
          onChange={(e) => handleFilterChange('brand', e.target.value)}
          placeholder="Orn. Xiaomi"
          className="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-primary"
        />
      </div>

      <div className="grid grid-cols-2 gap-2">
        <div>
          <label className="block text-xs font-semibold text-gray-500 mb-1">Min Fiyat</label>
          <input
            type="number"
            min="0"
            step="0.01"
            value={filterForm.minPrice}
            onChange={(e) => handleFilterChange('minPrice', e.target.value)}
            className="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-primary"
          />
        </div>
        <div>
          <label className="block text-xs font-semibold text-gray-500 mb-1">Max Fiyat</label>
          <input
            type="number"
            min="0"
            step="0.01"
            value={filterForm.maxPrice}
            onChange={(e) => handleFilterChange('maxPrice', e.target.value)}
            className="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-primary"
          />
        </div>
      </div>

      <div>
        <label className="block text-xs font-semibold text-gray-500 mb-1">Siralama</label>
        <select
          value={filterForm.sortBy}
          onChange={(e) => handleFilterChange('sortBy', e.target.value)}
          className="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-primary"
        >
          <option value="">Varsayilan</option>
          <option value="PRICE_ASC">Fiyat (Artan)</option>
          <option value="PRICE_DESC">Fiyat (Azalan)</option>
          <option value="NEWEST">En Yeni</option>
          <option value="NAME_ASC">Isim (A-Z)</option>
          <option value="NAME_DESC">Isim (Z-A)</option>
        </select>
      </div>
    </>
  );

  return (
    <>
      <CategoryBar />

      <div className="max-w-7xl mx-auto px-4 py-8">
        {showFilterPanel && (
          <div className="lg:hidden mb-4">
            <button
              type="button"
              onClick={() => setMobileFiltersOpen(true)}
              className="w-full flex items-center justify-center gap-2 bg-white border border-gray-200 rounded-lg py-2.5 text-sm font-semibold text-secondary hover:bg-gray-50 transition-colors cursor-pointer"
            >
              <HiAdjustmentsHorizontal className="h-5 w-5" />
              Filtrele ve Sirala
            </button>
          </div>
        )}

        {showFilterPanel && mobileFiltersOpen && (
          <div className="fixed inset-0 z-[100] lg:hidden">
            <div className="absolute inset-0 bg-black/40" onClick={() => setMobileFiltersOpen(false)} />
            <div className="absolute bottom-0 left-0 right-0 bg-white rounded-t-2xl p-4 max-h-[85vh] overflow-y-auto">
              <div className="flex items-center justify-between mb-4">
                <h2 className="text-base font-bold text-secondary">Filtrele</h2>
                <button
                  type="button"
                  onClick={() => setMobileFiltersOpen(false)}
                  className="p-1 rounded-md text-gray-500 hover:bg-gray-100 cursor-pointer"
                >
                  <HiXMark className="h-5 w-5" />
                </button>
              </div>
              <form onSubmit={applyFilters} className="space-y-4">
                {renderFilterFormFields()}
                <div className="flex gap-2 pt-2">
                  <button
                    type="submit"
                    className="flex-1 bg-primary text-white rounded-lg py-2 text-sm font-semibold hover:bg-primary/90 transition-colors cursor-pointer"
                  >
                    Uygula
                  </button>
                  <button
                    type="button"
                    onClick={clearFilters}
                    className="flex-1 border border-gray-200 text-gray-600 rounded-lg py-2 text-sm font-semibold hover:bg-gray-50 transition-colors cursor-pointer"
                  >
                    Temizle
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}

        <div className={`grid grid-cols-1 gap-6 ${showFilterPanel ? 'lg:grid-cols-4' : ''}`}>
          {showFilterPanel && (
            <aside className="hidden lg:block lg:col-span-1">
              <form onSubmit={applyFilters} className="bg-white border border-gray-100 rounded-xl p-4 space-y-4 sticky top-24">
                <h2 className="text-base font-bold text-secondary">Filtrele</h2>
                {renderFilterFormFields()}
                <div className="flex gap-2 pt-2">
                  <button
                    type="submit"
                    className="flex-1 bg-primary text-white rounded-lg py-2 text-sm font-semibold hover:bg-primary/90 transition-colors cursor-pointer"
                  >
                    Uygula
                  </button>
                  <button
                    type="button"
                    onClick={clearFilters}
                    className="flex-1 border border-gray-200 text-gray-600 rounded-lg py-2 text-sm font-semibold hover:bg-gray-50 transition-colors cursor-pointer"
                  >
                    Temizle
                  </button>
                </div>
              </form>
            </aside>
          )}

          <section className={showFilterPanel ? 'lg:col-span-3' : ''}>
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
          </section>
        </div>
        {!isFiltered && (
          <p className="text-xs text-gray-400 mt-4">Aradiginiz urune daha hizli ulasmak icin filtreleri kullanabilirsiniz.</p>
        )}
      </div>
    </>
  );
}
