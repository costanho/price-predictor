import React, { useEffect, useState } from "react";
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

// Store price factors for fallback calculation
const STORE_FACTORS: Record<string, number> = {
  walmart: 0.92,
  kroger: 1.0,
  aldi: 0.75,
  target: 1.08,
  whole_foods: 1.25,
  trader_joes: 0.95,
};

// Get price for a store — use real price if available, else estimate
const getStorePrice = (storeId: string, forecast: any): number => {
  if (forecast?.storePrices && forecast.storePrices[storeId]) {
    return forecast.storePrices[storeId];
  }
  const factor = STORE_FACTORS[storeId] || 1.0;
  return (forecast?.currentPrice || 0) * factor;
};

interface Store {
  name: string;
  price: string;
  isBest?: boolean;
}

interface WebMobileCompareProps {
  productId?: string;
}

const STORE_NAMES: Record<string, string> = {
  walmart: "Walmart",
  kroger: "Kroger",
  aldi: "Aldi",
  target: "Target",
  whole_foods: "Whole Foods",
  trader_joes: "Trader Joe's",
};

const WebMobileCompare: React.FC<WebMobileCompareProps> = ({
  productId = "sample-product",
}) => {
  const [stores, setStores] = useState<Store[]>([]);

  const product = {
    name: "Eggs, grade A",
    spec: "Large, dozen",
    icon: "🥚",
  };

  const mockForecast = {
    currentPrice: 5.40,
    storePrices: {
      walmart: 4.99,
      kroger: 5.49,
      aldi: 4.05,
      target: 5.83,
      whole_foods: 6.75,
      trader_joes: 5.13,
    },
  };

  const userStores = ["walmart", "kroger", "aldi", "target"];

  useEffect(() => {
    if (mockForecast) {
      const storeList = userStores
        .map((storeId) => {
          const price = getStorePrice(storeId, mockForecast);
          return {
            name: STORE_NAMES[storeId] || storeId,
            price: `$${price.toFixed(2)}`,
            storeId,
            rawPrice: price,
          };
        })
        .sort((a, b) => a.rawPrice - b.rawPrice)
        .map((store, index) => ({
          name: store.name,
          price: store.price,
          isBest: index === 0,
        }));
      setStores(storeList);
    }
  }, []);

  const dealDNA = [
    {
      title: "Kroger typically runs a sale on eggs every 3 weeks",
      subtitle: "Next predicted sale: May 20, 2026",
      type: "deal" as const,
    },
    {
      title: "Prices typically drop 8-12% in late spring",
      subtitle: "Seasonal pattern detected over 40 years",
      type: "seasonal" as const,
    },
  ];

  if (isMobile) {
    return (
      <ScrollView style={styles.containerMobile}>
        <View style={styles.contentMobile}>
          {/* Product Card */}
          <View style={styles.productCardMobile}>
            <Text style={styles.productIconMobile}>{product.icon}</Text>
            <View style={styles.productInfoMobile}>
              <Text style={styles.productNameMobile}>{product.name}</Text>
              <Text style={styles.productSpecMobile}>{product.spec}</Text>
            </View>
          </View>

          {/* Current Prices */}
          <Text style={styles.sectionTitleMobile}>Current prices</Text>
          {stores.length > 0 ? (
            <View style={styles.pricesContainerMobile}>
              {stores.map((store, index) => (
              <View
                key={index}
                style={[
                  styles.priceRowMobile,
                  index < stores.length - 1 && styles.priceRowBorderMobile,
                ]}
              >
                <View style={styles.storeIconContainerMobile} />
                <Text style={styles.storeNameMobile}>{store.name}</Text>
                <Text
                  style={[
                    styles.storePriceMobile,
                    store.isBest && styles.bestPriceMobile,
                  ]}
                >
                  {store.price}
                </Text>
              </View>
            ))}
            </View>
          ) : (
            <View style={styles.pricesContainerMobile}>
              <Text style={{ padding: 16, color: "#999" }}>
                No stores selected. Update your preferences.
              </Text>
            </View>
          )}

          {/* Chart Section */}
          <Text style={styles.sectionTitleMobile}>
            12-month trend + 6-month forecast
          </Text>
          <View style={styles.chartContainerMobile}>
            <View style={styles.chartMobile}>
              <View style={styles.chartArea}>
                <View style={styles.chartLine} />
                <View style={styles.todayMarkerMobile}>
                  <Text style={styles.todayTextMobile}>Today</Text>
                </View>
                <View style={styles.chartDot} />
                <Text style={styles.currentPriceMobile}>$4.82</Text>
              </View>
              <View style={styles.chartLabels}>
                <Text style={styles.chartLabelMobile}>12mo ago</Text>
              </View>
            </View>
            <View style={styles.legendMobile}>
              <View style={styles.legendItemMobile}>
                <View style={[styles.legendDot, styles.historicalDot]} />
                <Text style={styles.legendTextMobile}>Historical</Text>
              </View>
              <View style={styles.legendItemMobile}>
                <View style={[styles.legendDot, styles.forecastDot]} />
                <Text style={styles.legendTextMobile}>Forecast</Text>
              </View>
            </View>
          </View>

          {/* Deal DNA */}
          <Text style={styles.sectionTitleMobile}>Deal DNA</Text>
          {dealDNA.map((item, index) => (
            <View
              key={index}
              style={[
                styles.dealCardMobile,
                item.type === "deal"
                  ? styles.dealCardBlueMobile
                  : styles.dealCardGreenMobile,
              ]}
            >
              <Text
                style={[
                  styles.dealTitleMobile,
                  item.type === "deal"
                    ? styles.dealTitleBlueMobile
                    : styles.dealTitleGreenMobile,
                ]}
              >
                {item.title}
              </Text>
              <Text
                style={[
                  styles.dealSubtitleMobile,
                  item.type === "deal"
                    ? styles.dealSubtitleBlueMobile
                    : styles.dealSubtitleGreenMobile,
                ]}
              >
                {item.subtitle}
              </Text>
            </View>
          ))}
        </View>
      </ScrollView>
    );
  }

  // Web Layout
  return (
    <ScrollView style={styles.containerWeb}>
      <View style={styles.contentWeb}>
        {/* Product Card */}
        <View style={styles.productCardWeb}>
          <Text style={styles.productIconWeb}>{product.icon}</Text>
          <View style={styles.productInfoWeb}>
            <Text style={styles.productNameWeb}>{product.name}</Text>
            <Text style={styles.productSpecWeb}>{product.spec}</Text>
          </View>
        </View>

        <View style={styles.mainGridWeb}>
          {/* Left Column - Current Prices */}
          <View style={styles.leftColumnWeb}>
            <Text style={styles.sectionTitleWeb}>Current prices</Text>
            {stores.length > 0 ? (
              <View style={styles.pricesContainerWeb}>
                {stores.map((store, index) => (
                <View
                  key={index}
                  style={[
                    styles.priceRowWeb,
                    index < stores.length - 1 && styles.priceRowBorderWeb,
                  ]}
                >
                  <View style={styles.storeIconContainerWeb} />
                  <Text style={styles.storeNameWeb}>{store.name}</Text>
                  <Text
                    style={[
                      styles.storePriceWeb,
                      store.isBest && styles.bestPriceWeb,
                    ]}
                  >
                    {store.price}
                  </Text>
                </View>
              ))}
              </View>
            ) : (
              <View style={styles.pricesContainerWeb}>
                <Text style={{ padding: 16, color: "#999" }}>
                  No stores selected. Update your preferences.
                </Text>
              </View>
            )}
          </View>

          {/* Right Column - Chart & Deal DNA */}
          <View style={styles.rightColumnWeb}>
            {/* Chart */}
            <Text style={styles.sectionTitleWeb}>
              12-month trend + 6-month forecast
            </Text>
            <View style={styles.chartContainerWeb}>
              <View style={styles.chartWeb}>
                <View style={styles.chartAreaWeb}>
                  <View style={styles.chartLineWeb} />
                  <View style={styles.todayMarkerWeb}>
                    <Text style={styles.todayTextWeb}>Today</Text>
                  </View>
                  <View style={styles.chartDotWeb} />
                  <Text style={styles.currentPriceWeb}>$4.82</Text>
                </View>
                <View style={styles.chartLabelsWeb}>
                  <Text style={styles.chartLabelWeb}>12mo ago</Text>
                </View>
              </View>
              <View style={styles.legendWeb}>
                <View style={styles.legendItemWeb}>
                  <View style={[styles.legendDot, styles.historicalDot]} />
                  <Text style={styles.legendTextWeb}>Historical</Text>
                </View>
                <View style={styles.legendItemWeb}>
                  <View style={[styles.legendDot, styles.forecastDot]} />
                  <Text style={styles.legendTextWeb}>Forecast</Text>
                </View>
              </View>
            </View>

            {/* Deal DNA */}
            <Text style={styles.sectionTitleWeb}>Deal DNA</Text>
            {dealDNA.map((item, index) => (
              <View
                key={index}
                style={[
                  styles.dealCardWeb,
                  item.type === "deal"
                    ? styles.dealCardBlueWeb
                    : styles.dealCardGreenWeb,
                  index > 0 && styles.dealCardSpacingWeb,
                ]}
              >
                <Text
                  style={[
                    styles.dealTitleWeb,
                    item.type === "deal"
                      ? styles.dealTitleBlueWeb
                      : styles.dealTitleGreenWeb,
                  ]}
                >
                  {item.title}
                </Text>
                <Text
                  style={[
                    styles.dealSubtitleWeb,
                    item.type === "deal"
                      ? styles.dealSubtitleBlueWeb
                      : styles.dealSubtitleGreenWeb,
                  ]}
                >
                  {item.subtitle}
                </Text>
              </View>
            ))}
          </View>
        </View>
      </View>
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  // Mobile Styles
  containerMobile: {
    flex: 1,
    backgroundColor: "#FAFAF5",
  },
  contentMobile: {
    padding: 16,
    paddingTop: 16,
  },
  productCardMobile: {
    flexDirection: "row",
    alignItems: "center",
    backgroundColor: "#F5F5F0",
    borderRadius: 12,
    padding: 16,
    marginBottom: 24,
  },
  productIconMobile: {
    fontSize: 48,
    marginRight: 16,
  },
  productInfoMobile: {
    flex: 1,
  },
  productNameMobile: {
    fontSize: 18,
    fontWeight: "700",
    color: "#1A1A1A",
    marginBottom: 4,
  },
  productSpecMobile: {
    fontSize: 14,
    color: "#888888",
  },
  sectionTitleMobile: {
    fontSize: 18,
    fontWeight: "700",
    color: "#1A1A1A",
    marginBottom: 12,
  },
  pricesContainerMobile: {
    backgroundColor: "#FFFFFF",
    borderRadius: 12,
    borderWidth: 1,
    borderColor: "#E5E5E5",
    overflow: "hidden",
    marginBottom: 24,
  },
  priceRowMobile: {
    flexDirection: "row",
    alignItems: "center",
    paddingHorizontal: 16,
    paddingVertical: 14,
  },
  priceRowBorderMobile: {
    borderBottomWidth: 1,
    borderBottomColor: "#F0F0F0",
  },
  storeIconContainerMobile: {
    width: 40,
    height: 40,
    backgroundColor: "#D5D5D5",
    borderRadius: 8,
    marginRight: 12,
  },
  storeNameMobile: {
    fontSize: 16,
    fontWeight: "600",
    color: "#1A1A1A",
    flex: 1,
  },
  storePriceMobile: {
    fontSize: 16,
    fontWeight: "700",
    color: "#1A1A1A",
  },
  bestPriceMobile: {
    color: "#16A34A",
  },
  chartContainerMobile: {
    backgroundColor: "#FFFFFF",
    borderRadius: 12,
    borderWidth: 1,
    borderColor: "#E5E5E5",
    padding: 16,
    marginBottom: 24,
  },
  chartMobile: {
    height: 200,
    marginBottom: 16,
    position: "relative",
  },
  chartArea: {
    flex: 1,
    position: "relative",
    justifyContent: "flex-end",
  },
  chartLine: {
    position: "absolute",
    left: 0,
    right: 0,
    bottom: 60,
    height: 80,
    borderTopWidth: 3,
    borderTopColor: "#3B82F6",
    borderTopLeftRadius: 8,
  },
  todayMarkerMobile: {
    position: "absolute",
    right: 20,
    top: 0,
    bottom: 0,
    width: 1,
    backgroundColor: "#D5D5D5",
    justifyContent: "flex-start",
    alignItems: "center",
    paddingTop: 8,
  },
  todayTextMobile: {
    fontSize: 12,
    color: "#888888",
    fontWeight: "500",
  },
  chartDot: {
    position: "absolute",
    right: 16,
    bottom: 64,
    width: 12,
    height: 12,
    borderRadius: 6,
    backgroundColor: "#3B82F6",
  },
  currentPriceMobile: {
    position: "absolute",
    right: 40,
    bottom: 50,
    fontSize: 16,
    fontWeight: "700",
    color: "#3B82F6",
  },
  chartLabels: {
    flexDirection: "row",
    justifyContent: "space-between",
  },
  chartLabelMobile: {
    fontSize: 12,
    color: "#888888",
  },
  legendMobile: {
    flexDirection: "row",
    gap: 16,
  },
  legendItemMobile: {
    flexDirection: "row",
    alignItems: "center",
    gap: 6,
  },
  legendDot: {
    width: 10,
    height: 2,
    borderRadius: 1,
  },
  historicalDot: {
    backgroundColor: "#3B82F6",
  },
  forecastDot: {
    backgroundColor: "#10B981",
  },
  legendTextMobile: {
    fontSize: 12,
    color: "#666666",
  },
  dealCardMobile: {
    borderRadius: 12,
    padding: 16,
    marginBottom: 12,
  },
  dealCardBlueMobile: {
    backgroundColor: "#E0E7FF",
  },
  dealCardGreenMobile: {
    backgroundColor: "#E0F2E7",
  },
  dealTitleMobile: {
    fontSize: 15,
    fontWeight: "700",
    marginBottom: 4,
  },
  dealTitleBlueMobile: {
    color: "#3B82F6",
  },
  dealTitleGreenMobile: {
    color: "#16A34A",
  },
  dealSubtitleMobile: {
    fontSize: 12,
    fontWeight: "500",
  },
  dealSubtitleBlueMobile: {
    color: "#3B82F6",
  },
  dealSubtitleGreenMobile: {
    color: "#16A34A",
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
  productCardWeb: {
    flexDirection: "row",
    alignItems: "center",
    backgroundColor: "#F5F5F0",
    borderRadius: 12,
    padding: 20,
    marginBottom: 24,
  },
  productIconWeb: {
    fontSize: 56,
    marginRight: 20,
  },
  productInfoWeb: {
    flex: 1,
  },
  productNameWeb: {
    fontSize: 20,
    fontWeight: "700",
    color: "#1A1A1A",
    marginBottom: 4,
  },
  productSpecWeb: {
    fontSize: 15,
    color: "#888888",
  },
  mainGridWeb: {
    flexDirection: "row",
    gap: 24,
  },
  leftColumnWeb: {
    flex: 1,
  },
  rightColumnWeb: {
    flex: 1,
  },
  sectionTitleWeb: {
    fontSize: 18,
    fontWeight: "700",
    color: "#1A1A1A",
    marginBottom: 16,
  },
  pricesContainerWeb: {
    backgroundColor: "#FFFFFF",
    borderRadius: 12,
    borderWidth: 1,
    borderColor: "#E5E5E5",
    overflow: "hidden",
    marginBottom: 24,
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
  priceRowWeb: {
    flexDirection: "row",
    alignItems: "center",
    paddingHorizontal: 16,
    paddingVertical: 14,
  },
  priceRowBorderWeb: {
    borderBottomWidth: 1,
    borderBottomColor: "#F0F0F0",
  },
  storeIconContainerWeb: {
    width: 48,
    height: 48,
    backgroundColor: "#D5D5D5",
    borderRadius: 8,
    marginRight: 16,
  },
  storeNameWeb: {
    fontSize: 16,
    fontWeight: "600",
    color: "#1A1A1A",
    flex: 1,
  },
  storePriceWeb: {
    fontSize: 16,
    fontWeight: "700",
    color: "#1A1A1A",
  },
  bestPriceWeb: {
    color: "#16A34A",
  },
  chartContainerWeb: {
    backgroundColor: "#FFFFFF",
    borderRadius: 12,
    borderWidth: 1,
    borderColor: "#E5E5E5",
    padding: 20,
    marginBottom: 24,
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
  chartWeb: {
    height: 260,
    marginBottom: 20,
    position: "relative",
  },
  chartAreaWeb: {
    flex: 1,
    position: "relative",
    justifyContent: "flex-end",
  },
  chartLineWeb: {
    position: "absolute",
    left: 0,
    right: 0,
    bottom: 80,
    height: 100,
    borderTopWidth: 3,
    borderTopColor: "#3B82F6",
    borderTopLeftRadius: 8,
  },
  todayMarkerWeb: {
    position: "absolute",
    right: 40,
    top: 0,
    bottom: 0,
    width: 1,
    backgroundColor: "#D5D5D5",
    justifyContent: "flex-start",
    alignItems: "center",
    paddingTop: 8,
  },
  todayTextWeb: {
    fontSize: 12,
    color: "#888888",
    fontWeight: "500",
  },
  chartDotWeb: {
    position: "absolute",
    right: 36,
    bottom: 84,
    width: 12,
    height: 12,
    borderRadius: 6,
    backgroundColor: "#3B82F6",
  },
  currentPriceWeb: {
    position: "absolute",
    right: 60,
    bottom: 70,
    fontSize: 16,
    fontWeight: "700",
    color: "#3B82F6",
  },
  chartLabelsWeb: {
    flexDirection: "row",
    justifyContent: "space-between",
  },
  chartLabelWeb: {
    fontSize: 12,
    color: "#888888",
  },
  legendWeb: {
    flexDirection: "row",
    gap: 20,
  },
  legendItemWeb: {
    flexDirection: "row",
    alignItems: "center",
    gap: 8,
  },
  legendTextWeb: {
    fontSize: 13,
    color: "#666666",
  },
  dealCardWeb: {
    borderRadius: 12,
    padding: 16,
    marginBottom: 12,
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
  dealCardBlueWeb: {
    backgroundColor: "#E0E7FF",
  },
  dealCardGreenWeb: {
    backgroundColor: "#E0F2E7",
  },
  dealCardSpacingWeb: {
    marginTop: 12,
  },
  dealTitleWeb: {
    fontSize: 15,
    fontWeight: "700",
    marginBottom: 4,
  },
  dealTitleBlueWeb: {
    color: "#3B82F6",
  },
  dealTitleGreenWeb: {
    color: "#16A34A",
  },
  dealSubtitleWeb: {
    fontSize: 13,
    fontWeight: "500",
  },
  dealSubtitleBlueWeb: {
    color: "#3B82F6",
  },
  dealSubtitleGreenWeb: {
    color: "#16A34A",
  },
});

export default WebMobileCompare;
