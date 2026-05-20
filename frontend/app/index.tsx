import { Platform } from "react-native";
import MobileDashboard from "../mobile/MobileDashboard/MobileDashboard";
import WebDashboard from "../web/Dashboard/WebDashboard";

export default function Page() {
  const isWeb = Platform.OS === "web";
  return isWeb ? <WebDashboard /> : <MobileDashboard />;
}
