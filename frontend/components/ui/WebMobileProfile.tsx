import React, { useState } from "react";
import {
  StyleSheet,
  View,
  Text,
  ScrollView,
  Dimensions,
  Platform,
  Pressable,
  Switch,
} from "react-native";
import { MaterialIcons } from "@expo/vector-icons";
import useStore from "../../store/useStore";
import client from "../../api/client";

const { width } = Dimensions.get("window");
const isMobile = width < 768;

const ALL_STORES = [
  { id: "walmart", name: "Walmart", priceTag: "Low prices" },
  { id: "kroger", name: "Kroger", priceTag: "Market rate" },
  { id: "aldi", name: "Aldi", priceTag: "Cheapest overall" },
  { id: "target", name: "Target", priceTag: "Slightly above average" },
  { id: "whole_foods", name: "Whole Foods", priceTag: "Premium pricing" },
  { id: "trader_joes", name: "Trader Joe's", priceTag: "5% below average" },
];

const WebMobileProfile: React.FC = () => {
  const [priceDropAlerts, setPriceDropAlerts] = useState(true);
  const [anomalyWarnings, setAnomalyWarnings] = useState(true);
  const [dealDNAAlerts, setDealDNAAlerts] = useState(true);
  const [loadingStore, setLoadingStore] = useState<string | null>(null);

  const { userStores, addUserStore, removeUserStore, user } = useStore();

  const handleStoreToggle = async (storeId: string, currentlySelected: boolean) => {
    setLoadingStore(storeId);
    try {
      if (currentlySelected) {
        await client.delete(`/user/stores/${storeId}`);
        removeUserStore(storeId);
      } else {
        await client.post(`/user/stores/${storeId}`);
        addUserStore(storeId);
      }
    } catch (err) {
      console.error("Store toggle failed:", err);
    } finally {
      setLoadingStore(null);
    }
  };

  return (
    <ScrollView
      style={isMobile ? styles.containerMobile : styles.containerWeb}
    >
      <View style={isMobile ? styles.contentMobile : styles.contentWeb}>
        {/* Profile Header Card */}
        <View style={styles.profileCard}>
          <View style={styles.avatarContainer}>
            <View style={styles.avatar}>
              <MaterialIcons name="person" size={48} color="#5B8FD4" />
            </View>
          </View>
          <Text style={styles.profileName}>{user?.name || "User"}</Text>
          <Text style={styles.profileEmail}>{user?.email || "email@example.com"}</Text>

          <View style={styles.divider} />

          <View style={styles.statsContainer}>
            <View style={styles.statItem}>
              <Text style={styles.statValue}>$247</Text>
              <Text style={styles.statLabel}>Total saved</Text>
            </View>
            <View style={styles.statItem}>
              <Text style={styles.statValue}>23</Text>
              <Text style={styles.statLabel}>Items tracked</Text>
            </View>
            <View style={styles.statItem}>
              <Text style={styles.statValue}>8</Text>
              <Text style={styles.statLabel}>Months active</Text>
            </View>
          </View>
        </View>

        {/* Preferences Section */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Preferences</Text>

          <Pressable style={styles.settingRow}>
            <View style={styles.settingContent}>
              <Text style={styles.settingTitle}>Zip code</Text>
              <Text style={styles.settingValue}>19103 (Philadelphia, PA)</Text>
            </View>
            <MaterialIcons name="chevron-right" size={24} color="#999" />
          </Pressable>

          <Pressable style={styles.settingRow}>
            <View style={styles.settingContent}>
              <Text style={styles.settingTitle}>Distance radius</Text>
              <Text style={styles.settingValue}>10 miles</Text>
            </View>
            <MaterialIcons name="chevron-right" size={24} color="#999" />
          </Pressable>
        </View>

        {/* Tracked Stores Section */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Tracked stores</Text>

          <View style={styles.storesGrid}>
            {ALL_STORES.map((store) => {
              const isSelected = userStores.includes(store.id);
              const isLoading = loadingStore === store.id;
              return (
                <Pressable
                  key={store.id}
                  style={[
                    styles.storeButton,
                    isSelected && styles.storeButtonActive,
                    isLoading && styles.storeButtonLoading,
                  ]}
                  onPress={() => handleStoreToggle(store.id, isSelected)}
                  disabled={isLoading}
                >
                  <Text
                    style={[
                      styles.storeButtonText,
                      isSelected && styles.storeButtonTextActive,
                    ]}
                  >
                    {store.name} {isSelected ? "✓" : ""}
                  </Text>
                  <Text
                    style={[
                      styles.storeButtonSubtext,
                      isSelected && styles.storeButtonSubtextActive,
                    ]}
                  >
                    {store.priceTag}
                  </Text>
                </Pressable>
              );
            })}
          </View>

          <View style={styles.settingRow}>
            <View style={styles.settingContent}>
              <Text style={styles.settingTitle}>
                {userStores.length} store{userStores.length !== 1 ? "s" : ""} selected
              </Text>
              <Text style={styles.settingValue}>
                Forecasts use selected stores
              </Text>
            </View>
          </View>
        </View>

        {/* Notifications Section */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Notifications</Text>

          <View style={styles.toggleRow}>
            <View style={styles.toggleContent}>
              <Text style={styles.toggleTitle}>Price drop alerts</Text>
              <Text style={styles.toggleDescription}>
                When items drop below target
              </Text>
            </View>
            <Switch
              value={priceDropAlerts}
              onValueChange={setPriceDropAlerts}
              trackColor={{ false: "#D5D5D5", true: "#52B788" }}
              thumbColor={priceDropAlerts ? "#FFFFFF" : "#F5F5F5"}
            />
          </View>

          <View style={styles.toggleRow}>
            <View style={styles.toggleContent}>
              <Text style={styles.toggleTitle}>Anomaly warnings</Text>
              <Text style={styles.toggleDescription}>
                Unusual pricing detected
              </Text>
            </View>
            <Switch
              value={anomalyWarnings}
              onValueChange={setAnomalyWarnings}
              trackColor={{ false: "#D5D5D5", true: "#52B788" }}
              thumbColor={anomalyWarnings ? "#FFFFFF" : "#F5F5F5"}
            />
          </View>

          <View style={styles.toggleRow}>
            <View style={styles.toggleContent}>
              <Text style={styles.toggleTitle}>Deal DNA alerts</Text>
              <Text style={styles.toggleDescription}>
                Before recurring sales happen
              </Text>
            </View>
            <Switch
              value={dealDNAAlerts}
              onValueChange={setDealDNAAlerts}
              trackColor={{ false: "#D5D5D5", true: "#52B788" }}
              thumbColor={dealDNAAlerts ? "#FFFFFF" : "#F5F5F5"}
            />
          </View>
        </View>

        {/* App Section */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>App</Text>

          <Pressable style={styles.settingRow}>
            <View style={styles.settingContent}>
              <Text style={styles.settingTitle}>App version</Text>
              <Text style={styles.settingValue}>1.2.3</Text>
            </View>
          </Pressable>

          <Pressable style={styles.settingRow}>
            <View style={styles.settingContent}>
              <Text style={styles.settingTitle}>Privacy policy</Text>
            </View>
            <MaterialIcons name="chevron-right" size={24} color="#999" />
          </Pressable>

          <Pressable style={styles.settingRow}>
            <View style={styles.settingContent}>
              <Text style={styles.settingTitle}>Terms of service</Text>
            </View>
            <MaterialIcons name="chevron-right" size={24} color="#999" />
          </Pressable>
        </View>

        {/* Action Buttons */}
        <View style={styles.buttonSection}>
          <Pressable style={styles.exportButton}>
            <Text style={styles.exportButtonText}>Export my data</Text>
          </Pressable>

          <Pressable
            style={styles.logoutButton}
            onPress={() => useStore.getState().logout()}
          >
            <Text style={styles.logoutButtonText}>Log out</Text>
          </Pressable>
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
    paddingBottom: 32,
  },

  // Web Styles
  containerWeb: {
    flex: 1,
    backgroundColor: "#FAFAF5",
  },
  contentWeb: {
    padding: 24,
    paddingTop: 24,
    paddingBottom: 40,
    maxWidth: 800,
    alignSelf: "center",
    width: "100%",
  },

  // Profile Card
  profileCard: {
    backgroundColor: "#3B82F6",
    borderRadius: 20,
    paddingVertical: 32,
    paddingHorizontal: 24,
    alignItems: "center",
    marginBottom: 32,
    ...Platform.select({
      ios: {
        shadowColor: "#000",
        shadowOffset: { width: 0, height: 2 },
        shadowOpacity: 0.1,
        shadowRadius: 8,
      },
      android: {
        elevation: 4,
      },
      web: {
        boxShadow: "0 2px 8px rgba(0,0,0,0.1)",
      },
    }),
  },
  avatarContainer: {
    marginBottom: 16,
  },
  avatar: {
    width: 100,
    height: 100,
    borderRadius: 50,
    borderWidth: 4,
    borderColor: "#FFFFFF",
    backgroundColor: "#5B8FD4",
    justifyContent: "center",
    alignItems: "center",
  },
  profileName: {
    fontSize: 24,
    fontWeight: "700",
    color: "#FFFFFF",
    marginBottom: 4,
  },
  profileEmail: {
    fontSize: 16,
    color: "#E0E7FF",
    marginBottom: 24,
  },
  divider: {
    width: "100%",
    height: 1,
    backgroundColor: "rgba(255, 255, 255, 0.3)",
    marginBottom: 24,
  },
  statsContainer: {
    flexDirection: "row",
    justifyContent: "space-around",
    width: "100%",
  },
  statItem: {
    alignItems: "center",
  },
  statValue: {
    fontSize: 20,
    fontWeight: "700",
    color: "#FFFFFF",
    marginBottom: 4,
  },
  statLabel: {
    fontSize: 12,
    color: "#E0E7FF",
  },

  // Sections
  section: {
    marginBottom: 24,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: "700",
    color: "#1A1A1A",
    marginBottom: 12,
  },

  // Setting Rows
  settingRow: {
    backgroundColor: "#F5F5F5",
    borderRadius: 12,
    padding: 16,
    marginBottom: 12,
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    ...Platform.select({
      ios: {
        shadowColor: "#000",
        shadowOffset: { width: 0, height: 1 },
        shadowOpacity: 0.03,
        shadowRadius: 2,
      },
      android: {
        elevation: 1,
      },
    }),
  },
  settingContent: {
    flex: 1,
  },
  settingTitle: {
    fontSize: 16,
    fontWeight: "600",
    color: "#1A1A1A",
    marginBottom: 4,
  },
  settingValue: {
    fontSize: 13,
    color: "#888888",
  },

  // Tracked Stores
  storesGrid: {
    flexDirection: "row",
    flexWrap: "wrap",
    gap: 10,
    marginBottom: 12,
  },
  storeButton: {
    backgroundColor: "#F0F0F0",
    borderRadius: 12,
    paddingHorizontal: 16,
    paddingVertical: 12,
    borderWidth: 1,
    borderColor: "#E5E5E5",
    alignItems: "center",
    minWidth: "30%",
  },
  storeButtonActive: {
    backgroundColor: "#DBEAFE",
    borderColor: "#3B82F6",
  },
  storeButtonLoading: {
    opacity: 0.6,
  },
  storeButtonText: {
    fontSize: 14,
    fontWeight: "600",
    color: "#666666",
  },
  storeButtonTextActive: {
    color: "#3B82F6",
  },
  storeButtonSubtext: {
    fontSize: 11,
    color: "#999999",
    marginTop: 4,
  },
  storeButtonSubtextActive: {
    color: "#2563EB",
  },

  // Toggle Rows
  toggleRow: {
    backgroundColor: "#F5F5F5",
    borderRadius: 12,
    padding: 16,
    marginBottom: 12,
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    ...Platform.select({
      ios: {
        shadowColor: "#000",
        shadowOffset: { width: 0, height: 1 },
        shadowOpacity: 0.03,
        shadowRadius: 2,
      },
      android: {
        elevation: 1,
      },
    }),
  },
  toggleContent: {
    flex: 1,
    marginRight: 16,
  },
  toggleTitle: {
    fontSize: 16,
    fontWeight: "600",
    color: "#1A1A1A",
    marginBottom: 4,
  },
  toggleDescription: {
    fontSize: 13,
    color: "#888888",
  },

  // Buttons
  buttonSection: {
    gap: 12,
    marginTop: 24,
  },
  exportButton: {
    backgroundColor: "#F5F5F5",
    borderRadius: 12,
    paddingVertical: 14,
    alignItems: "center",
    borderWidth: 1,
    borderColor: "#E5E5E5",
    ...Platform.select({
      ios: {
        shadowColor: "#000",
        shadowOffset: { width: 0, height: 1 },
        shadowOpacity: 0.03,
        shadowRadius: 2,
      },
      android: {
        elevation: 1,
      },
    }),
  },
  exportButtonText: {
    fontSize: 16,
    fontWeight: "600",
    color: "#1A1A1A",
  },
  logoutButton: {
    backgroundColor: "#FDD4D8",
    borderRadius: 12,
    paddingVertical: 14,
    alignItems: "center",
    borderWidth: 1,
    borderColor: "#FBCFE8",
    ...Platform.select({
      ios: {
        shadowColor: "#000",
        shadowOffset: { width: 0, height: 1 },
        shadowOpacity: 0.03,
        shadowRadius: 2,
      },
      android: {
        elevation: 1,
      },
    }),
  },
  logoutButtonText: {
    fontSize: 16,
    fontWeight: "600",
    color: "#991B1B",
  },
});

export default WebMobileProfile;
