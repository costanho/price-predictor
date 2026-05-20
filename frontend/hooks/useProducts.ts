import { useEffect, useState } from 'react';
import { getAllProducts } from '../api/products';
import useStore from '../store/useStore';

export function useProducts() {
  const { products, setProducts } = useStore();
  const [loading, setLoading] = useState(products.length === 0);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (products.length > 0) return;
    getAllProducts()
      .then((res) => setProducts(res.data))
      .catch(() => setError('Failed to load products'))
      .finally(() => setLoading(false));
  }, []);

  return { products, loading, error };
}
