import React, { useState } from "react";
import {
  StyleSheet,
  View,
  Text,
  ScrollView,
  Dimensions,
  Platform,
  Pressable,
  FlatList,
} from "react-native";

const { width } = Dimensions.get("window");
const isMobile = width < 768;

interface ListItem {
  id: string;
  name: string;
  quantity: string;
  bestStore: string;
  bestPrice: string;
  currentPrice: string;
  checked: boolean;
}

interface StoreOption {
  store: string;
  price: string;
}

const WebMobileMyList: React.FC = () => {
  const [checked, setChecked] = useState<Set<string>>(new Set());

  const mockCartItems = [
    { id: "1", name: "Eggs (12ct)", quantity: "2", price: "4.99", store: "Walmart" },
    { id: "2", name: "Milk (1L)", quantity: "1", price: "3.91", store: "Kroger" },
    { id: "3", name: "Whole wheat bread", quantity: "2", price: "2.98", store: "ALDI" },
    { id: "4", name: "Chicken breast (1lb)", quantity: "3", price: "7.99", store: "Walmart" },
    { id: "5", name: "Bananas (1lb)", quantity: "2", price: "0.52", store: "Kroger" },
  ];

  const mockCartOptimization = {
    totalItems: 5,
    totalPrice: "$24.39",
    savings: "$6.47",
    cheapest_single_total: "$28.39",
    store_breakdown: { "Walmart": "$12.98", "Kroger": "$4.43", "ALDI": "$2.98" },
    optimized_total: "$20.39",
    weekly_savings: "$6.47",
    annual_projection: "$336.44",
    wait_one_month_savings: "$12.50",
    stores: [
      { store: "Walmart", items: 2, price: "$12.98" },
      { store: "Kroger", items: 2, price: "$4.43" },
      { store: "ALDI", items: 1, price: "$2.98" },
    ],
  };

  const toggleItem = (id: string) => {
    const newChecked = new Set(checked);
    if (newChecked.has(id)) {
      newChecked.delete(id);
    } else {
      newChecked.add(id);
    }
    setChecked(newChecked);
  };

  if (isMobile) {
    return (
      <ScrollView style={styles.containerMobile}>
        <View style={styles.contentMobile}>
          {/* Items List */}
          <View style={styles.itemsListMobile}>
            {mockCartItems.map((item: any) => (
              <Pressable
                key={item.id}
                onPress={() => toggleItem(item.id)}
                style={styles.itemCardMobile}
              >
                <View style={styles.itemCheckboxArea}>
                  <View
                    style={[
                      styles.checkbox,
                      checked.has(item.id) && styles.checkboxChecked,
                    ]}
                  >
                    {checked.has(item.id) && <Text style={styles.checkmark}>✓</Text>}
                  </View>
                </View>
                <View style={styles.itemContentMobile}>
                  <Text style={styles.itemNameMobile}>{item.name}</Text>
                  <Text style={styles.itemSubtitleMobile}>
                    Qty: {item.quantity}
                  </Text>
                </View>
                <Text style={styles.itemPriceMobile}>${item.price}</Text>
              </Pressable>
            ))}
          </View>

          {/* Total Section */}
          <View style={styles.totalSectionMobile}>
            <Text style={styles.totalLabelMobile}>Total at cheapest stores</Text>
            <Text style={styles.totalValueMobile}>${mockCartOptimization?.cheapest_single_total || '0'}</Text>
          </View>

          {/* View Store Breakdown Button */}
          <Pressable style={styles.viewBreakdownButtonMobile}>
            <Text style={styles.viewBreakdownTextMobile}>
              View store breakdown →
            </Text>
          </Pressable>

          {/* Split-Cart Optimizer */}
          {mockCartOptimization && (
            <View style={styles.optimizerSectionMobile}>
              <Text style={styles.optimizerTitleMobile}>Split-cart optimizer</Text>

              {Object.entries(mockCartOptimization.store_breakdown || {}).map(([store, price]: any) => (
                <View key={store} style={styles.storeOptionMobile}>
                  <Text style={styles.storeNameMobile}>{store}</Text>
                  <Text style={styles.storePriceMobile}>${price}</Text>
                </View>
              ))}

              <View style={styles.optimizedTotalMobile}>
                <Text style={styles.optimizedLabelMobile}>Optimized total</Text>
                <Text style={styles.optimizedValueMobile}>${mockCartOptimization.optimized_total}</Text>
              </View>

              <View style={styles.savingsMobile}>
                <Text style={styles.savingsTextMobile}>
                  Save ${mockCartOptimization.weekly_savings} vs. single store • ${mockCartOptimization.annual_projection}/year
                </Text>
              </View>

              {/* Wait Banner */}
              <View style={styles.waitBannerMobile}>
                <Text style={styles.waitTitleMobile}>
                  Wait 1 month and save an additional
                </Text>
                <Text style={styles.waitAmountMobile}>${mockCartOptimization.wait_one_month_savings}</Text>
              </View>
            </View>
          )}
        </View>
      </ScrollView>
    );
  }

  // Web Layout
  return (
    <ScrollView style={styles.containerWeb}>
      <View style={styles.contentWeb}>
        <View style={styles.mainGridWeb}>
          {/* Left Column - Items List */}
          <View style={styles.leftColumnWeb}>
            <View style={styles.itemsListWeb}>
              {mockCartItems.map((item: any) => (
                <Pressable
                  key={item.id}
                  onPress={() => toggleItem(item.id)}
                  style={styles.itemCardWeb}
                >
                  <View style={styles.itemCheckboxArea}>
                    <View
                      style={[
                        styles.checkbox,
                        checked.has(item.id) && styles.checkboxChecked,
                      ]}
                    >
                      {checked.has(item.id) && <Text style={styles.checkmark}>✓</Text>}
                    </View>
                  </View>
                  <View style={styles.itemContentWeb}>
                    <Text style={styles.itemNameWeb}>{item.name}</Text>
                    <Text style={styles.itemSubtitleWeb}>
                      Qty: {item.quantity}
                    </Text>
                  </View>
                  <Text style={styles.itemPriceWeb}>${item.price}</Text>
                </Pressable>
              ))}
            </View>

            {/* Total Section */}
            <View style={styles.totalSectionWeb}>
              <Text style={styles.totalLabelWeb}>Total at cheapest stores</Text>
              <Text style={styles.totalValueWeb}>${mockCartOptimization?.cheapest_single_total || '0'}</Text>
            </View>

            {/* View Store Breakdown Button */}
            <Pressable style={styles.viewBreakdownButtonWeb}>
              <Text style={styles.viewBreakdownTextWeb}>
                View store breakdown →
              </Text>
            </Pressable>
          </View>

          {/* Right Column - Optimizer */}
          <View style={styles.rightColumnWeb}>
            {mockCartOptimization && (
              <View style={styles.optimizerSectionWeb}>
                <Text style={styles.optimizerTitleWeb}>Split-cart optimizer</Text>

                {Object.entries(mockCartOptimization.store_breakdown || {}).map(([store, price]: any) => (
                  <View key={store} style={styles.storeOptionWeb}>
                    <Text style={styles.storeNameWeb}>{store}</Text>
                    <Text style={styles.storePriceWeb}>${price}</Text>
                  </View>
                ))}

                <View style={styles.dividerWeb} />

                <View style={styles.optimizedTotalWeb}>
                  <Text style={styles.optimizedLabelWeb}>Optimized total</Text>
                  <Text style={styles.optimizedValueWeb}>${mockCartOptimization.optimized_total}</Text>
                </View>

                <View style={styles.savingsWeb}>
                  <Text style={styles.savingsTextWeb}>
                    Save ${mockCartOptimization.weekly_savings} vs. single store • ${mockCartOptimization.annual_projection}/year
                  </Text>
                </View>

                {/* Wait Banner */}
                <View style={styles.waitBannerWeb}>
                  <Text style={styles.waitTitleWeb}>
                    Wait 1 month and save an additional
                  </Text>
                  <Text style={styles.waitAmountWeb}>${mockCartOptimization.wait_one_month_savings}</Text>
                </View>
              </View>
            )}
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
  itemsListMobile: {
    marginBottom: 16,
  },
  itemCardMobile: {
    flexDirection: "row",
    alignItems: "center",
    backgroundColor: "#F5F5F0",
    borderRadius: 12,
    paddingHorizontal: 12,
    paddingVertical: 12,
    marginBottom: 12,
  },
  itemCheckboxArea: {
    paddingRight: 12,
  },
  checkbox: {
    width: 24,
    height: 24,
    borderRadius: 4,
    borderWidth: 2,
    borderColor: "#CCCCCC",
    justifyContent: "center",
    alignItems: "center",
    backgroundColor: "#FFFFFF",
  },
  checkboxChecked: {
    backgroundColor: "#3B82F6",
    borderColor: "#3B82F6",
  },
  checkmark: {
    color: "#FFFFFF",
    fontSize: 14,
    fontWeight: "700",
  },
  itemContentMobile: {
    flex: 1,
  },
  itemNameMobile: {
    fontSize: 16,
    fontWeight: "600",
    color: "#1A1A1A",
    marginBottom: 4,
  },
  itemSubtitleMobile: {
    fontSize: 13,
    color: "#888888",
  },
  itemPriceMobile: {
    fontSize: 16,
    fontWeight: "700",
    color: "#1A1A1A",
    textAlign: "right",
    marginLeft: 12,
  },
  totalSectionMobile: {
    marginBottom: 16,
    paddingHorizontal: 12,
  },
  totalLabelMobile: {
    fontSize: 16,
    fontWeight: "600",
    color: "#1A1A1A",
    marginBottom: 8,
  },
  totalValueMobile: {
    fontSize: 24,
    fontWeight: "700",
    color: "#16A34A",
  },
  viewBreakdownButtonMobile: {
    backgroundColor: "#FFFFFF",
    borderRadius: 12,
    paddingVertical: 16,
    marginBottom: 20,
    alignItems: "center",
    borderWidth: 1,
    borderColor: "#E5E5E5",
  },
  viewBreakdownTextMobile: {
    fontSize: 16,
    fontWeight: "600",
    color: "#1A1A1A",
  },
  optimizerSectionMobile: {
    backgroundColor: "#E0E7FF",
    borderRadius: 16,
    padding: 16,
    marginBottom: 16,
  },
  optimizerTitleMobile: {
    fontSize: 16,
    fontWeight: "700",
    color: "#3B82F6",
    marginBottom: 12,
  },
  storeOptionMobile: {
    backgroundColor: "#FFFFFF",
    borderRadius: 12,
    paddingHorizontal: 16,
    paddingVertical: 12,
    marginBottom: 12,
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
  },
  storeNameMobile: {
    fontSize: 15,
    fontWeight: "600",
    color: "#1A1A1A",
  },
  storePriceMobile: {
    fontSize: 15,
    fontWeight: "700",
    color: "#1A1A1A",
  },
  optimizedTotalMobile: {
    backgroundColor: "#FFFFFF",
    borderRadius: 12,
    paddingHorizontal: 16,
    paddingVertical: 12,
    marginBottom: 12,
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
  },
  optimizedLabelMobile: {
    fontSize: 15,
    fontWeight: "600",
    color: "#3B82F6",
  },
  optimizedValueMobile: {
    fontSize: 18,
    fontWeight: "700",
    color: "#3B82F6",
  },
  savingsMobile: {
    backgroundColor: "#FFFFFF",
    borderRadius: 12,
    paddingHorizontal: 16,
    paddingVertical: 12,
    marginBottom: 16,
  },
  savingsTextMobile: {
    fontSize: 12,
    color: "#3B82F6",
    fontWeight: "500",
    textAlign: "center",
  },
  waitBannerMobile: {
    backgroundColor: "#E0F2E7",
    borderRadius: 16,
    paddingVertical: 24,
    paddingHorizontal: 16,
    alignItems: "center",
  },
  waitTitleMobile: {
    fontSize: 14,
    color: "#1A1A1A",
    marginBottom: 8,
  },
  waitAmountMobile: {
    fontSize: 28,
    fontWeight: "700",
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
  mainGridWeb: {
    flexDirection: "row",
    gap: 24,
  },
  leftColumnWeb: {
    flex: 2,
  },
  rightColumnWeb: {
    flex: 1,
  },
  itemsListWeb: {
    backgroundColor: "#FFFFFF",
    borderRadius: 12,
    overflow: "hidden",
    marginBottom: 16,
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
  itemCardWeb: {
    flexDirection: "row",
    alignItems: "center",
    paddingHorizontal: 20,
    paddingVertical: 16,
    borderBottomWidth: 1,
    borderBottomColor: "#F0F0F0",
  },
  itemContentWeb: {
    flex: 1,
    marginHorizontal: 16,
  },
  itemNameWeb: {
    fontSize: 16,
    fontWeight: "600",
    color: "#1A1A1A",
    marginBottom: 4,
  },
  itemSubtitleWeb: {
    fontSize: 13,
    color: "#888888",
  },
  itemPriceWeb: {
    fontSize: 16,
    fontWeight: "700",
    color: "#1A1A1A",
    textAlign: "right",
  },
  totalSectionWeb: {
    backgroundColor: "#FFFFFF",
    borderRadius: 12,
    padding: 20,
    marginBottom: 16,
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
  totalLabelWeb: {
    fontSize: 14,
    color: "#1A1A1A",
    marginBottom: 8,
  },
  totalValueWeb: {
    fontSize: 24,
    fontWeight: "700",
    color: "#16A34A",
  },
  viewBreakdownButtonWeb: {
    backgroundColor: "#FFFFFF",
    borderRadius: 12,
    paddingVertical: 14,
    alignItems: "center",
    borderWidth: 1,
    borderColor: "#D5D5D5",
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
  viewBreakdownTextWeb: {
    fontSize: 15,
    fontWeight: "600",
    color: "#1A1A1A",
  },
  optimizerSectionWeb: {
    backgroundColor: "#E0E7FF",
    borderRadius: 16,
    padding: 20,
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
  optimizerTitleWeb: {
    fontSize: 16,
    fontWeight: "700",
    color: "#3B82F6",
    marginBottom: 16,
  },
  storeOptionWeb: {
    backgroundColor: "#FFFFFF",
    borderRadius: 12,
    paddingHorizontal: 16,
    paddingVertical: 12,
    marginBottom: 12,
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
  },
  storeNameWeb: {
    fontSize: 15,
    fontWeight: "600",
    color: "#1A1A1A",
  },
  storePriceWeb: {
    fontSize: 15,
    fontWeight: "700",
    color: "#1A1A1A",
  },
  dividerWeb: {
    height: 1,
    backgroundColor: "#C7D2FE",
    marginVertical: 12,
  },
  optimizedTotalWeb: {
    backgroundColor: "#FFFFFF",
    borderRadius: 12,
    paddingHorizontal: 16,
    paddingVertical: 12,
    marginBottom: 12,
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
  },
  optimizedLabelWeb: {
    fontSize: 14,
    fontWeight: "600",
    color: "#3B82F6",
  },
  optimizedValueWeb: {
    fontSize: 18,
    fontWeight: "700",
    color: "#3B82F6",
  },
  savingsWeb: {
    backgroundColor: "#FFFFFF",
    borderRadius: 12,
    paddingHorizontal: 16,
    paddingVertical: 12,
    marginBottom: 16,
  },
  savingsTextWeb: {
    fontSize: 12,
    color: "#3B82F6",
    fontWeight: "500",
    textAlign: "center",
  },
  waitBannerWeb: {
    backgroundColor: "#E0F2E7",
    borderRadius: 16,
    paddingVertical: 20,
    paddingHorizontal: 16,
    alignItems: "center",
  },
  waitTitleWeb: {
    fontSize: 13,
    color: "#1A1A1A",
    marginBottom: 8,
  },
  waitAmountWeb: {
    fontSize: 24,
    fontWeight: "700",
    color: "#16A34A",
  },
});

export default WebMobileMyList;
