import { useEffect, useState } from 'react';
import { getAlerts, getAlertCount, getDashboardAlerts, markAlertRead, deleteAlert } from '../api/alerts';
import useStore from '../store/useStore';

export function useAlerts(dashboardOnly = false) {
  const { alerts, unreadCount, setAlerts, setUnreadCount } = useStore();
  const [loading, setLoading] = useState(alerts.length === 0);
  const [error, setError] = useState<string | null>(null);

  const refreshAlerts = async () => {
    setLoading(true);
    try {
      const alertRes = dashboardOnly
        ? await getDashboardAlerts()
        : await getAlerts(false, 50);
      const countRes = await getAlertCount();

      setAlerts(alertRes.data);
      setUnreadCount(countRes.data.unreadCount || 0);
    } catch (err) {
      setError('Failed to load alerts');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    refreshAlerts();
  }, [dashboardOnly]);

  const markAsRead = async (alertId: string) => {
    try {
      await markAlertRead(alertId);
      await refreshAlerts();
    } catch (err) {
      setError('Failed to mark as read');
    }
  };

  const remove = async (alertId: string) => {
    try {
      await deleteAlert(alertId);
      await refreshAlerts();
    } catch (err) {
      setError('Failed to delete alert');
    }
  };

  return { alerts, unreadCount, loading, error, markAsRead, remove, refreshAlerts };
}
