import client from './client';
import useStore from '../store/useStore';

export interface PriceForecast {
  id: string;
  product_id: string;
  region_code: string;
  forecast_month: string;
  current_price: number;
  predicted_price: number;
  percent_change: number;
  recommendation: 'buy_now' | 'wait';
  confidence_score: number;
  model_version: string;
  generated_at: string;
}

export interface AnomalyDetection {
  id: string;
  product_id: string;
  region_code: string;
  detection_date: string;
  is_anomalous: boolean;
  severity: 'none' | 'low' | 'medium' | 'high';
  notes?: string;
}

export const getForecast = (productId: string, region: string = 'national') => {
  const { userStores } = useStore.getState();
  const storesParam = userStores.length > 0 ? userStores.join(',') : 'all';
  return client.get(`/forecasts/${productId}`, {
    params: { region, stores: storesParam }
  });
};

export const getAnomalyStatus = (productId: string, region: string = 'national') =>
  client.get(`/anomaly/${productId}`, { params: { region } });

export const getBatchForecasts = (productIds: string[], region: string = 'national') => {
  const { userStores } = useStore.getState();
  const storesParam = userStores.length > 0 ? userStores.join(',') : 'all';
  return client.get('/forecasts/batch', {
    params: {
      productIds: productIds.join(','),
      region,
      stores: storesParam
    }
  });
};
