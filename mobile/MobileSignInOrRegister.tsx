import React, { useState } from "react";
import { StyleSheet, View, Text, TextInput, TouchableOpacity, ActivityIndicator, ScrollView } from "react-native";
import { MaterialIcons } from "@expo/vector-icons";
import useStore from "../store/useStore";

interface MobileSignInOrRegisterProps {
  onClose?: () => void;
  onAuthSuccess?: () => void;
}

type AuthMode = "signin" | "register";

export default function MobileSignInOrRegister({ onClose, onAuthSuccess }: MobileSignInOrRegisterProps) {
  const [mode, setMode] = useState<AuthMode>("signin");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [fullName, setFullName] = useState("");
  const [zipCode, setZipCode] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const { login } = useStore();

  const handleSignIn = async () => {
    if (!email || !password) {
      setError("Email and password required");
      return;
    }
    setLoading(true);
    setError(null);
    try {
      await login(email, password);
      // Token is automatically stored by login() in the store
      // Subsequent API calls will use the token via the request interceptor
      if (onAuthSuccess) {
        onAuthSuccess();
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : "Sign in failed");
    } finally {
      setLoading(false);
    }
  };

  const handleRegister = async () => {
    if (!fullName || !email || !password || !confirmPassword || !zipCode) {
      setError("All fields required");
      return;
    }
    if (password !== confirmPassword) {
      setError("Passwords don't match");
      return;
    }
    if (password.length < 6) {
      setError("Password must be at least 6 characters");
      return;
    }

    setLoading(true);
    setError(null);
    try {
      // TODO: Replace with actual register API call
      // await register(fullName, email, password, zipCode);
      await login(email, password);
      // Token is automatically stored by login() in the store
      // Subsequent API calls will use the token via the request interceptor
      if (onAuthSuccess) {
        onAuthSuccess();
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : "Registration failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.contentContainer}>
      {/* Close Button */}
      {onClose && (
        <TouchableOpacity onPress={onClose} style={styles.closeButton}>
          <Text style={styles.closeButtonText}>← Return to dashboard</Text>
        </TouchableOpacity>
      )}

      {/* Logo Section */}
      <View style={styles.logoSection}>
        <View style={styles.logoBadge}>
          <MaterialIcons name="playlist-add-check" size={32} color="#10b981" />
        </View>
        <Text style={styles.appName}>ShopPredict</Text>
        <Text style={styles.appTagline}>AI grocery price intelligence</Text>
      </View>

      {/* Card Container */}
      <View style={styles.card}>
        {/* Tab Buttons */}
        <View style={styles.tabContainer}>
          <TouchableOpacity
            onPress={() => {
              setMode("signin");
              setError(null);
            }}
            style={[styles.tabButton, mode === "signin" && styles.tabButtonActive]}
          >
            <Text style={[styles.tabText, mode === "signin" && styles.tabTextActive]}>
              Sign in
            </Text>
          </TouchableOpacity>
          <TouchableOpacity
            onPress={() => {
              setMode("register");
              setError(null);
            }}
            style={[styles.tabButton, mode === "register" && styles.tabButtonActive]}
          >
            <Text style={[styles.tabText, mode === "register" && styles.tabTextActive]}>
              Register
            </Text>
          </TouchableOpacity>
        </View>

        {/* Error Message */}
        {error && (
          <View style={styles.errorContainer}>
            <MaterialIcons name="error-outline" size={16} color="#EF4444" />
            <Text style={styles.errorText}>{error}</Text>
          </View>
        )}

        {/* Sign In Form */}
        {mode === "signin" && (
          <View style={styles.formContainer}>
            <View style={styles.inputGroup}>
              <Text style={styles.label}>EMAIL</Text>
              <TextInput
                style={styles.input}
                placeholder="you@example.com"
                placeholderTextColor="#555"
                value={email}
                onChangeText={setEmail}
                editable={!loading}
              />
            </View>

            <View style={styles.inputGroup}>
              <Text style={styles.label}>PASSWORD</Text>
              <TextInput
                style={styles.input}
                placeholder="••••••••"
                placeholderTextColor="#555"
                value={password}
                onChangeText={setPassword}
                secureTextEntry
                editable={!loading}
              />
            </View>

            <TouchableOpacity
              onPress={handleSignIn}
              disabled={loading}
              style={styles.submitButton}
            >
              {loading ? (
                <ActivityIndicator color="#1a1a2e" />
              ) : (
                <Text style={styles.submitButtonText}>Sign in</Text>
              )}
            </TouchableOpacity>

            <View style={styles.footerLink}>
              <Text style={styles.footerText}>Forgot password? </Text>
              <Text style={styles.linkTextGreen}>Reset it</Text>
            </View>
          </View>
        )}

        {/* Register Form */}
        {mode === "register" && (
          <View style={styles.formContainer}>
            <View style={styles.inputGroup}>
              <Text style={styles.label}>FULL NAME</Text>
              <TextInput
                style={styles.input}
                placeholder="Costa Mwangi"
                placeholderTextColor="#555"
                value={fullName}
                onChangeText={setFullName}
                editable={!loading}
              />
            </View>

            <View style={styles.inputGroup}>
              <Text style={styles.label}>EMAIL</Text>
              <TextInput
                style={styles.input}
                placeholder="you@example.com"
                placeholderTextColor="#555"
                value={email}
                onChangeText={setEmail}
                editable={!loading}
              />
            </View>

            <View style={styles.inputGroup}>
              <Text style={styles.label}>ZIP CODE</Text>
              <TextInput
                style={styles.input}
                placeholder="19103"
                placeholderTextColor="#555"
                value={zipCode}
                onChangeText={setZipCode}
                editable={!loading}
              />
            </View>

            <View style={styles.hintContainer}>
              <Text style={styles.hintIcon}>📍</Text>
              <Text style={styles.hintText}>Zip sets your regional price data</Text>
            </View>

            <View style={styles.inputGroup}>
              <Text style={styles.label}>PASSWORD</Text>
              <TextInput
                style={styles.input}
                placeholder="6+ characters"
                placeholderTextColor="#555"
                value={password}
                onChangeText={setPassword}
                secureTextEntry
                editable={!loading}
              />
            </View>

            <View style={styles.inputGroup}>
              <Text style={styles.label}>CONFIRM</Text>
              <TextInput
                style={styles.input}
                placeholder="••••••••"
                placeholderTextColor="#555"
                value={confirmPassword}
                onChangeText={setConfirmPassword}
                secureTextEntry
                editable={!loading}
              />
            </View>

            <TouchableOpacity
              onPress={handleRegister}
              disabled={loading}
              style={styles.submitButton}
            >
              {loading ? (
                <ActivityIndicator color="#1a1a2e" />
              ) : (
                <Text style={styles.submitButtonText}>Create free account</Text>
              )}
            </TouchableOpacity>

            <View style={styles.footerLink}>
              <Text style={styles.footerText}>Already have an account? </Text>
              <TouchableOpacity onPress={() => setMode("signin")}>
                <Text style={styles.linkTextGreen}>Sign in</Text>
              </TouchableOpacity>
            </View>
          </View>
        )}
      </View>

      {/* Features Section */}
      <View style={styles.featuresSection}>
        <View style={styles.featureBadge}>
          <View style={styles.featureDot} />
          <Text style={styles.featureText}>
            {mode === "signin" ? "6-month forecasts" : "Anomaly detection"}
          </Text>
        </View>
        <View style={styles.featureBadge}>
          <View style={styles.featureDot} />
          <Text style={styles.featureText}>
            {mode === "signin" ? "Cart optimizer" : "Inflation shield"}
          </Text>
        </View>
        <View style={styles.featureBadge}>
          <View style={styles.featureDot} />
          <Text style={styles.featureText}>
            {mode === "signin" ? "Price alerts" : "Deal DNA"}
          </Text>
        </View>
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#FAFAF6",
    width: "100%",
  },
  contentContainer: {
    paddingHorizontal: "5%",
    paddingVertical: "8%",
    flexGrow: 1,
  },
  closeButton: {
    paddingVertical: "2%",
    marginBottom: "4%",
  },
  closeButtonText: {
    fontSize: 13,
    fontWeight: "600",
    color: "#10b981",
  },
  logoSection: {
    alignItems: "center",
    marginBottom: "8%",
  },
  logoBadge: {
    width: "20%",
    aspectRatio: 1,
    borderRadius: 24,
    borderWidth: 2,
    borderColor: "#10b981",
    alignItems: "center",
    justifyContent: "center",
    marginBottom: "4%",
    backgroundColor: "#f0fdf4",
  },
  appName: {
    fontSize: 32,
    fontWeight: "700",
    color: "#111",
    marginBottom: "1%",
  },
  appTagline: {
    fontSize: 13,
    color: "#666",
  },
  card: {
    backgroundColor: "#fff",
    borderWidth: 1,
    borderColor: "#E5E5E5",
    borderRadius: 16,
    paddingVertical: "5%",
    marginBottom: "6%",
  },
  tabContainer: {
    flexDirection: "row",
    borderBottomWidth: 1,
    borderBottomColor: "#E5E5E5",
    marginBottom: "4%",
  },
  tabButton: {
    flex: 1,
    paddingVertical: "2%",
    alignItems: "center",
    borderBottomWidth: 2,
    borderBottomColor: "transparent",
  },
  tabButtonActive: {
    borderBottomColor: "#10b981",
  },
  tabText: {
    fontSize: 13,
    color: "#999",
    fontWeight: "600",
  },
  tabTextActive: {
    color: "#111",
  },
  errorContainer: {
    flexDirection: "row",
    alignItems: "center",
    backgroundColor: "#fee2e2",
    borderRadius: 8,
    paddingVertical: "2%",
    paddingHorizontal: "3%",
    marginHorizontal: "4%",
    marginBottom: "3%",
  },
  errorText: {
    color: "#EF4444",
    marginLeft: "2%",
    fontSize: 12,
    flex: 1,
  },
  formContainer: {
    paddingHorizontal: "4%",
    gap: "3%",
  },
  inputGroup: {
    marginBottom: 0,
  },
  label: {
    fontSize: 10,
    fontWeight: "700",
    color: "#999",
    marginBottom: "2%",
    letterSpacing: 0.5,
  },
  input: {
    backgroundColor: "#f9f9f7",
    borderWidth: 1,
    borderColor: "#E5E5E5",
    borderRadius: 8,
    paddingVertical: "3%",
    paddingHorizontal: "3%",
    fontSize: 14,
    color: "#111",
    minHeight: 48,
  },
  hintContainer: {
    flexDirection: "row",
    alignItems: "center",
    marginBottom: "1%",
    marginTop: "-1.5%",
  },
  hintIcon: {
    fontSize: 13,
    marginRight: "1%",
  },
  hintText: {
    fontSize: 11,
    color: "#10b981",
  },
  submitButton: {
    backgroundColor: "#10b981",
    borderRadius: 8,
    paddingVertical: "3.5%",
    alignItems: "center",
    marginTop: "2%",
    minHeight: 48,
    justifyContent: "center",
  },
  submitButtonText: {
    color: "#fff",
    fontSize: 15,
    fontWeight: "700",
  },
  footerLink: {
    flexDirection: "row",
    justifyContent: "center",
    alignItems: "center",
    marginTop: "3%",
  },
  footerText: {
    color: "#999",
    fontSize: 12,
  },
  linkTextGreen: {
    color: "#10b981",
    fontSize: 12,
    fontWeight: "600",
  },
  featuresSection: {
    gap: "2%",
    marginTop: "4%",
    paddingHorizontal: "5%",
  },
  featureBadge: {
    flexDirection: "row",
    alignItems: "center",
    backgroundColor: "#fff",
    borderWidth: 1,
    borderColor: "#E5E5E5",
    borderRadius: 12,
    paddingVertical: "2.5%",
    paddingHorizontal: "3%",
  },
  featureDot: {
    width: 6,
    height: 6,
    borderRadius: 3,
    backgroundColor: "#10b981",
    marginRight: "2%",
  },
  featureText: {
    color: "#666",
    fontSize: 12,
    fontWeight: "500",
    flex: 1,
  },
});
