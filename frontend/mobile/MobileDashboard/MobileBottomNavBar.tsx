import {
    FontAwesome,
    MaterialCommunityIcons,
    MaterialIcons,
} from "@expo/vector-icons";
import React from "react";
import { StyleSheet, Text, TouchableOpacity, View } from "react-native";
import useStore from "../../store/useStore";

const navItems = [
  {
    label: "Dashboard",
    icon: <FontAwesome name="square" size={20} color="#2563eb" />,
  },
  {
    label: "My list",
    icon: <MaterialIcons name="menu" size={20} color="#222" />,
  },
  {
    label: "Compare",
    icon: <MaterialCommunityIcons name="compare" size={20} color="#222" />,
  },
  {
    label: "Alerts",
    icon: <FontAwesome name="bell" size={20} color="#eab308" />,
  },
  {
    label: "Analytics",
    icon: <MaterialIcons name="bar-chart" size={20} color="#222" />,
  },
  {
    label: "Profile",
    icon: <FontAwesome name="user" size={20} color="#222" />,
  },
];

interface MobileBottomNavBarProps {
  activeTab: string;
  onTabChange: (tab: string) => void;
}

export default function MobileBottomNavBar({
  activeTab,
  onTabChange,
}: MobileBottomNavBarProps) {
  const { unreadAlertCount } = useStore();

  return (
    <View style={styles.wrapper}>
      <View style={styles.container}>
        {navItems.map((item) => {
          const isActive = activeTab === item.label;
          const isAlertsTab = item.label === "Alerts";

          return (
            <TouchableOpacity
              key={item.label}
              style={styles.navItem}
              activeOpacity={0.7}
              onPress={() => onTabChange(item.label)}
            >
              <View
                style={[
                  styles.iconWrapper,
                  isActive && styles.activeIconWrapper,
                ]}
              >
                {item.icon}
                {isAlertsTab && unreadAlertCount > 0 && (
                  <View style={styles.badge}>
                    <Text style={styles.badgeText}>
                      {unreadAlertCount > 99 ? '99+' : unreadAlertCount}
                    </Text>
                  </View>
                )}
              </View>
              <Text style={[styles.label, isActive && styles.activeLabel]}>
                {item.label}
              </Text>
            </TouchableOpacity>
          );
        })}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  wrapper: {
    backgroundColor: "#FAFAF5",
    borderTopLeftRadius: 24,
    borderTopRightRadius: 24,
    shadowColor: "#000",
    shadowOffset: { width: 0, height: -2 },
    shadowOpacity: 0.05,
    shadowRadius: 8,
    elevation: 4,
  },
  container: {
    flexDirection: "row",
    justifyContent: "space-around",
    alignItems: "center",
    paddingVertical: 10,
    paddingHorizontal: 8,
  },
  navItem: {
    flex: 1,
    alignItems: "center",
    justifyContent: "center",
  },
  iconWrapper: {
    padding: 8,
    borderRadius: 12,
    marginBottom: 2,
  },
  activeIconWrapper: {
    backgroundColor: "#e0e7ff", // light blue
  },
  label: {
    fontSize: 14,
    color: "#222",
    fontWeight: "400",
  },
  activeLabel: {
    color: "#2563eb",
    fontWeight: "600",
  },
  badge: {
    position: "absolute",
    top: -4,
    right: -4,
    backgroundColor: "#E24B4A",
    borderRadius: 8,
    minWidth: 16,
    paddingHorizontal: 4,
    alignItems: "center",
    justifyContent: "center",
  },
  badgeText: {
    color: "#fff",
    fontSize: 10,
    fontWeight: "600",
  },
});
