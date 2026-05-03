import { useState, useEffect, useCallback, useMemo } from 'react';
import { useSearchParams } from 'react-router-dom';
import {
  HiAdjustmentsHorizontal,
  HiXMark,
  HiChevronDown,
  HiRectangleGroup,
  HiBuildingStorefront,
  HiBanknotes,
} from 'react-icons/hi2';
import { productService } from '../services/productService';
import { categoryService } from '../services/categoryService';
import { buildCategoryGroups, getChildrenOf } from '../utils/categoryTree';
import ProductGrid from '../components/product/ProductGrid';
import Pagination from '../components/product/Pagination';
import CategoryBar from '../components/layout/CategoryBar';

const SORT_OPTIONS = [
  { value: '', label: 'Önerilen' },
  { value: 'NEWEST', label: 'En Yeni' },
  { value: 'PRICE_ASC', label: 'En Ucuz' },
  { value: 'PRICE_DESC', label: 'En Pahalı' },
  { value: 'NAME_ASC', label: 'A – Z' },
];

const PRICE_PRESETS = [
  { label: '0 – 200 ₺', min: '', max: '200' },
  { label: '200 – 500 ₺', min: '200', max: '500' },
  { label: '500 – 1.000 ₺', min: '500', max: '1000' },
  { label: '1.000 ₺+', min: '1000', max: '' },
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
  const [expandedCategoryIds, setExpandedCategoryIds] = useState(() => new Set());
  const [expandedFilters, setExpandedFilters] = useState(
    () => new Set(['category', 'brand', 'price']),
  );

  const { roots, childrenByParent } = useMemo(
    () => buildCategoryGroups(categories),
    [categories],
  );

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

  useEffect(() => {
    if (!categoryId || categories.length === 0) return;
    const cat = categories.find((c) => String(c.id) === categoryId);
    if (!cat) return;
    const { childrenByParent: byParent } = buildCategoryGroups(categories);
    setExpandedCategoryIds((prev) => {
      const next = new Set(prev);
      if (cat.parentId != null) {
        next.add(Number(cat.parentId));
      } else if (getChildrenOf(cat.id, byParent).length > 0) {
        next.add(Number(cat.id));
      }
      return next;
    });
  }, [categoryId, categories]);

  const toggleCategoryExpand = (id) => {
    setExpandedCategoryIds((prev) => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  };

  const toggleFilterSection = (key) => {
    setExpandedFilters((prev) => {
      const next = new Set(prev);
      if (next.has(key)) next.delete(key);
      else next.add(key);
      return next;
    });
  };

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

  const applyPricePreset = (preset) => {
    setFilterForm((prev) => ({ ...prev, minPrice: preset.min, maxPrice: preset.max }));
  };

  const applyFilters = (e) => {
    e?.preventDefault();
    const next = new URLSearchParams(searchParams);
    filterForm.categoryId
      ? next.set('categoryId', filterForm.categoryId)
      : next.delete('categoryId');
    filterForm.brand.trim()
      ? next.set('brand', filterForm.brand.trim())
      : next.delete('brand');
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
  if (minPrice) activeTags.push({ key: 'minPrice', label: `Min: ${minPrice} ₺` });
  if (maxPrice) activeTags.push({ key: 'maxPrice', label: `Maks: ${maxPrice} ₺` });
  const hasActiveFilters = activeTags.length > 0;

  const activePricePreset = PRICE_PRESETS.find(
    (p) => p.min === filterForm.minPrice && p.max === filterForm.maxPrice,
  );

  const filterSectionHeader = (id, Icon, label) => {
    const open = expandedFilters.has(id);
    return (
      <button
        type="button"
        onClick={() => toggleFilterSection(id)}
        className="w-full flex items-center justify-between py-1 mb-2.5 cursor-pointer"
      >
        <span className="flex items-center gap-1.5 text-[11px] font-semibold text-gray-400 uppercase tracking-widest">
          <Icon className="h-3.5 w-3.5" />
          {label}
        </span>
        <HiChevronDown
          className={`h-3.5 w-3.5 text-gray-300 transition-transform duration-200 ${
            open ? 'rotate-180' : ''
          }`}
        />
      </button>
    );
  };

  const renderFilterFields = () => (
    <div className="space-y-1">
      {/* Category */}
      <div>
        {filterSectionHeader('category', HiRectangleGroup, 'Kategori')}
        {expandedFilters.has('category') && (
          <div className="max-h-52 overflow-y-auto space-y-0.5 pr-1 mb-1">
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
            {roots.map((cat) => {
              const children = getChildrenOf(cat.id, childrenByParent);
              const hasChildren = children.length > 0;
              const expanded = expandedCategoryIds.has(cat.id);
              const rootSelected = String(cat.id) === filterForm.categoryId;
              const childSelected = children.some((ch) => String(ch.id) === filterForm.categoryId);

              return (
                <div key={cat.id} className="space-y-0.5">
                  {hasChildren ? (
                    <>
                      <button
                        type="button"
                        onClick={() => toggleCategoryExpand(cat.id)}
                        className={`w-full text-left px-3 py-2 rounded-lg text-sm transition-colors flex items-center justify-between gap-2 ${
                          rootSelected || childSelected
                            ? 'bg-primary/10 text-primary font-semibold'
                            : expanded
                              ? 'bg-gray-50 text-secondary font-medium'
                              : 'text-gray-600 hover:bg-gray-50'
                        }`}
                      >
                        <span className="truncate flex items-center gap-2 min-w-0">
                          <HiChevronDown
                            className={`h-4 w-4 shrink-0 transition-transform duration-200 ${
                              expanded ? 'rotate-180' : ''
                            }`}
                          />
                          {cat.name}
                        </span>
                        {cat.productCount > 0 && (
                          <span className="text-xs text-gray-400 shrink-0">{cat.productCount}</span>
                        )}
                      </button>
                      {expanded && (
                        <div className="ml-2 pl-2 border-l border-gray-100 space-y-0.5">
                          <button
                            type="button"
                            onClick={() => handleFilterChange('categoryId', String(cat.id))}
                            className={`w-full text-left px-3 py-2 rounded-lg text-sm transition-colors ${
                              rootSelected
                                ? 'bg-primary/10 text-primary font-semibold'
                                : 'text-gray-600 hover:bg-gray-50'
                            }`}
                          >
                            Tümü
                          </button>
                          {children.map((child) => (
                            <button
                              key={child.id}
                              type="button"
                              onClick={() => handleFilterChange('categoryId', String(child.id))}
                              className={`w-full text-left px-3 py-2 rounded-lg text-sm transition-colors flex items-center justify-between gap-2 ${
                                String(child.id) === filterForm.categoryId
                                  ? 'bg-primary/10 text-primary font-semibold'
                                  : 'text-gray-600 hover:bg-gray-50'
                              }`}
                            >
                              <span className="truncate">{child.name}</span>
                              {child.productCount > 0 && (
                                <span className="text-xs text-gray-400 shrink-0">
                                  {child.productCount}
                                </span>
                              )}
                            </button>
                          ))}
                        </div>
                      )}
                    </>
                  ) : (
                    <button
                      type="button"
                      onClick={() => handleFilterChange('categoryId', String(cat.id))}
                      className={`w-full text-left px-3 py-2 rounded-lg text-sm transition-colors flex items-center justify-between gap-2 ${
                        rootSelected
                          ? 'bg-primary/10 text-primary font-semibold'
                          : 'text-gray-600 hover:bg-gray-50'
                      }`}
                    >
                      <span className="truncate">{cat.name}</span>
                      {cat.productCount > 0 && (
                        <span className="text-xs text-gray-400 shrink-0">{cat.productCount}</span>
                      )}
                    </button>
                  )}
                </div>
              );
            })}
          </div>
        )}
      </div>

      {/* Brand */}
      <div className="border-t border-gray-100 pt-4">
        {filterSectionHeader('brand', HiBuildingStorefront, 'Marka')}
        {expandedFilters.has('brand') && (
          <input
            type="text"
            value={filterForm.brand}
            onChange={(e) => handleFilterChange('brand', e.target.value)}
            placeholder="Örn. Samsung, Apple..."
            className="w-full border border-gray-200 rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/10 transition-colors placeholder:text-gray-300 mb-1"
          />
        )}
      </div>

      {/* Price range */}
      <div className="border-t border-gray-100 pt-4">
        {filterSectionHeader('price', HiBanknotes, 'Fiyat Aralığı')}
        {expandedFilters.has('price') && (
          <div className="space-y-3 mb-1">
            {/* Quick presets */}
            <div className="flex flex-wrap gap-1.5">
              {PRICE_PRESETS.map((preset) => {
                const isActive = activePricePreset?.label === preset.label;
                return (
                  <button
                    key={preset.label}
                    type="button"
                    onClick={() => applyPricePreset(preset)}
                    className={`px-2.5 py-1 text-xs rounded-full font-medium transition-all cursor-pointer ${
                      isActive
                        ? 'bg-primary text-white shadow-sm shadow-primary/25'
                        : 'bg-gray-100 text-gray-600 hover:bg-primary/10 hover:text-primary'
                    }`}
                  >
                    {preset.label}
                  </button>
                );
              })}
            </div>
            {/* Manual inputs */}
            <div className="flex items-center gap-2">
              <input
                type="number"
                min="0"
                value={filterForm.minPrice}
                onChange={(e) => handleFilterChange('minPrice', e.target.value)}
                placeholder="Min ₺"
                className="w-full border border-gray-200 rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/10 transition-colors placeholder:text-gray-300"
              />
              <span className="text-gray-300 shrink-0">—</span>
              <input
                type="number"
                min="0"
                value={filterForm.maxPrice}
                onChange={(e) => handleFilterChange('maxPrice', e.target.value)}
                placeholder="Maks ₺"
                className="w-full border border-gray-200 rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/10 transition-colors placeholder:text-gray-300"
              />
            </div>
          </div>
        )}
      </div>
    </div>
  );

  const pageTitle = keyword
    ? `"${keyword}" için sonuçlar`
    : categoryId
      ? categories.find((c) => String(c.id) === categoryId)?.name || 'Kategori Ürünleri'
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
                {totalElements > 0
                  ? `${totalElements.toLocaleString('tr-TR')} ürün listeleniyor`
                  : 'Ürün bulunamadı'}
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

            {/* Mobile sort select */}
            <div className="relative sm:hidden">
              <select
                value={sortBy}
                onChange={(e) => applySortBy(e.target.value)}
                className="appearance-none pl-3 pr-8 py-2 border border-gray-200 rounded-lg text-sm font-medium text-gray-600 focus:outline-none focus:border-primary bg-white hover:bg-gray-50 transition-colors cursor-pointer"
              >
                {SORT_OPTIONS.map((opt) => (
                  <option key={opt.value} value={opt.value}>
                    {opt.label}
                  </option>
                ))}
              </select>
              <HiChevronDown className="absolute right-2.5 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-400 pointer-events-none" />
            </div>

            {/* Desktop sort button tabs */}
            <div className="hidden sm:flex items-center bg-gray-100/70 rounded-xl p-1 gap-0.5">
              {SORT_OPTIONS.map((opt) => (
                <button
                  key={opt.value}
                  type="button"
                  onClick={() => applySortBy(opt.value)}
                  className={`px-3 py-1.5 text-xs rounded-lg transition-all cursor-pointer whitespace-nowrap font-medium ${
                    sortBy === opt.value
                      ? 'bg-white text-secondary shadow-sm font-semibold'
                      : 'text-gray-500 hover:text-gray-700 hover:bg-white/60'
                  }`}
                >
                  {opt.label}
                </button>
              ))}
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
                className="inline-flex items-center gap-1 px-3 py-1.5 bg-primary/8 text-primary rounded-full text-xs font-medium hover:bg-primary/15 transition-colors cursor-pointer border border-primary/15"
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
            <form
              onSubmit={applyFilters}
              className="bg-white border border-gray-100 rounded-2xl p-5 sticky top-24 shadow-sm"
            >
              <div className="flex items-center justify-between mb-5">
                <h2 className="text-sm font-bold text-secondary flex items-center gap-2">
                  <HiAdjustmentsHorizontal className="h-4 w-4 text-primary" />
                  Filtreler
                </h2>
                {hasActiveFilters && (
                  <button
                    type="button"
                    onClick={clearAllFilters}
                    className="inline-flex items-center gap-1 text-xs text-gray-400 hover:text-error transition-colors cursor-pointer"
                  >
                    <HiXMark className="h-3.5 w-3.5" />
                    Temizle
                  </button>
                )}
              </div>

              {renderFilterFields()}

              <button
                type="submit"
                className="mt-6 w-full bg-primary text-white rounded-xl py-2.5 text-sm font-semibold hover:bg-primary/90 transition-colors cursor-pointer shadow-sm shadow-primary/20"
              >
                Uygula
              </button>
            </form>
          </aside>

          {/* Products */}
          <section className="lg:col-span-3">
            <ProductGrid
              products={products}
              loading={loading}
              error={error}
              onRetry={fetchProducts}
            />
            <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
          </section>
        </div>
      </div>

      {/* Mobile filter drawer */}
      {mobileFiltersOpen && (
        <div className="fixed inset-0 z-[100] lg:hidden">
          <div
            className="absolute inset-0 bg-black/50 backdrop-blur-sm"
            onClick={() => setMobileFiltersOpen(false)}
          />
          <div className="absolute bottom-0 left-0 right-0 bg-white rounded-t-3xl flex flex-col max-h-[90vh] shadow-2xl">
            <div className="flex items-center justify-between px-5 py-4 border-b border-gray-100 shrink-0">
              <h2 className="text-base font-bold text-secondary flex items-center gap-2">
                <HiAdjustmentsHorizontal className="h-4 w-4 text-primary" />
                Filtreler
              </h2>
              <button
                type="button"
                onClick={() => setMobileFiltersOpen(false)}
                className="p-1.5 rounded-xl text-gray-400 hover:bg-gray-100 transition-colors cursor-pointer"
              >
                <HiXMark className="h-5 w-5" />
              </button>
            </div>
            <div className="overflow-y-auto flex-1 px-5 py-5">
              <form id="mobile-filter-form" onSubmit={applyFilters}>
                {renderFilterFields()}
              </form>
            </div>
            <div className="px-5 py-4 border-t border-gray-100 shrink-0 flex gap-3">
              <button
                type="button"
                onClick={clearAllFilters}
                className="flex-1 border border-gray-200 text-gray-600 rounded-xl py-2.5 text-sm font-semibold hover:bg-gray-50 transition-colors cursor-pointer"
              >
                Temizle
              </button>
              <button
                type="submit"
                form="mobile-filter-form"
                className="flex-1 bg-primary text-white rounded-xl py-2.5 text-sm font-semibold hover:bg-primary/90 transition-colors cursor-pointer shadow-sm shadow-primary/20"
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
