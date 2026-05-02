import { useState, useEffect, useCallback } from 'react';
import { useSearchParams } from 'react-router-dom';
import { HiAdjustmentsHorizontal, HiXMark, HiChevronDown } from 'react-icons/hi2';
import { productService } from '../services/productService';
import { categoryService } from '../services/categoryService';
import ProductGrid from '../components/product/ProductGrid';
import Pagination from '../components/product/Pagination';
import CategoryBar from '../components/layout/CategoryBar';

const SORT_OPTIONS = [
  { value: '', label: 'Önerilen' },
  { value: 'NEWEST', label: 'En Yeni' },
  { value: 'PRICE_ASC', label: 'Fiyat: Düşükten Yükseğe' },
  { value: 'PRICE_DESC', label: 'Fiyat: Yüksekten Düşüğe' },
  { value: 'NAME_ASC', label: 'İsim: A-Z' },
];

export default function HomePage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [categories, setCategories] = useState([]);
  const [mobileFiltersOpen, setMobileFiltersOpen] = useState(false);

  const keyword = searchParams.get('keyword') || '';
  const categoryId = searchParams.get('categoryId') || '';
  const brand = searchParams.get('brand') || '';
  const minPrice = searchParams.get('minPrice') || '';
  const maxPrice = searchParams.get('maxPrice') || '';
  const sortBy = searchParams.get('sortBy') || '';

  const [filterForm, setFilterForm] = useState({ categoryId, brand, minPrice, maxPrice });

  useEffect(() => {
    setFilterForm({ categoryId, brand, minPrice, maxPrice });
  }, [categoryId, brand, minPrice, maxPrice]);

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
      setTotalElements(pageData.totalElements ?? pageData.content?.length ?? 0);
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
    e?.preventDefault();
    const next = new URLSearchParams(searchParams);
    filterForm.categoryId ? next.set('categoryId', filterForm.categoryId) : next.delete('categoryId');
    filterForm.brand.trim() ? next.set('brand', filterForm.brand.trim()) : next.delete('brand');
    filterForm.minPrice ? next.set('minPrice', filterForm.minPrice) : next.delete('minPrice');
    filterForm.maxPrice ? next.set('maxPrice', filterForm.maxPrice) : next.delete('maxPrice');
    setSearchParams(next);
    setPage(0);
    setMobileFiltersOpen(false);
  };

  const applySortBy = (value) => {
    const next = new URLSearchParams(searchParams);
    value ? next.set('sortBy', value) : next.delete('sortBy');
    setSearchParams(next);
    setPage(0);
  };

  const removeFilter = (key) => {
    const next = new URLSearchParams(searchParams);
    next.delete(key);
    setSearchParams(next);
    setPage(0);
  };

  const clearAllFilters = () => {
    const next = new URLSearchParams(searchParams);
    ['categoryId', 'brand', 'minPrice', 'maxPrice', 'sortBy'].forEach((k) => next.delete(k));
    setSearchParams(next);
    setPage(0);
    setMobileFiltersOpen(false);
  };

  const activeTags = [];
  if (categoryId) {
    const cat = categories.find((c) => String(c.id) === categoryId);
    activeTags.push({ key: 'categoryId', label: cat?.name || 'Kategori' });
  }
  if (brand) activeTags.push({ key: 'brand', label: `Marka: ${brand}` });
  if (minPrice) activeTags.push({ key: 'minPrice', label: `Min: ${minPrice} TL` });
  if (maxPrice) activeTags.push({ key: 'maxPrice', label: `Maks: ${maxPrice} TL` });
  const hasActiveFilters = activeTags.length > 0;

  const renderFilterFields = () => (
    <div className="space-y-6">
      {/* Category */}
      <div>
        <p className="text-[11px] font-semibold text-gray-400 uppercase tracking-widest mb-2">Kategori</p>
        <div className="max-h-52 overflow-y-auto space-y-0.5 pr-1">
          <button
            type="button"
            onClick={() => handleFilterChange('categoryId', '')}
            className={`w-full text-left px-3 py-2 rounded-lg text-sm transition-colors ${
              !filterForm.categoryId
                ? 'bg-primary/10 text-primary font-semibold'
                : 'text-gray-600 hover:bg-gray-50'
            }`}
          >
            Tümü
          </button>
          {categories.map((cat) => (
            <button
              key={cat.id}
              type="button"
              onClick={() => handleFilterChange('categoryId', String(cat.id))}
              className={`w-full text-left px-3 py-2 rounded-lg text-sm transition-colors flex items-center justify-between gap-2 ${
                String(cat.id) === filterForm.categoryId
                  ? 'bg-primary/10 text-primary font-semibold'
                  : 'text-gray-600 hover:bg-gray-50'
              }`}
            >
              <span className="truncate">{cat.name}</span>
              {cat.productCount > 0 && (
                <span className="text-xs text-gray-400 shrink-0">{cat.productCount}</span>
              )}
            </button>
          ))}
        </div>
      </div>

      {/* Brand */}
      <div className="border-t border-gray-100 pt-5">
        <p className="text-[11px] font-semibold text-gray-400 uppercase tracking-widest mb-2">Marka</p>
        <input
          type="text"
          value={filterForm.brand}
          onChange={(e) => handleFilterChange('brand', e.target.value)}
          placeholder="Örn. Samsung, Apple..."
          className="w-full border border-gray-200 rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/10 transition-colors placeholder:text-gray-300"
        />
      </div>

      {/* Price range */}
      <div className="border-t border-gray-100 pt-5">
        <p className="text-[11px] font-semibold text-gray-400 uppercase tracking-widest mb-2">Fiyat Aralığı (TL)</p>
        <div className="flex items-center gap-2">
          <input
            type="number"
            min="0"
            value={filterForm.minPrice}
            onChange={(e) => handleFilterChange('minPrice', e.target.value)}
            placeholder="Min"
            className="w-full border border-gray-200 rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/10 transition-colors placeholder:text-gray-300"
          />
          <span className="text-gray-300 shrink-0">—</span>
          <input
            type="number"
            min="0"
            value={filterForm.maxPrice}
            onChange={(e) => handleFilterChange('maxPrice', e.target.value)}
            placeholder="Maks"
            className="w-full border border-gray-200 rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/10 transition-colors placeholder:text-gray-300"
          />
        </div>
      </div>
    </div>
  );

  const pageTitle = keyword
    ? `"${keyword}" için sonuçlar`
    : categoryId
    ? (categories.find((c) => String(c.id) === categoryId)?.name || 'Kategori Ürünleri')
    : 'Tüm Ürünler';

  return (
    <>
      <CategoryBar />

      <div className="max-w-7xl mx-auto px-4 py-6">
        {/* Top bar */}
        <div className="flex items-center justify-between gap-4 mb-5">
          <div>
            <h1 className="text-lg font-bold text-secondary">{pageTitle}</h1>
            {!loading && !error && (
              <p className="text-sm text-gray-400 mt-0.5">
                {totalElements > 0 ? `${totalElements} ürün listeleniyor` : 'Ürün bulunamadı'}
              </p>
            )}
          </div>

          <div className="flex items-center gap-2 shrink-0">
            {/* Mobile filter button */}
            <button
              type="button"
              onClick={() => setMobileFiltersOpen(true)}
              className="lg:hidden flex items-center gap-1.5 px-3 py-2 border border-gray-200 rounded-lg text-sm font-medium text-gray-600 hover:bg-gray-50 transition-colors cursor-pointer"
            >
              <HiAdjustmentsHorizontal className="h-4 w-4" />
              Filtrele
              {hasActiveFilters && (
                <span className="bg-primary text-white text-[10px] font-bold rounded-full h-4 w-4 flex items-center justify-center leading-none">
                  {activeTags.length}
                </span>
              )}
            </button>

            {/* Sort select */}
            <div className="relative">
              <select
                value={sortBy}
                onChange={(e) => applySortBy(e.target.value)}
                className="appearance-none pl-3 pr-8 py-2 border border-gray-200 rounded-lg text-sm font-medium text-gray-600 focus:outline-none focus:border-primary bg-white hover:bg-gray-50 transition-colors cursor-pointer"
              >
                {SORT_OPTIONS.map((opt) => (
                  <option key={opt.value} value={opt.value}>{opt.label}</option>
                ))}
              </select>
              <HiChevronDown className="absolute right-2.5 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-400 pointer-events-none" />
            </div>
          </div>
        </div>

        {/* Active filter tags */}
        {hasActiveFilters && (
          <div className="flex flex-wrap items-center gap-2 mb-5">
            <span className="text-xs text-gray-400 font-medium">Aktif:</span>
            {activeTags.map((tag) => (
              <button
                key={tag.key}
                type="button"
                onClick={() => removeFilter(tag.key)}
                className="inline-flex items-center gap-1 px-2.5 py-1 bg-primary/8 text-primary rounded-full text-xs font-medium hover:bg-primary/15 transition-colors cursor-pointer"
              >
                {tag.label}
                <HiXMark className="h-3.5 w-3.5" />
              </button>
            ))}
            <button
              type="button"
              onClick={clearAllFilters}
              className="text-xs text-gray-400 hover:text-gray-600 underline underline-offset-2 transition-colors cursor-pointer"
            >
              Tümünü temizle
            </button>
          </div>
        )}

        <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
          {/* Desktop filter sidebar */}
          <aside className="hidden lg:block lg:col-span-1">
            <form onSubmit={applyFilters} className="bg-white border border-gray-100 rounded-xl p-5 sticky top-24">
              <div className="flex items-center justify-between mb-5">
                <h2 className="text-sm font-bold text-secondary flex items-center gap-2">
                  <HiAdjustmentsHorizontal className="h-4 w-4 text-gray-400" />
                  Filtreler
                </h2>
                {hasActiveFilters && (
                  <button
                    type="button"
                    onClick={clearAllFilters}
                    className="text-xs text-gray-400 hover:text-error transition-colors cursor-pointer"
                  >
                    Temizle
                  </button>
                )}
              </div>

              {renderFilterFields()}

              <button
                type="submit"
                className="mt-6 w-full bg-primary text-white rounded-lg py-2.5 text-sm font-semibold hover:bg-primary/90 transition-colors cursor-pointer"
              >
                Uygula
              </button>
            </form>
          </aside>

          {/* Products */}
          <section className="lg:col-span-3">
            <ProductGrid products={products} loading={loading} error={error} onRetry={fetchProducts} />
            <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
          </section>
        </div>
      </div>

      {/* Mobile filter drawer */}
      {mobileFiltersOpen && (
        <div className="fixed inset-0 z-[100] lg:hidden">
          <div
            className="absolute inset-0 bg-black/50"
            onClick={() => setMobileFiltersOpen(false)}
          />
          <div className="absolute bottom-0 left-0 right-0 bg-white rounded-t-2xl flex flex-col max-h-[90vh]">
            <div className="flex items-center justify-between px-4 py-4 border-b border-gray-100 shrink-0">
              <h2 className="text-base font-bold text-secondary">Filtreler</h2>
              <button
                type="button"
                onClick={() => setMobileFiltersOpen(false)}
                className="p-1.5 rounded-lg text-gray-400 hover:bg-gray-100 transition-colors cursor-pointer"
              >
                <HiXMark className="h-5 w-5" />
              </button>
            </div>
            <div className="overflow-y-auto flex-1 px-4 py-5">
              <form id="mobile-filter-form" onSubmit={applyFilters}>
                {renderFilterFields()}
              </form>
            </div>
            <div className="px-4 py-4 border-t border-gray-100 shrink-0 flex gap-3">
              <button
                type="button"
                onClick={clearAllFilters}
                className="flex-1 border border-gray-200 text-gray-600 rounded-lg py-2.5 text-sm font-semibold hover:bg-gray-50 transition-colors cursor-pointer"
              >
                Temizle
              </button>
              <button
                type="submit"
                form="mobile-filter-form"
                className="flex-1 bg-primary text-white rounded-lg py-2.5 text-sm font-semibold hover:bg-primary/90 transition-colors cursor-pointer"
              >
                Uygula
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
