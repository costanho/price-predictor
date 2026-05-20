import client from './client';

export interface Product {
  id: string;
  name: string;
  short_name: string;
  category: string;
  unit: string;
  image_url?: string;
  is_active: boolean;
}

export interface PriceHistory {
  id: string;
  product_id: string;
  region_code: string;
  price: number;
  price_date: string;
  data_source: string;
}

export const getAllProducts = () => client.get<Product[]>('/products');
export const getProduct = (id: string) => client.get<Product>(`/products/${id}`);
export const searchProducts = (query: string) =>
  client.get<Product[]>('/products/search', { params: { q: query } });
export const getPriceHistory = (productId: string, regionCode?: string) =>
  client.get<PriceHistory[]>(`/products/${productId}/price-history`, { params: { region_code: regionCode } });
