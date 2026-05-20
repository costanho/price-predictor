import React, { useState } from "react";
import {
  StyleSheet,
  View,
  Text,
  ScrollView,
  Dimensions,
  Platform,
  TouchableOpacity,
  ActivityIndicator,
} from "react-native";
import { MaterialCommunityIcons, FontAwesome, MaterialIcons } from "@expo/vector-icons";

const { width } = Dimensions.get("window");
const isMobile = width < 768;

interface Alert {
  id: string;
  type: "price_drop" | "anomaly" | "deal_dna" | "forecast_update" | "volatility_warning" | "target_met";
  title: string;
  description: string;
  actionOrExtra?: string;
  extraInfo?: string;
  timestamp?: string;
  createdAt?: string;
  isRead?: boolean;
  expiresAt?: string;
}

const ALERT_STYLES: Record<string, { color: string; label: string; borderColor: string; backgroundColor: string }> = {
  price_drop: { color: "#16A34A", label: "Price drop", borderColor: "#166534", backgroundColor: "#E0F2E7" },
  anomaly: { color: "#DC2626", label: "Anomaly", borderColor: "#991B1B", backgroundColor: "#FDD4D8" },
  deal_dna: { color: "#92400E", label: "Deal pattern", borderColor: "#92400E", backgroundColor: "#F5E5D5" },
  forecast_update: { color: "#3B82F6", label: "Forecast", borderColor: "#3B82F6", backgroundColor: "#E0E7FF" },
  volatility_warning: { color: "#DC2626", label: "Volatile", borderColor: "#991B1B", backgroundColor: "#FDD4D8" },
  target_met: { color: "#16A34A", label: "Target met", borderColor: "#166534", backgroundColor: "#E0F2E7" },
};

function isExpiringSoon(expiresAt?: string): boolean {
  if (!expiresAt) return false;
  const expiry = new Date(expiresAt).getTime();
  const now = Date.now();
  const twentyFourHours = 24 * 60 * 60 * 1000;
  return expiry - now < twentyFourHours && expiry > now;
}

function formatDate(dateString?: string): string {
  if (!dateString) return "";
  const date = new Date(dateString);
  const now = new Date();
  const diffMs = now.getTime() - date.getTime();
  const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
  const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));

  if (diffHours < 1) return "Just now";
  if (diffHours < 24) return `${diffHours} hour${diffHours > 1 ? "s" : ""} ago`;
  if (diffDays < 7) return `${diffDays} day${diffDays > 1 ? "s" : ""} ago`;
  return date.toLocaleDateString();
}

