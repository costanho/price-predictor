import React, { useState } from "react";
import { StyleSheet, Text, TextInput, View, TouchableOpacity } from "react-native";
import DashboardDefaultView from "../../components/ui/DashboardDefaultView";
import WebMobileMyList from "../../components/ui/WebMobileMyList";
import WebMobileCompare from "../../components/ui/WebMobileCompare";
import WebMobileAlerts from "../../components/ui/WebMobileAlerts";
import WebMobileProfile from "../../components/ui/WebMobileProfile";
import WebSideBar from "./WebSideBar";
import WebSignInOrRegister from "../WebSignInOrRegister";
import useStore from "../../store/useStore";

export default function Dashboard() {
  const [activeTab, setActiveTab] = useState<string>("Dashboard");
  const [showAuthModal, setShowAuthModal] = useState(false);
  const { isAuthenticated, user } = useStore();

  const renderContent = () => {
    switch (activeTab) {
      case "My list":
        return <WebMobileMyList />;
      case "Compare stores":
        return <WebMobileCompare />;
      case "Alerts":
        return <WebMobileAlerts />;
      case "Profile":
        return <WebMobileProfile />;
      case "Dashboard":
      default:
        return <DashboardDefaultView />;
    }
  };

  if (showAuthModal && !isAuthenticated) {
    return (
      <WebSignInOrRegister
        onClose={() => setShowAuthModal(false)}
        onAuthSuccess={() => setShowAuthModal(false)}
      />
    );
  }

  return (
    <View style={styles.container}>
      <View style={styles.sidebarContainer}>
        <WebSideBar activeTab={activeTab} onTabChange={setActiveTab} />
      </View>
      <View style={styles.mainArea}>
        <View style={styles.topBar}>
          <View style={styles.topBarLeft}>
            <Text style={styles.welcomeText}>
              {isAuthenticated ? `Welcome back, ${user?.name}` : "Welcome"}
            </Text>
            <TextInput
              style={styles.searchInput}
              placeholder="Search products..."
              placeholderTextColor="#999"
            />
          </View>
          {!isAuthenticated && (
            <View style={styles.topBarRight}>
              <Text
                style={styles.signInLink}
                onPress={() => setShowAuthModal(true)}
              >
                Sign in
              </Text>
            </View>
          )}
        </View>
        <View style={styles.contentContainer}>{renderContent()}</View>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flexDirection: "row",
    flex: 1,
    backgroundColor: "#FAFAF5",
  },
  mainArea: {
    flex: 1,
    flexDirection: "column",
    backgroundColor: "#FAFAF5",
    // minHeight: "100vh", // Not supported in React Native
  },
  topBar: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    paddingVertical: 24,
    paddingHorizontal: 24,
    backgroundColor: "#fff",
    borderBottomWidth: 1,
    borderColor: "#E5E5E5",
    zIndex: 2,
  },
  topBarLeft: {
    flex: 1,
    flexDirection: "row",
    alignItems: "center",
    gap: 24,
  },
  topBarRight: {
    flexDirection: "row",
    alignItems: "center",
    gap: 16,
  },
  welcomeText: {
    fontSize: 28,
    fontWeight: "700",
    color: "#111",
    letterSpacing: -0.5,
    minWidth: 250,
  },
  searchInput: {
    width: 280,
    height: 40,
    paddingHorizontal: 16,
    borderWidth: 1,
    borderColor: "#E5E5E5",
    borderRadius: 10,
    backgroundColor: "#FAFAF5",
    fontSize: 16,
    color: "#222",
    shadowColor: "#000",
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.03,
    shadowRadius: 2,
  },
  signInLink: {
    fontSize: 14,
    fontWeight: "600",
    color: "#2563eb",
    paddingHorizontal: 12,
    paddingVertical: 8,
    cursor: "pointer",
  },
  sidebarContainer: {
    width: 240,
    backgroundColor: "#fff",
    borderRightWidth: 1,
    borderColor: "#E5E5E5",
  },
  contentContainer: {
    flex: 1,
    padding: 16,
  },
});
