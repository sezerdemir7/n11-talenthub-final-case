import { useState, useEffect, useMemo, useRef } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { HiChevronDown, HiSquares2X2 } from 'react-icons/hi2';
import { categoryService } from '../../services/categoryService';
import { buildCategoryGroups, getChildrenOf } from '../../utils/categoryTree';

export default function CategoryBar() {
  const [categories, setCategories] = useState([]);
  const [searchParams] = useSearchParams();
  const activeCategoryId = searchParams.get('categoryId') || '';
  const [expandedRootId, setExpandedRootId] = useState(null);
  const scrollRef = useRef(null);
  const [showLeftFade, setShowLeftFade] = useState(false);
  const [showRightFade, setShowRightFade] = useState(false);

  const { roots, childrenByParent } = useMemo(
    () => buildCategoryGroups(categories),
    [categories],
  );

  useEffect(() => {
    const fetchCategories = async () => {
      try {
        const { data: res } = await categoryService.getFilters();
        setCategories(res.data || []);
      } catch {
        // kategori bar kritik değil, sessizce geç
      }
    };
    fetchCategories();
  }, []);

  useEffect(() => {
    if (categories.length === 0) return;
    if (!activeCategoryId) {
      setExpandedRootId(null);
      return;
    }
    const active = categories.find((c) => String(c.id) === activeCategoryId);
    if (!active) return;
    const { childrenByParent: byParent } = buildCategoryGroups(categories);
    if (active.parentId != null) {
      setExpandedRootId(Number(active.parentId));
    } else if (getChildrenOf(active.id, byParent).length > 0) {
      setExpandedRootId(Number(active.id));
    } else {
      setExpandedRootId(null);
    }
  }, [activeCategoryId, categories]);

  const updateFades = () => {
    const el = scrollRef.current;
    if (!el) return;
    setShowLeftFade(el.scrollLeft > 8);
    setShowRightFade(el.scrollLeft < el.scrollWidth - el.clientWidth - 8);
  };

  useEffect(() => {
    updateFades();
    const el = scrollRef.current;
    if (!el) return;
    el.addEventListener('scroll', updateFades, { passive: true });
    const ro = new ResizeObserver(updateFades);
    ro.observe(el);
    return () => {
      el.removeEventListener('scroll', updateFades);
      ro.disconnect();
    };
  }, [categories]);

  if (categories.length === 0) return null;

  const expandedChildren =
    expandedRootId != null ? getChildrenOf(expandedRootId, childrenByParent) : [];

  const pillBase =
    'shrink-0 px-4 py-2 text-sm rounded-full transition-all duration-150 whitespace-nowrap inline-flex items-center gap-1.5 font-medium';
  const pillActive = 'bg-primary text-white shadow-sm shadow-primary/30';
  const pillInactive = 'text-gray-600 hover:text-primary hover:bg-primary/8';

  return (
    <div className="bg-white border-b border-gray-100 shadow-sm">
      <div className="max-w-7xl mx-auto px-4">
        {/* Main row with scroll fades */}
        <div className="relative">
          {showLeftFade && (
            <div className="absolute left-0 top-0 bottom-0 w-14 bg-gradient-to-r from-white to-transparent z-10 pointer-events-none" />
          )}
          {showRightFade && (
            <div className="absolute right-0 top-0 bottom-0 w-14 bg-gradient-to-l from-white to-transparent z-10 pointer-events-none" />
          )}
          <div
            ref={scrollRef}
            className="flex items-center gap-1 overflow-x-auto scrollbar-hide py-3"
          >
            <Link
              to="/"
              className={`${pillBase} ${!activeCategoryId ? pillActive : pillInactive}`}
            >
              <HiSquares2X2 className="h-3.5 w-3.5" />
              Tümü
            </Link>

            {roots.map((cat) => {
              const isActive = String(cat.id) === activeCategoryId;
              const children = getChildrenOf(cat.id, childrenByParent);
              const hasChildren = children.length > 0;
              const isExpanded = expandedRootId === cat.id;
              const hasActiveChild =
                hasChildren && children.some((c) => String(c.id) === activeCategoryId);
              const isHighlighted = isActive || isExpanded || hasActiveChild;

              const countChip =
                cat.productCount > 0 ? (
                  <span
                    className={`text-xs px-1.5 py-0.5 rounded-full ${
                      isHighlighted ? 'bg-white/25 text-white' : 'bg-gray-100 text-gray-500'
                    }`}
                  >
                    {cat.productCount}
                  </span>
                ) : null;

              if (!hasChildren) {
                return (
                  <Link
                    key={cat.id}
                    to={`/?categoryId=${cat.id}`}
                    className={`${pillBase} ${isActive ? pillActive : pillInactive}`}
                  >
                    {cat.name}
                    {countChip}
                  </Link>
                );
              }

              return (
                <button
                  key={cat.id}
                  type="button"
                  aria-expanded={isExpanded}
                  onClick={() =>
                    setExpandedRootId((prev) => (prev === cat.id ? null : cat.id))
                  }
                  className={`${pillBase} border-0 cursor-pointer ${
                    isHighlighted ? pillActive : pillInactive
                  }`}
                >
                  {cat.name}
                  {countChip}
                  <HiChevronDown
                    className={`h-3.5 w-3.5 shrink-0 transition-transform duration-200 ${
                      isExpanded ? 'rotate-180' : ''
                    }`}
                  />
                </button>
              );
            })}
          </div>
        </div>

        {/* Subcategory row */}
        {expandedRootId != null && expandedChildren.length > 0 && (
          <div className="flex flex-wrap items-center gap-1.5 pb-3 pt-2 border-t border-gray-50">
            <Link
              to={`/?categoryId=${expandedRootId}`}
              className={`px-3 py-1.5 text-xs font-semibold rounded-full transition-all ${
                String(expandedRootId) === activeCategoryId
                  ? 'bg-secondary text-white'
                  : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
              }`}
            >
              Tümü
            </Link>
            {expandedChildren.map((child) => {
              const childActive = String(child.id) === activeCategoryId;
              return (
                <Link
                  key={child.id}
                  to={`/?categoryId=${child.id}`}
                  className={`px-3 py-1.5 text-xs font-medium rounded-full transition-all inline-flex items-center gap-1 ${
                    childActive
                      ? 'bg-primary text-white shadow-sm shadow-primary/25'
                      : 'text-gray-500 bg-gray-50 hover:bg-primary/8 hover:text-primary'
                  }`}
                >
                  {child.name}
                  {child.productCount > 0 && (
                    <span className={`text-xs ${childActive ? 'text-white/70' : 'text-gray-400'}`}>
                      {child.productCount}
                    </span>
                  )}
                </Link>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}
