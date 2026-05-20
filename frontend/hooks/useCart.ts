import { useEffect, useState } from 'react';
import { getCart, getCartOptimization, addToCart as addToCartAPI } from '../api/cart';
import useStore from '../store/useStore';

export function useCart() {
  const { cartItems, cartOptimization, setCartItems, setCartOptimization } = useStore();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const refreshCart = async () => {
    setLoading(true);
    try {
      const [cartRes, optRes] = await Promise.all([getCart(), getCartOptimization()]);
      setCartItems(cartRes.data);
      setCartOptimization(optRes.data);
    } catch (err) {
      setError('Failed to load cart');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    refreshCart();
  }, []);

  const addToCart = async (productId: string, quantity: number) => {
    try {
      await addToCartAPI(productId, quantity);
      await refreshCart();
    } catch (err) {
      setError('Failed to add to cart');
    }
  };

  return { cartItems, cartOptimization, loading, error, addToCart, refreshCart };
}
