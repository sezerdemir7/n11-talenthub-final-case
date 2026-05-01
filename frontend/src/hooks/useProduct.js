import { useState, useEffect } from 'react';
import { productService } from '../services/productService';

export function useProduct(id) {
  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!id) return;

    const fetchProduct = async () => {
      setLoading(true);
      setError(null);
      try {
        const { data: restResponse } = await productService.getById(id);
        setProduct(restResponse.data);
      } catch (err) {
        setError(err.message || 'Ürün bilgileri yüklenemedi');
      } finally {
        setLoading(false);
      }
    };

    fetchProduct();
  }, [id]);

  return { product, loading, error };
}
