import {
    FontAwesome,
    MaterialCommunityIcons,
    MaterialIcons,
} from "@expo/vector-icons";
import React from "react";
import { StyleSheet, Text, TouchableOpacity, View } from "react-native";
import useStore from "../../store/useStore";

const menuItems = [
  {
    label: "Dashboard",
    icon: <FontAwesome name="square" size={20} color="#2563eb" />,
  },
  {
    label: "My list",
    icon: <MaterialIcons name="menu" size={20} color="#222" />,
  },
  {
    label: "Compare stores",
    icon: (
      <MaterialCommunityIcons name="scale-balance" size={20} color="#222" />
    ),
  },
  {
    label: "Alerts",
    icon: <FontAwesome name="bell" size={20} color="#eab308" />,
  },
  {
    label: "Analytics",
    icon: <MaterialIcons name="bar-chart" size={20} color="#2563eb" />,
  },
  {
    label: "Profile",
    icon: <MaterialIcons name="person" size={20} color="#222" />,
  },
];

interface WebSideBarProps {
  activeTab: string;
  onTabChange: (tab: string) => void;
}

export default function WebSideBar({ activeTab, onTabChange }: WebSideBarProps) {
  const { unreadAlertCount } = useStore();

  return (
    <View style={styles.sidebar}>
      <Text style={styles.title}>AI Price Monitor</Text>
      {menuItems.map((item) => {
        const isActive = activeTab === item.label;
        const isAlertsItem = item.label === "Alerts";

        return (
          <TouchableOpacity
            key={item.label}
            style={[styles.menuItem, isActive && styles.activeItem]}
            onPress={() => onTabChange(item.label)}
          >
            <View style={styles.iconContainer}>
              <View style={styles.icon}>{item.icon}</View>
              {isAlertsItem && unreadAlertCount > 0 && (
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
  );
}

const styles = StyleSheet.create({
  sidebar: {
    backgroundColor: "#fff",
    width: 240,
    height: "100%",
    paddingVertical: 20,
    paddingHorizontal: 16,
    borderRightWidth: 1,
    borderColor: "#E5E5E5",
  },
  title: {
    fontSize: 20,
    fontWeight: "700",
    marginBottom: 24,
    color: "#222",
  },
  menuItem: {
    flexDirection: "row",
    alignItems: "center",
    paddingVertical: 12,
    paddingHorizontal: 8,
    borderRadius: 8,
    marginBottom: 8,
  },
  activeItem: {
    backgroundColor: "#E0E7FF",
  },
  iconContainer: {
    position: "relative",
    marginRight: 12,
  },
  icon: {
    justifyContent: "center",
    alignItems: "center",
  },
  badge: {
    position: "absolute",
    top: -8,
    right: -8,
    backgroundColor: "#E24B4A",
    borderRadius: 8,
    minWidth: 18,
    paddingHorizontal: 4,
    alignItems: "center",
    justifyContent: "center",
    height: 18,
  },
  badgeText: {
    color: "#fff",
    fontSize: 10,
    fontWeight: "600",
  },
  label: {
    fontSize: 16,
    color: "#222",
    fontWeight: "500",
  },
  activeLabel: {
    color: "#2563eb",
    fontWeight: "600",
  },
});
