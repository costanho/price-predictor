import { useEffect, useState } from 'react';
import { getForecast, getAnomalyStatus, getBatchForecasts } from '../api/forecasts';
import useStore from '../store/useStore';

export function useForecasts(productId?: string) {
  const [forecasts, setForecasts] = useState<any[]>([]);
  const [anomaly, setAnomaly] = useState<any>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const userRegion = useStore((state) => state.userRegion);

  useEffect(() => {
    if (!productId) return;

    setLoading(true);
    Promise.all([
      getForecast(productId, userRegion),
      getAnomalyStatus(productId, userRegion)
    ])
      .then(([forecastRes, anomalyRes]) => {
        setForecasts(forecastRes.data);
        setAnomaly(anomalyRes.data);
      })
      .catch(() => setError('Failed to load forecasts'))
      .finally(() => setLoading(false));
  }, [productId, userRegion]);

  return { forecasts, anomaly, loading, error };
}

export function useBatchForecasts(productIds: string[] = []) {
  const [forecasts, setForecasts] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const userRegion = useStore((state) => state.userRegion);

  useEffect(() => {
    if (productIds.length === 0) return;

    setLoading(true);
    getBatchForecasts(productIds, userRegion)
      .then((res) => setForecasts(res.data))
      .catch(() => setError('Failed to load forecasts'))
      .finally(() => setLoading(false));
  }, [productIds.join(','), userRegion]);

  return { forecasts, loading, error };
}
