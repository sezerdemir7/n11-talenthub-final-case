import { useState, useEffect } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { categoryService } from '../../services/categoryService';

export default function CategoryBar() {
  const [categories, setCategories] = useState([]);
  const [searchParams] = useSearchParams();
  const activeCategoryId = searchParams.get('categoryId') || '';

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

  if (categories.length === 0) return null;

  return (
    <div className="bg-white border-b border-gray-100">
      <div className="max-w-7xl mx-auto px-4">
        <div className="flex items-center gap-0.5 overflow-x-auto scrollbar-hide py-1">
          <Link
            to="/"
            className={`shrink-0 px-4 py-2.5 text-sm font-medium rounded-lg transition-colors whitespace-nowrap ${
              !activeCategoryId
                ? 'text-primary bg-primary/8 font-semibold'
                : 'text-gray-600 hover:text-primary hover:bg-gray-50'
            }`}
          >
            Tümü
          </Link>
          {categories.map((cat) => {
            const isActive = String(cat.id) === activeCategoryId;
            return (
              <Link
                key={cat.id}
                to={`/?categoryId=${cat.id}`}
                className={`shrink-0 px-4 py-2.5 text-sm font-medium rounded-lg transition-colors whitespace-nowrap ${
                  isActive
                    ? 'text-primary bg-primary/8 font-semibold'
                    : 'text-gray-600 hover:text-primary hover:bg-gray-50'
                }`}
              >
                {cat.name}
                {cat.productCount > 0 && (
                  <span className={`ml-1.5 text-xs ${isActive ? 'text-primary/70' : 'text-gray-400'}`}>
                    ({cat.productCount})
                  </span>
                )}
              </Link>
            );
          })}
        </div>
      </div>
    </div>
  );
}
