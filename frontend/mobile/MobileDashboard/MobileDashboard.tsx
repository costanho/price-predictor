import React, { useState } from "react";
import { StyleSheet, View } from "react-native";

import DashboardDefaultView from "../../components/ui/DashboardDefaultView";
import WebMobileMyList from "../../components/ui/WebMobileMyList";
import WebMobileCompare from "../../components/ui/WebMobileCompare";
import WebMobileAlerts from "../../components/ui/WebMobileAlerts";
import WebMobileProfile from "../../components/ui/WebMobileProfile";
import MobileBottomNavBar from "./MobileBottomNavBar";
import MobileTopNavBar from "./MobileTopNavBar";
import MobileSignInOrRegister from "../MobileSignInOrRegister";
import useStore from "../../store/useStore";

export default function MobileDashboard() {
  const [activeTab, setActiveTab] = useState<string>("Dashboard");
  const [showAuthModal, setShowAuthModal] = useState(false);
  const { isAuthenticated } = useStore();

  const renderContent = () => {
    switch (activeTab) {
      case "My list":
        return <WebMobileMyList />;
      case "Compare":
        return <WebMobileCompare />;
      case "Alerts":
        return <WebMobileAlerts />;
      case "Analytics":
        return <DashboardDefaultView />;
      case "Profile":
        return <WebMobileProfile />;
      case "Dashboard":
      default:
        return <DashboardDefaultView />;
    }
  };

  if (showAuthModal && !isAuthenticated) {
    return (
      <MobileSignInOrRegister
        onClose={() => setShowAuthModal(false)}
        onAuthSuccess={() => setShowAuthModal(false)}
      />
    );
  }

  return (
    <View style={styles.container}>
      <MobileTopNavBar activeTab={activeTab} onSignInPress={() => setShowAuthModal(true)} />
      {renderContent()}
      <MobileBottomNavBar activeTab={activeTab} onTabChange={setActiveTab} />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#FAFAF5",
  },
});
