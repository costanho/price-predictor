import client from './client';

export interface CartItem {
  id: string;
  product_id: string;
  quantity: number;
  unit_override?: string;
  notes?: string;
}

export interface CartOptimization {
  id: string;
  total_items: number;
  cheapest_single_total: number;
  cheapest_single_store: string;
  optimized_total: number;
  weekly_savings: number;
  annual_projection: number;
  store_breakdown: Record<string, number>;
  wait_one_month_total: number;
  wait_one_month_savings: number;
}

export const getCart = () => client.get<CartItem[]>('/cart');
export const addToCart = (productId: string, quantity: number) =>
  client.post('/cart', { product_id: productId, quantity });
export const updateCartItem = (itemId: string, quantity: number) =>
  client.put(`/cart/${itemId}`, { quantity });
export const removeFromCart = (itemId: string) => client.delete(`/cart/${itemId}`);
export const clearCart = () => client.post('/cart/clear');
export const getCartOptimization = () => client.get<CartOptimization>('/cart/optimization');
