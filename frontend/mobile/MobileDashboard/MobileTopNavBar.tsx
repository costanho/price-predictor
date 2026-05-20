import React, { useState } from "react";
import {
    StyleSheet,
    Text,
    View,
    TouchableOpacity,
} from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import useStore from "../../store/useStore";

interface MobileTopNavBarProps {
  activeTab?: string;
  onSignInPress?: () => void;
}

export default function MobileTopNavBar({ activeTab = "Dashboard", onSignInPress }: MobileTopNavBarProps) {
  const insets = useSafeAreaInsets();
  const { isAuthenticated, user } = useStore();
  const isDefaultView = activeTab === "Dashboard";

  return (
    <View style={[styles.safeArea, { paddingTop: insets.top + 16 }]}>
      <View style={styles.divider} />
      <View style={styles.headerContainer}>
        <View style={styles.greetingContainer}>
          {isDefaultView ? (
            <>
              <Text style={styles.greeting}>
                Hi {isAuthenticated ? user?.name?.split(' ')[0] : 'there'} <Text style={styles.wave}>👋</Text>
              </Text>
              {isAuthenticated && (
                <Text style={styles.savings}>You've saved $47 this month</Text>
              )}
            </>
          ) : (
            <Text style={styles.greeting}>{activeTab}</Text>
          )}
        </View>
        {!isAuthenticated && (
          <TouchableOpacity onPress={onSignInPress}>
            <Text style={styles.signInLink}>Sign in</Text>
          </TouchableOpacity>
        )}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  safeArea: {
    backgroundColor: "#FAFAF5",
    width: "100%",
  },
  divider: {
    height: 1,
    backgroundColor: "#E5E5E5",
    marginBottom: 18,
    marginHorizontal: 0,
  },
  headerContainer: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "flex-start",
    paddingHorizontal: 16,
    marginBottom: 12,
  },
  greetingContainer: {
    flex: 1,
  },
  greeting: {
    fontSize: 28,
    fontWeight: "700",
    color: "#222",
    flexWrap: "wrap",
  },
  wave: {
    fontSize: 28,
  },
  savings: {
    fontSize: 16,
    color: "#888888",
    marginTop: 6,
    fontWeight: "400",
    flexWrap: "wrap",
  },
  signInLink: {
    fontSize: 13,
    fontWeight: "600",
    color: "#2563eb",
    paddingHorizontal: 10,
    paddingVertical: 6,
  },
});
