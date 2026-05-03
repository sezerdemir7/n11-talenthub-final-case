import { useState, useEffect, useMemo } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { HiChevronDown } from 'react-icons/hi2';
import { categoryService } from '../../services/categoryService';
import { buildCategoryGroups, getChildrenOf } from '../../utils/categoryTree';

export default function CategoryBar() {
  const [categories, setCategories] = useState([]);
  const [searchParams] = useSearchParams();
  const activeCategoryId = searchParams.get('categoryId') || '';
  const [expandedRootId, setExpandedRootId] = useState(null);

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

  if (categories.length === 0) return null;

  const expandedChildren =
    expandedRootId != null ? getChildrenOf(expandedRootId, childrenByParent) : [];

  const pillBase =
    'shrink-0 px-4 py-2.5 text-sm font-medium rounded-lg transition-colors whitespace-nowrap inline-flex items-center gap-1';

  return (
    <div className="bg-white border-b border-gray-100">
      <div className="max-w-7xl mx-auto px-4 py-1">
        <div className="flex items-center gap-0.5 overflow-x-auto scrollbar-hide">
          <Link
            to="/"
            className={`${pillBase} ${
              !activeCategoryId
                ? 'text-primary bg-primary/8 font-semibold'
                : 'text-gray-600 hover:text-primary hover:bg-gray-50'
            }`}
          >
            Tümü
          </Link>
          {roots.map((cat) => {
            const isActive = String(cat.id) === activeCategoryId;
            const children = getChildrenOf(cat.id, childrenByParent);
            const hasChildren = children.length > 0;
            const isExpanded = expandedRootId === cat.id;

            if (!hasChildren) {
              return (
                <Link
                  key={cat.id}
                  to={`/?categoryId=${cat.id}`}
                  className={`${pillBase} ${
                    isActive
                      ? 'text-primary bg-primary/8 font-semibold'
                      : 'text-gray-600 hover:text-primary hover:bg-gray-50'
                  }`}
                >
                  {cat.name}
                  {cat.productCount > 0 && (
                    <span
                      className={`text-xs ${isActive ? 'text-primary/70' : 'text-gray-400'}`}
                    >
                      ({cat.productCount})
                    </span>
                  )}
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
                className={`${pillBase} cursor-pointer border-0 bg-transparent ${
                  isActive || isExpanded
                    ? 'text-primary bg-primary/8 font-semibold'
                    : 'text-gray-600 hover:text-primary hover:bg-gray-50'
                }`}
              >
                {cat.name}
                <HiChevronDown
                  className={`h-4 w-4 shrink-0 transition-transform ${isExpanded ? 'rotate-180' : ''}`}
                />
              </button>
            );
          })}
        </div>

        {expandedRootId != null && expandedChildren.length > 0 && (
          <div className="flex flex-wrap items-center gap-1 pt-2 pb-2 mt-1 border-t border-gray-100">
            <Link
              to={`/?categoryId=${expandedRootId}`}
              className={`shrink-0 px-3 py-1.5 text-xs font-semibold rounded-md transition-colors ${
                String(expandedRootId) === activeCategoryId
                  ? 'bg-primary text-white'
                  : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
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
                  className={`shrink-0 px-3 py-1.5 text-xs font-medium rounded-md transition-colors ${
                    childActive
                      ? 'bg-primary/15 text-primary ring-1 ring-primary/30'
                      : 'text-gray-600 bg-gray-50 hover:bg-gray-100'
                  }`}
                >
                  {child.name}
                  {child.productCount > 0 && (
                    <span className={`ml-1 ${childActive ? 'text-primary/70' : 'text-gray-400'}`}>
                      ({child.productCount})
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
