import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { HiTag } from 'react-icons/hi2';
import { categoryService } from '../../services/categoryService';

export default function CategoryBar() {
  const [categories, setCategories] = useState([]);

  useEffect(() => {
    const fetchCategories = async () => {
      try {
        const { data: restResponse } = await categoryService.getFilters();
        setCategories(restResponse.data || []);
      } catch {
        // Silently fail — categories bar is non-critical
      }
    };

    fetchCategories();
  }, []);

  if (categories.length === 0) return null;

  return (
    <div className="bg-white border-b">
      <div className="max-w-7xl mx-auto px-4">
        <div className="flex items-center gap-1 overflow-x-auto py-3 scrollbar-hide">
          {categories.map((cat) => (
            <Link
              key={cat.id}
              to={`/?categoryId=${cat.id}`}
              className="flex flex-col items-center gap-1.5 px-4 py-2 rounded-lg hover:bg-gray-50 transition-colors shrink-0 group"
            >
              <HiTag className="h-5 w-5 text-gray-500 group-hover:text-primary transition-colors" />
              <span className="text-xs font-medium text-gray-600 group-hover:text-primary transition-colors whitespace-nowrap">
                {cat.name}
                {cat.productCount > 0 && (
                  <span className="text-gray-400 ml-1">({cat.productCount})</span>
                )}
              </span>
            </Link>
          ))}
        </div>
      </div>
    </div>
  );
}
