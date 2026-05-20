import React from "react";
import {
  StyleSheet,
  View,
  Text,
  ScrollView,
  Dimensions,
  Platform,
} from "react-native";

const { width } = Dimensions.get("window");
const isMobile = width < 768;
const CARD_WIDTH = 180;

interface StatCardProps {
  title: string;
  value: string;
  subtitle: string;
  isPositive?: boolean;
}

const StatCard: React.FC<StatCardProps> = ({
  title,
  value,
  subtitle,
  isPositive = true,
}) => (
  <View style={[styles.statCard, isMobile && styles.statCardMobile]}>
    <Text style={styles.statTitle}>{title}</Text>
    <Text style={styles.statValue}>{value}</Text>
    <Text style={[styles.statSubtitle, isPositive && styles.positiveText]}>
      {isPositive ? "↑" : "↓"} {subtitle}
    </Text>
  </View>
);

interface PriceCardProps {
  emoji: string;
  name: string;
  currentPrice: string;
  forecastPrice: string;
  action: "buy" | "wait";
  percentage: string;
}

const PriceCard: React.FC<PriceCardProps> = ({
  emoji,
  name,
  currentPrice,
  forecastPrice,
  action,
  percentage,
}) => {
  if (isMobile) {
    return (
      <View style={styles.priceCardMobile}>
        <View style={styles.mobileCardHeader}>
          <View style={styles.emojiContainer}>
            <Text style={styles.emoji}>{emoji}</Text>
          </View>
          <Text style={styles.mobileProductName}>{name}</Text>
        </View>
        <View style={styles.mobilePriceRow}>
          <Text style={styles.mobilePriceLabel}>Now</Text>
          <Text style={styles.mobilePriceValue}>{currentPrice}</Text>
        </View>
        <View style={styles.mobilePriceRow}>
          <Text style={styles.mobilePriceLabel}>Next mo.</Text>
          <Text
            style={[
              styles.mobilePriceValue,
              action === "buy" ? styles.buyText : styles.waitText,
            ]}
          >
            {forecastPrice}
          </Text>
        </View>
        <View
          style={[
            styles.actionButton,
            action === "buy" ? styles.buyButton : styles.waitButton,
          ]}
        >
          <Text
            style={[
              styles.actionText,
              action === "buy" ? styles.buyText : styles.waitText,
            ]}
          >
            {action === "buy" ? "Buy now" : "Wait"}{" "}
            {action === "buy" ? "↓" : "↑"} {percentage}
          </Text>
        </View>
      </View>
    );
  }

  return (
    <View style={styles.priceCard}>
      <View style={styles.priceHeader}>
        <Text style={styles.emoji}>{emoji}</Text>
        <View style={styles.priceInfo}>
          <Text style={styles.productName}>{name}</Text>
          <Text style={styles.currentPrice}>
            Current: {currentPrice} → Forecast: {forecastPrice}
          </Text>
        </View>
      </View>
      <View
        style={[
          styles.actionButton,
          action === "buy" ? styles.buyButton : styles.waitButton,
        ]}
      >
        <Text
          style={[
            styles.actionText,
            action === "buy" ? styles.buyText : styles.waitText,
          ]}
        >
          {action === "buy" ? "Buy now" : "Wait"}{" "}
          {action === "buy" ? "↓" : "↑"} {percentage}
        </Text>
      </View>
    </View>
  );
};

interface InflationItemProps {
  score: number;
  name: string;
  description: string;
  volatility: "high" | "low";
}

const InflationItem: React.FC<InflationItemProps> = ({
  score,
  name,
  description,
  volatility,
}) => (
  <View style={styles.inflationItem}>
    <View
      style={[
        styles.scoreCircle,
        volatility === "high" ? styles.highVolatility : styles.lowVolatility,
      ]}
    >
      <Text
        style={[
          styles.scoreText,
          volatility === "high"
            ? styles.highVolatilityText
            : styles.lowVolatilityText,
        ]}
      >
        {score}
      </Text>
    </View>
    <View style={styles.inflationInfo}>
      <Text style={styles.inflationName}>{name}</Text>
      <Text style={styles.inflationDescription}>{description}</Text>
    </View>
  </View>
);