const WebMobileAlerts: React.FC = () => {
  const [alerts, setAlerts] = useState<Alert[]>([
    {
      id: "1",
      type: "price_drop",
      title: "Eggs price dropped at Kroger",
      description: "Price dropped 12% to $4.99/lb — below your target price of $4.50.",
      actionOrExtra: "Action: Stock up while on sale",
      timestamp: "2 hours ago",
      isRead: false,
    },
    {
      id: "2",
      type: "anomaly",
      title: "Anomaly detected — Milk",
      description: "Price behavior is unusual. Historical patterns suggest a potential spike coming.",
      actionOrExtra: "Action: Consider buying before spike",
      timestamp: "5 hours ago",
      isRead: false,
    },
    {
      id: "3",
      type: "deal_dna",
      title: "Deal DNA trigger — Chicken breast",
      description: "Based on historical pattern, Kroger typically runs a sale this week.",
      extraInfo: "Next predicted: March 18-24",
      timestamp: "1 day ago",
      isRead: true,
    },
    {
      id: "4",
      type: "forecast_update",
      title: "Forecast update — Your basket",
      description: "Next month's total predicted at $62.40, down from $67.20 this month.",
      extraInfo: "Potential savings: $4.80",
      timestamp: "1 day ago",
      isRead: true,
    },
  ]);

  const handleMarkRead = (alertId: string) => {
    setAlerts(alerts.map(a => a.id === alertId ? { ...a, isRead: true } : a));
  };

  const handleDelete = (alertId: string) => {
    setAlerts(alerts.filter(a => a.id !== alertId));
  };

  const handleMarkAllRead = () => {
    setAlerts(alerts.map(a => ({ ...a, isRead: true })));
  };

  const unreadAlerts = alerts.filter((a) => !a.isRead);
  const readAlerts = alerts.filter((a) => a.isRead);
  const loading = false;

  const getAlertIcon = (type: Alert["type"]) => {
    const iconColor = ALERT_STYLES[type]?.color || "#666";
    const backgroundColor = ALERT_STYLES[type]?.backgroundColor || "#f0f0f0";

    const iconMap: Record<Alert["type"], keyof typeof MaterialIcons.glyphMap> = {
      price_drop: "trending-down",
      anomaly: "warning",
      deal_dna: "swap-horiz",
      forecast_update: "info",
      volatility_warning: "trending-up",
      target_met: "check-circle",
    };

    return (
      <View style={[styles.iconCircle, { backgroundColor }]}>
        <MaterialIcons name={iconMap[type]} size={20} color={iconColor} />
      </View>
    );
  };

  const AlertItem: React.FC<{ alert: Alert; isUnread: boolean }> = ({ alert, isUnread }) => {
    const style = ALERT_STYLES[alert.type];
    const expiresIn24h = isExpiringSoon(alert.expiresAt);

    return (
      <View style={[styles.alertCard, { backgroundColor: style?.backgroundColor, borderLeftColor: style?.borderColor }]}>
        {getAlertIcon(alert.type)}
        <View style={styles.alertContent}>
          <View style={styles.alertHeader}>
            <Text style={[styles.alertTitle, { color: style?.color }]}>
              {alert.title}
            </Text>
            {isUnread && <View style={styles.unreadDot} />}
          </View>
          <Text style={styles.alertDescription}>{alert.description}</Text>
          {alert.extraInfo && (
            <Text style={styles.alertExtraInfo}>{alert.extraInfo}</Text>
          )}
          {alert.actionOrExtra && !alert.extraInfo && (
            <Text style={styles.alertActionText}>{alert.actionOrExtra}</Text>
          )}
          {expiresIn24h && (
            <Text style={styles.expiryWarning}>⚠️ Expires in less than 24 hours</Text>
          )}
          <View style={styles.alertFooter}>
            <Text style={styles.alertTimestamp}>
              {alert.createdAt ? formatDate(alert.createdAt) : alert.timestamp || ""}
            </Text>
            <View style={styles.actionButtons}>
              {isUnread && (
                <TouchableOpacity
                  onPress={() => handleMarkRead(alert.id)}
                  style={styles.actionButton}
                >
                  <Text style={styles.actionButtonText}>Mark read</Text>
                </TouchableOpacity>
              )}
              <TouchableOpacity
                onPress={() => handleDelete(alert.id)}
                style={[styles.actionButton, styles.deleteButton]}
              >
                <Text style={styles.deleteButtonText}>Dismiss</Text>
              </TouchableOpacity>
            </View>
          </View>
        </View>
      </View>
    );
  };

  return (
    <ScrollView
      style={isMobile ? styles.containerMobile : styles.containerWeb}
    >
      <View style={isMobile ? styles.contentMobile : styles.contentWeb}>
        {unreadAlerts.length > 0 && (
          <TouchableOpacity
            onPress={handleMarkAllRead}
            style={styles.markAllReadButton}
          >
            <Text style={styles.markAllReadText}>Mark all as read ({unreadAlerts.length})</Text>
          </TouchableOpacity>
        )}

        {alerts.length === 0 ? (
          <View style={styles.emptyContainer}>
            <MaterialIcons name="notifications-none" size={48} color="#ccc" />
            <Text style={styles.emptyText}>No alerts yet</Text>
          </View>
        ) : (
          <>
            {unreadAlerts.length > 0 && (
              <View style={styles.section}>
                <Text style={styles.sectionTitle}>New</Text>
                <View style={styles.alertsList}>
                  {unreadAlerts.map((alert) => (
                    <AlertItem key={alert.id} alert={alert} isUnread={true} />
                  ))}
                </View>
              </View>
            )}

            {readAlerts.length > 0 && (
              <View style={styles.section}>
                <Text style={styles.sectionTitle}>Recent</Text>
                <View style={styles.alertsList}>
                  {readAlerts.map((alert) => (
                    <AlertItem key={alert.id} alert={alert} isUnread={false} />
                  ))}
                </View>
              </View>
            )}
          </>
        )}
      </View>
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  centerContainer: {
    flex: 1,
    justifyContent: "center",
    alignItems: "center",
    backgroundColor: "#FAFAF5",
  },

  // Mobile Styles
  containerMobile: {
    flex: 1,
    backgroundColor: "#FAFAF5",
  },
  contentMobile: {
    padding: 16,
    paddingTop: 16,
  },

  // Web Styles
  containerWeb: {
    flex: 1,
    backgroundColor: "#FAFAF5",
  },
  contentWeb: {
    padding: 24,
    paddingTop: 24,
    maxWidth: 1400,
    alignSelf: "center",
    width: "100%",
  },

  // Mark All Read Button
  markAllReadButton: {
    backgroundColor: "#3B82F6",
    paddingVertical: 12,
    paddingHorizontal: 16,
    borderRadius: 8,
    marginBottom: 20,
  },
  markAllReadText: {
    color: "#ffffff",
    fontSize: 14,
    fontWeight: "600",
    textAlign: "center",
  },

  // Empty State
  emptyContainer: {
    alignItems: "center",
    justifyContent: "center",
    paddingVertical: 60,
  },
  emptyText: {
    fontSize: 16,
    color: "#999",
    marginTop: 12,
  },

  // Shared Styles
  section: {
    marginBottom: 28,
  },
  sectionTitle: {
    fontSize: 13,
    fontWeight: "800",
    color: "#666666",
    marginBottom: 12,
    letterSpacing: 1.5,
    textTransform: "uppercase",
  },
  alertsList: {
    gap: 12,
  },
  alertCard: {
    flexDirection: "row",
    borderRadius: 12,
    padding: 16,
    borderLeftWidth: 4,
    ...Platform.select({
      ios: {
        shadowColor: "#000",
        shadowOffset: { width: 0, height: 1 },
        shadowOpacity: 0.05,
        shadowRadius: 4,
      },
      android: {
        elevation: 1,
      },
      web: {
        boxShadow: "0 1px 4px rgba(0,0,0,0.05)",
      },
    }),
  },

  // Alert Content
  iconCircle: {
    width: 44,
    height: 44,
    borderRadius: 10,
    justifyContent: "center",
    alignItems: "center",
    marginRight: 12,
    marginTop: 2,
    flexShrink: 0,
  },
  alertContent: {
    flex: 1,
  },
  alertHeader: {
    flexDirection: "row",
    alignItems: "center",
    marginBottom: 4,
  },
  alertTitle: {
    fontSize: 15,
    fontWeight: "700",
    flex: 1,
  },
  unreadDot: {
    width: 8,
    height: 8,
    borderRadius: 4,
    backgroundColor: "#3B82F6",
    marginLeft: 8,
  },
  alertDescription: {
    fontSize: 13,
    color: "#555555",
    marginBottom: 6,
    lineHeight: 19,
  },
  alertExtraInfo: {
    fontSize: 12,
    color: "#666666",
    marginBottom: 6,
    fontWeight: "500",
  },
  alertActionText: {
    fontSize: 12,
    color: "#666666",
    marginBottom: 6,
    fontWeight: "500",
  },
  expiryWarning: {
    fontSize: 12,
    color: "#DC2626",
    marginBottom: 8,
    fontWeight: "500",
  },
  alertFooter: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    marginTop: 10,
  },
  alertTimestamp: {
    fontSize: 11,
    color: "#888888",
  },
  actionButtons: {
    flexDirection: "row",
    gap: 8,
  },
  actionButton: {
    paddingHorizontal: 12,
    paddingVertical: 6,
    backgroundColor: "#f0f0f0",
    borderRadius: 6,
  },
  actionButtonText: {
    fontSize: 12,
    color: "#333333",
    fontWeight: "500",
  },
  deleteButton: {
    backgroundColor: "#fee2e2",
  },
  deleteButtonText: {
    fontSize: 12,
    color: "#dc2626",
    fontWeight: "500",
  },
});

export default WebMobileAlerts;
