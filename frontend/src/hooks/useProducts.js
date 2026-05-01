import { useState, useEffect, useCallback } from 'react';
import { productService } from '../services/productService';

export function useProducts(params = {}) {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  const { page = 0, size = 12, keyword, categoryId, minPrice, maxPrice, sort } = params;

  const fetchProducts = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const { data: restResponse } = await productService.getAll({
        page, size, keyword, categoryId, minPrice, maxPrice, sort,
      });

      const pageData = restResponse.data;
      setProducts(pageData.content || []);
      setTotalPages(pageData.totalPages || 0);
      setTotalElements(pageData.totalElements || 0);
    } catch (err) {
      setError(err.message || 'Ürünler yüklenirken bir hata oluştu');
    } finally {
      setLoading(false);
    }
  }, [page, size, keyword, categoryId, minPrice, maxPrice, sort]);

  useEffect(() => {
    fetchProducts();
  }, [fetchProducts]);

  return { products, loading, error, totalPages, totalElements, refetch: fetchProducts };
}
