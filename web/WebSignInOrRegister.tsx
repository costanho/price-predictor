import React, { useState } from "react";
import { StyleSheet, View, Text, TextInput, TouchableOpacity, ActivityIndicator } from "react-native";
import { MaterialIcons } from "@expo/vector-icons";
import useStore from "../store/useStore";

interface WebSignInOrRegisterProps {
  onClose?: () => void;
  onAuthSuccess?: () => void;
}

type AuthMode = "signin" | "register";

export default function WebSignInOrRegister({ onClose, onAuthSuccess }: WebSignInOrRegisterProps) {
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
    <View style={styles.container}>
      <View style={styles.card}>
        {/* Close Button */}
        {onClose && (
          <TouchableOpacity onPress={onClose} style={styles.closeButton}>
            <Text style={styles.closeButtonText}>← Return to dashboard</Text>
          </TouchableOpacity>
        )}

        {/* Header */}
        <View style={styles.header}>
          <Text style={styles.title}>
            {mode === "signin" ? "Welcome back" : "Start saving today"}
          </Text>
          <Text style={styles.subtitle}>
            {mode === "signin"
              ? "Sign in to your account or create a new one"
              : "Free account · No credit card · 2 minutes"}
          </Text>
        </View>

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
              Create account
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
              <Text style={styles.label}>EMAIL ADDRESS</Text>
              <TextInput
                style={styles.input}
                placeholder="you@example.com"
                placeholderTextColor="#666"
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
                placeholderTextColor="#666"
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

            <View style={styles.linksContainer}>
              <Text style={styles.linkText}>Forgot password?</Text>
              <Text style={styles.separator}>·</Text>
              <Text style={styles.linkText}>No account? </Text>
              <TouchableOpacity onPress={() => setMode("register")}>
                <Text style={styles.linkTextGreen}>Register free</Text>
              </TouchableOpacity>
            </View>
          </View>
        )}

        {/* Register Form */}
        {mode === "register" && (
          <View style={styles.formContainer}>
            <View style={styles.rowContainer}>
              <View style={[styles.inputGroup, { flex: 1, marginRight: 12 }]}>
                <Text style={styles.label}>FULL NAME</Text>
                <TextInput
                  style={styles.input}
                  placeholder="Jane Smith"
                  placeholderTextColor="#666"
                  value={fullName}
                  onChangeText={setFullName}
                  editable={!loading}
                />
              </View>
              <View style={[styles.inputGroup, { flex: 1 }]}>
                <Text style={styles.label}>ZIP CODE</Text>
                <TextInput
                  style={styles.input}
                  placeholder="19103"
                  placeholderTextColor="#666"
                  value={zipCode}
                  onChangeText={setZipCode}
                  editable={!loading}
                />
              </View>
            </View>

            <View style={styles.hintContainer}>
              <Text style={styles.hintIcon}>📍</Text>
              <Text style={styles.hintText}>For regional price data</Text>
            </View>

            <View style={styles.inputGroup}>
              <Text style={styles.label}>EMAIL ADDRESS</Text>
              <TextInput
                style={styles.input}
                placeholder="you@example.com"
                placeholderTextColor="#666"
                value={email}
                onChangeText={setEmail}
                editable={!loading}
              />
            </View>

            <View style={styles.rowContainer}>
              <View style={[styles.inputGroup, { flex: 1, marginRight: 12 }]}>
                <Text style={styles.label}>PASSWORD</Text>
                <TextInput
                  style={styles.input}
                  placeholder="6+ characters"
                  placeholderTextColor="#666"
                  value={password}
                  onChangeText={setPassword}
                  secureTextEntry
                  editable={!loading}
                />
              </View>
              <View style={[styles.inputGroup, { flex: 1 }]}>
                <Text style={styles.label}>CONFIRM</Text>
                <TextInput
                  style={styles.input}
                  placeholder="••••••••"
                  placeholderTextColor="#666"
                  value={confirmPassword}
                  onChangeText={setConfirmPassword}
                  secureTextEntry
                  editable={!loading}
                />
              </View>
            </View>

            <TouchableOpacity
              onPress={handleRegister}
              disabled={loading}
              style={styles.submitButton}
            >
              {loading ? (
                <ActivityIndicator color="#1a1a2e" />
              ) : (
                <Text style={styles.submitButtonText}>Create account — it's free</Text>
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
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#FAFAF6",
    justifyContent: "center",
    alignItems: "center",
    padding: 24,
  },
  card: {
    width: "100%",
    maxWidth: 600,
    backgroundColor: "#FAFAF6",
  },
  closeButton: {
    paddingHorizontal: 0,
    paddingVertical: 12,
    marginBottom: 20,
  },
  closeButtonText: {
    fontSize: 13,
    fontWeight: "600",
    color: "#10b981",
  },
  header: {
    marginBottom: 32,
    alignItems: "center",
  },
  title: {
    fontSize: 40,
    fontWeight: "700",
    color: "#111",
    marginBottom: 8,
  },
  subtitle: {
    fontSize: 16,
    color: "#666",
    textAlign: "center",
  },
  tabContainer: {
    flexDirection: "row",
    marginBottom: 32,
    borderBottomWidth: 1,
    borderBottomColor: "#E5E5E5",
  },
  tabButton: {
    flex: 1,
    paddingVertical: 12,
    borderBottomWidth: 2,
    borderBottomColor: "transparent",
    alignItems: "center",
  },
  tabButtonActive: {
    borderBottomColor: "#10b981",
  },
  tabText: {
    fontSize: 16,
    color: "#999",
    fontWeight: "500",
  },
  tabTextActive: {
    color: "#111",
  },
  errorContainer: {
    flexDirection: "row",
    alignItems: "center",
    backgroundColor: "#fee2e2",
    borderRadius: 8,
    padding: 12,
    marginBottom: 20,
  },
  errorText: {
    color: "#EF4444",
    marginLeft: 8,
    fontSize: 14,
    flex: 1,
  },
  formContainer: {
    gap: 16,
  },
  inputGroup: {
    marginBottom: 0,
  },
  label: {
    fontSize: 12,
    fontWeight: "600",
    color: "#999",
    marginBottom: 8,
    letterSpacing: 0.5,
  },
  input: {
    backgroundColor: "#fff",
    borderWidth: 1,
    borderColor: "#E5E5E5",
    borderRadius: 8,
    paddingVertical: 12,
    paddingHorizontal: 16,
    fontSize: 14,
    color: "#111",
  },
  rowContainer: {
    flexDirection: "row",
    gap: 12,
  },
  hintContainer: {
    flexDirection: "row",
    alignItems: "center",
    marginBottom: 8,
  },
  hintIcon: {
    fontSize: 14,
    marginRight: 6,
  },
  hintText: {
    fontSize: 13,
    color: "#10b981",
  },
  submitButton: {
    backgroundColor: "#10b981",
    borderRadius: 8,
    paddingVertical: 14,
    alignItems: "center",
    marginTop: 12,
  },
  submitButtonText: {
    color: "#fff",
    fontSize: 16,
    fontWeight: "700",
  },
  linksContainer: {
    flexDirection: "row",
    justifyContent: "center",
    alignItems: "center",
    marginTop: 16,
  },
  linkText: {
    color: "#999",
    fontSize: 14,
  },
  linkTextGreen: {
    color: "#10b981",
    fontSize: 14,
    fontWeight: "600",
  },
  separator: {
    color: "#999",
    marginHorizontal: 8,
  },
  footerLink: {
    flexDirection: "row",
    justifyContent: "center",
    marginTop: 16,
  },
  footerText: {
    color: "#999",
    fontSize: 14,
  },
});