export default function DashboardDefaultView() {
  const mockForecasts = [
    { emoji: "🥚", name: "Eggs", currentPrice: "$4.82", forecastPrice: "$4.20", action: "buy" as const, percentage: "12.8%" },
    { emoji: "🥛", name: "Milk", currentPrice: "$3.91", forecastPrice: "$3.55", action: "wait" as const, percentage: "9.2%" },
    { emoji: "🍞", name: "Bread", currentPrice: "$3.24", forecastPrice: "$2.98", action: "buy" as const, percentage: "8.0%" },
    { emoji: "🍗", name: "Chicken", currentPrice: "$8.50", forecastPrice: "$7.99", action: "wait" as const, percentage: "6.0%" },
  ];

  const mockInflationItems = [
    { score: 78, name: "Dairy products", description: "Stable pricing, low volatility", volatility: "low" as const },
    { score: 45, name: "Proteins", description: "Fluctuating due to supply chain", volatility: "high" as const },
    { score: 89, name: "Produce", description: "Strong downward trend", volatility: "low" as const },
  ];

  return (
    <ScrollView style={styles.container}>
      <View style={styles.content}>
        {/* Stats Section */}
        {isMobile ? (
          <ScrollView
            horizontal
            showsHorizontalScrollIndicator={false}
            style={styles.statsScrollView}
            contentContainerStyle={styles.statsScrollContent}
          >
            <StatCard
              title="Monthly savings"
              value="$47.20"
              subtitle="$12 from last month"
              isPositive={true}
            />
            <StatCard
              title="Tracked products"
              value="23"
              subtitle="5 volatile items"
              isPositive={false}
            />
            <StatCard
              title="Avg prediction error"
              value="3.03%"
              subtitle="0.2% improvement"
              isPositive={true}
            />
            <StatCard
              title="Annual projection"
              value="$566"
              subtitle="Based on current trends"
              isPositive={false}
            />
          </ScrollView>
        ) : (
          <View style={styles.statsContainer}>
            <StatCard
              title="Monthly savings"
              value="$47.20"
              subtitle="$12 from last month"
              isPositive={true}
            />
            <StatCard
              title="Tracked products"
              value="23"
              subtitle="5 volatile items"
              isPositive={false}
            />
            <StatCard
              title="Avg prediction error"
              value="3.03%"
              subtitle="0.2% improvement"
              isPositive={true}
            />
            <StatCard
              title="Annual projection"
              value="$566"
              subtitle="Based on current trends"
              isPositive={false}
            />
          </View>
        )}

        {/* Price Forecasts Section */}
        <Text style={styles.sectionTitle}>Price forecasts</Text>
        {mockForecasts.length > 0 ? (
          isMobile ? (
            <ScrollView
              horizontal
              showsHorizontalScrollIndicator={false}
              style={styles.priceScrollView}
              contentContainerStyle={styles.priceScrollContent}
            >
              {mockForecasts.slice(0, 4).map((item: any, index: number) => (
                <PriceCard
                  key={index}
                  emoji={item.emoji}
                  name={item.name}
                  currentPrice={item.currentPrice}
                  forecastPrice={item.forecastPrice}
                  action={item.action}
                  percentage={item.percentage}
                />
              ))}
            </ScrollView>
          ) : (
            <View style={styles.forecastsSection}>
              <View style={styles.sectionHeader}>
                <Text style={styles.sectionTitle}>
                  Price forecasts — next 30 days
                </Text>
                <Text style={styles.viewAll}>View all →</Text>
              </View>
              {mockForecasts.map((item: any, index: number) => (
                <PriceCard
                  key={index}
                  emoji={item.emoji}
                  name={item.name}
                  currentPrice={item.currentPrice}
                  forecastPrice={item.forecastPrice}
                  action={item.action}
                  percentage={item.percentage}
                />
              ))}
            </View>
          )
        ) : (
          <Text style={{ padding: 16, color: '#999' }}>No forecasts available</Text>
        )}

        {/* Inflation Shield Section */}
        {isMobile ? (
          <View style={styles.inflationSectionMobile}>
            <Text style={styles.sectionTitle}>Inflation shield</Text>
            {mockInflationItems.map((item: any, index: number) => (
              <InflationItem
                key={index}
                score={item.score}
                name={item.name}
                description={item.description}
                volatility={item.volatility}
              />
            ))}
          </View>
        ) : (
          <View style={styles.mainGrid}>
            <View style={styles.forecastsSection}>
              <View style={styles.sectionHeader}>
                <Text style={styles.sectionTitle}>
                  Price forecasts — next 30 days
                </Text>
                <Text style={styles.viewAll}>View all →</Text>
              </View>
              {mockForecasts.map((item: any, index: number) => (
                <PriceCard
                  key={index}
                  emoji={item.emoji}
                  name={item.name}
                  currentPrice={item.currentPrice}
                  forecastPrice={item.forecastPrice}
                  action={item.action}
                  percentage={item.percentage}
                />
              ))}
            </View>

            {/* Inflation Shield Section */}
            <View style={styles.inflationSection}>
              <Text style={styles.sectionTitle}>Inflation shield</Text>
              {mockInflationItems.map((item: any, index: number) => (
                <InflationItem
                  key={index}
                  score={item.score}
                  name={item.name}
                  description={item.description}
                  volatility={item.volatility}
                />
              ))}
            </View>
          </View>
        )}

        {/* Recent Alerts Section */}
        {!isMobile && (
          <View style={styles.alertsSection}>
            <View style={styles.sectionHeader}>
              <Text style={styles.sectionTitle}>Recent alerts</Text>
              <Text style={styles.viewAll}>View all →</Text>
            </View>
            <View style={styles.alertsTable}>
              <View style={styles.tableHeader}>
                <Text style={[styles.tableHeaderText, { flex: 2 }]}>
                  Product
                </Text>
                <Text style={[styles.tableHeaderText, { flex: 2 }]}>
                  Alert type
                </Text>
                <Text style={[styles.tableHeaderText, { flex: 2 }]}>
                  Action
                </Text>
                <Text style={[styles.tableHeaderText, { flex: 1 }]}>Time</Text>
              </View>
              <View style={styles.tableRow}>
                <Text style={[styles.tableCell, { flex: 2 }]}>Eggs</Text>
                <Text style={[styles.tableCell, styles.alertType, { flex: 2 }]}>Price drop</Text>
                <Text style={[styles.tableCell, { flex: 2 }]}>Stock up now</Text>
                <Text style={[styles.tableCell, styles.timeText, { flex: 1 }]}>2h</Text>
              </View>
              <View style={styles.tableRow}>
                <Text style={[styles.tableCell, { flex: 2 }]}>Milk</Text>
                <Text style={[styles.tableCell, styles.alertType, { flex: 2 }]}>Volatility</Text>
                <Text style={[styles.tableCell, { flex: 2 }]}>Monitor prices</Text>
                <Text style={[styles.tableCell, styles.timeText, { flex: 1 }]}>4h</Text>
              </View>
              <View style={styles.tableRow}>
                <Text style={[styles.tableCell, { flex: 2 }]}>Bread</Text>
                <Text style={[styles.tableCell, styles.alertType, { flex: 2 }]}>Deal Pattern</Text>
                <Text style={[styles.tableCell, { flex: 2 }]}>Check Kroger</Text>
                <Text style={[styles.tableCell, styles.timeText, { flex: 1 }]}>1d</Text>
              </View>
            </View>
          </View>
        )}

        {/* Your Basket Trend Section - Mobile Only at Bottom */}
        {isMobile && (
          <View style={styles.basketTrendSection}>
            <Text style={styles.sectionTitle}>Your basket trend</Text>
            <View style={styles.basketTrendCard}>
              <View style={styles.trendChart}>
                <View style={styles.chartLine} />
                <View style={styles.todayMarker}>
                  <Text style={styles.todayText}>Today</Text>
                </View>
              </View>
              <View style={styles.trendLabels}>
                <Text style={styles.trendLabel}>6 mo ago</Text>
                <Text style={styles.trendLabel}>+6 m</Text>
              </View>
              <View style={styles.legendRow}>
                <View style={styles.legendItem}>
                  <View style={[styles.legendDot, styles.pastDot]} />
                  <Text style={styles.legendText}>Past</Text>
                </View>
                <View style={styles.legendItem}>
                  <View style={[styles.legendDot, styles.forecastDot]} />
                  <Text style={styles.legendText}>Forecast</Text>
                </View>
              </View>
            </View>
          </View>
        )}
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#FAFAF5",
  },
  content: {
    padding: isMobile ? 0 : 24,
    paddingTop: isMobile ? 16 : 24,
    maxWidth: 1400,
    alignSelf: "center",
    width: "100%",
  },
  statsScrollView: {
    marginBottom: 24,
  },
  statsScrollContent: {
    paddingHorizontal: 16,
    gap: 12,
  },
  statsContainer: {
    flexDirection: "row",
    gap: 16,
    marginBottom: 24,
  },
  statCard: {
    flex: 1,
    backgroundColor: "#FFFFFF",
    borderRadius: 12,
    padding: 20,
    minWidth: 200,
    ...Platform.select({
      ios: {
        shadowColor: "#000",
        shadowOffset: { width: 0, height: 2 },
        shadowOpacity: 0.05,
        shadowRadius: 8,
      },
      android: {
        elevation: 2,
      },
      web: {
        boxShadow: "0 2px 8px rgba(0,0,0,0.05)",
      },
    }),
  },
  statCardMobile: {
    minWidth: 160,
    width: 160,
  },
  statTitle: {
    fontSize: 13,
    color: "#666666",
    marginBottom: 8,
    fontWeight: "500",
  },
  statValue: {
    fontSize: 32,
    fontWeight: "700",
    color: "#1A1A1A",
    marginBottom: 4,
  },
  statSubtitle: {
    fontSize: 13,
    color: "#666666",
  },
  positiveText: {
    color: "#16A34A",
  },
  mainGrid: {
    flexDirection: isMobile ? "column" : "row",
    gap: 24,
    marginBottom: 24,
  },
  forecastsSection: {
    flex: isMobile ? 1 : 2,
    backgroundColor: "#FFFFFF",
    borderRadius: 12,
    padding: 24,
    ...Platform.select({
      ios: {
        shadowColor: "#000",
        shadowOffset: { width: 0, height: 2 },
        shadowOpacity: 0.05,
        shadowRadius: 8,
      },
      android: {
        elevation: 2,
      },
      web: {
        boxShadow: "0 2px 8px rgba(0,0,0,0.05)",
      },
    }),
  },
  inflationSection: {
    flex: 1,
    backgroundColor: "#FFFFFF",
    borderRadius: 12,
    padding: 24,
    ...Platform.select({
      ios: {
        shadowColor: "#000",
        shadowOffset: { width: 0, height: 2 },
        shadowOpacity: 0.05,
        shadowRadius: 8,
      },
      android: {
        elevation: 2,
      },
      web: {
        boxShadow: "0 2px 8px rgba(0,0,0,0.05)",
      },
    }),
  },
  sectionHeader: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    marginBottom: 20,
  },
  sectionTitle: {
    fontSize: isMobile ? 20 : 18,
    fontWeight: "700",
    color: "#1A1A1A",
    marginBottom: isMobile ? 16 : 0,
    paddingHorizontal: isMobile ? 16 : 0,
  },
  viewAll: {
    fontSize: 14,
    color: "#666666",
    fontWeight: "500",
  },
  priceScrollView: {
    marginBottom: 32,
  },
  priceScrollContent: {
    paddingHorizontal: 16,
    gap: 12,
  },
  priceCardMobile: {
    backgroundColor: "#FFFFFF",
    borderRadius: 16,
    padding: 16,
    width: CARD_WIDTH,
    borderWidth: 1,
    borderColor: "#E5E5E5",
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
    }),
  },
  mobileCardHeader: {
    alignItems: "center",
    marginBottom: 16,
  },
  emojiContainer: {
    width: "100%",
    backgroundColor: "#F5F5F5",
    borderRadius: 12,
    paddingVertical: 24,
    alignItems: "center",
    justifyContent: "center",
    marginBottom: 8,
  },
  mobileProductName: {
    fontSize: 16,
    fontWeight: "500",
    color: "#888888",
    textAlign: "center",
  },
  mobilePriceRow: {
    marginBottom: 4,
  },
  mobilePriceLabel: {
    fontSize: 14,
    color: "#666666",
    marginBottom: 2,
  },
  mobilePriceValue: {
    fontSize: 24,
    fontWeight: "700",
    color: "#1A1A1A",
    marginBottom: 8,
  },
  priceCard: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    paddingVertical: 16,
    borderBottomWidth: 1,
    borderBottomColor: "#F0F0F0",
  },
  priceHeader: {
    flexDirection: "row",
    alignItems: "center",
    flex: 1,
    gap: 12,
  },
  emoji: {
    fontSize: 32,
  },
  priceInfo: {
    flex: 1,
  },
  productName: {
    fontSize: 15,
    fontWeight: "600",
    color: "#1A1A1A",
    marginBottom: 4,
  },
  currentPrice: {
    fontSize: 13,
    color: "#666666",
  },
  actionButton: {
    paddingHorizontal: isMobile ? 14 : 16,
    paddingVertical: isMobile ? 6 : 8,
    borderRadius: 8,
    minWidth: isMobile ? "auto" : 120,
    alignItems: "center",
    alignSelf: isMobile ? "flex-start" : "center",
  },
  buyButton: {
    backgroundColor: "#DCFCE7",
  },
  waitButton: {
    backgroundColor: "#FEF3C7",
  },
  actionText: {
    fontSize: isMobile ? 15 : 14,
    fontWeight: "600",
  },
  buyText: {
    color: isMobile ? "#15803D" : "#166534",
  },
  waitText: {
    color: "#92400E",
  },
  basketTrendSection: {
    marginBottom: 32,
    paddingHorizontal: 16,
  },
  basketTrendCard: {
    backgroundColor: "#FFFFFF",
    borderRadius: 16,
    padding: 20,
    borderWidth: 1,
    borderColor: "#E5E5E5",
  },
  trendChart: {
    height: 180,
    position: "relative",
    marginBottom: 12,
  },
  chartLine: {
    position: "absolute",
    left: 0,
    right: 0,
    top: 60,
    height: 80,
    borderTopWidth: 3,
    borderTopColor: "#3B82F6",
    borderTopLeftRadius: 8,
    borderTopRightRadius: 8,
  },
  todayMarker: {
    position: "absolute",
    right: "30%",
    top: 0,
    bottom: 0,
    width: 1,
    backgroundColor: "#E5E5E5",
    justifyContent: "flex-start",
    alignItems: "center",
    paddingTop: 8,
  },
  todayText: {
    fontSize: 13,
    color: "#666666",
    fontWeight: "500",
  },
  trendLabels: {
    flexDirection: "row",
    justifyContent: "space-between",
    marginBottom: 16,
  },
  trendLabel: {
    fontSize: 13,
    color: "#666666",
  },
  legendRow: {
    flexDirection: "row",
    gap: 24,
  },
  legendItem: {
    flexDirection: "row",
    alignItems: "center",
    gap: 8,
  },
  legendDot: {
    width: 12,
    height: 3,
    borderRadius: 1.5,
  },
  pastDot: {
    backgroundColor: "#3B82F6",
  },
  forecastDot: {
    backgroundColor: "#10B981",
  },
  legendText: {
    fontSize: 14,
    color: "#666666",
    fontWeight: "500",
  },
  inflationSectionMobile: {
    paddingHorizontal: 16,
    marginBottom: 24,
  },
  volatilitySection: {
    marginBottom: 24,
  },
  volatilityTitle: {
    fontSize: 13,
    fontWeight: "600",
    color: "#666666",
    marginBottom: 12,
    textTransform: "uppercase",
    letterSpacing: 0.5,
  },
  inflationItem: {
    flexDirection: "row",
    alignItems: "center",
    gap: 16,
    paddingVertical: isMobile ? 10 : 12,
    paddingHorizontal: isMobile ? 12 : 0,
    marginBottom: isMobile ? 12 : 0,
    borderBottomWidth: isMobile ? 0 : 1,
    borderBottomColor: "#F0F0F0",
    backgroundColor: isMobile ? "#F5F5F5" : "transparent",
    borderRadius: isMobile ? 14 : 0,
  },
  scoreCircle: {
    width: isMobile ? 48 : 56,
    height: isMobile ? 48 : 56,
    borderRadius: isMobile ? 12 : 28,
    justifyContent: "center",
    alignItems: "center",
  },
  highVolatility: {
    backgroundColor: "#FEE2E2",
  },
  lowVolatility: {
    backgroundColor: "#DCFCE7",
  },
  scoreText: {
    fontSize: 20,
    fontWeight: "700",
  },
  highVolatilityText: {
    color: "#991B1B",
  },
  lowVolatilityText: {
    color: "#166534",
  },
  inflationInfo: {
    flex: 1,
  },
  inflationName: {
    fontSize: isMobile ? 17 : 15,
    fontWeight: "600",
    color: "#1A1A1A",
    marginBottom: isMobile ? 4 : 2,
  },
  inflationDescription: {
    fontSize: isMobile ? 15 : 13,
    color: "#666666",
  },
  alertsSection: {
    backgroundColor: "#FFFFFF",
    borderRadius: 12,
    padding: 24,
    ...Platform.select({
      ios: {
        shadowColor: "#000",
        shadowOffset: { width: 0, height: 2 },
        shadowOpacity: 0.05,
        shadowRadius: 8,
      },
      android: {
        elevation: 2,
      },
      web: {
        boxShadow: "0 2px 8px rgba(0,0,0,0.05)",
      },
    }),
  },
  alertsTable: {
    marginTop: 16,
  },
  tableHeader: {
    flexDirection: "row",
    paddingVertical: 12,
    borderBottomWidth: 2,
    borderBottomColor: "#E5E5E5",
  },
  tableHeaderText: {
    fontSize: 13,
    fontWeight: "700",
    color: "#666666",
    textTransform: "uppercase",
    letterSpacing: 0.5,
  },
  tableRow: {
    flexDirection: "row",
    paddingVertical: 16,
    borderBottomWidth: 1,
    borderBottomColor: "#F0F0F0",
  },
  tableCell: {
    fontSize: 14,
    color: "#1A1A1A",
  },
  alertType: {
    color: "#DC2626",
  },
  timeText: {
    color: "#666666",
  },
});
