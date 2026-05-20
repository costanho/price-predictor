import client from './client';

export const getAlerts = (unreadOnly = false, limit = 50) =>
  client.get('/alerts', { params: { unreadOnly, limit } });

export const getAlertCount = () =>
  client.get('/alerts/count');

export const getDashboardAlerts = () =>
  client.get('/alerts', { params: { unreadOnly: true, limit: 3 } });

export const markAlertRead = (alertId: string) =>
  client.patch(`/alerts/${alertId}/read`);

export const markAllAlertsRead = () =>
  client.patch('/alerts/read-all');

export const deleteAlert = (alertId: string) =>
  client.delete(`/alerts/${alertId}`);
