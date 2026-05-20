package com.pricemonitor.controller;

import com.pricemonitor.model.NotificationPreferences;
import com.pricemonitor.model.Users;
import com.pricemonitor.model.UserTrackedStores;
import com.pricemonitor.repository.UserTrackedStoresRepository;
import com.pricemonitor.security.JwtUtil;
import com.pricemonitor.service.AuthService;
import com.pricemonitor.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:19006"})
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;
	private final AuthService authService;
	private final UserTrackedStoresRepository userTrackedStoreRepository;
	private final JwtUtil jwtUtil;

	@GetMapping("/profile")
	public ResponseEntity<?> getProfile(@RequestHeader("Authorization") String authHeader) {
		try {
			UUID userId = extractUserIdFromToken(authHeader);

			Map<String, Object> profileData = userService.getUserProfileData(userId);

			Map<String, Object> response = new HashMap<>();
			response.put("id", profileData.get("id"));
			response.put("email", profileData.get("email"));
			response.put("name", profileData.get("name"));
			response.put("zipCode", profileData.get("zipCode"));
			response.put("region", profileData.get("blsRegion"));
			response.put("searchRadiusMiles", profileData.get("searchRadiusMiles"));
			response.put("lastLoginAt", profileData.get("lastLoginAt"));

			@SuppressWarnings("unchecked")
			Map<String, Boolean> prefs = (Map<String, Boolean>) profileData.get("notificationPreferences");
			Map<String, Boolean> notificationsResponse = new HashMap<>();
			notificationsResponse.put("priceDrops", prefs.get("priceDrop"));
			notificationsResponse.put("anomalies", prefs.get("anomalyWarning"));
			notificationsResponse.put("dealDna", prefs.get("dealDna"));
			notificationsResponse.put("forecastUpdates", prefs.get("forecastUpdate"));
			notificationsResponse.put("weeklyEmail", prefs.get("weeklyEmail"));

			response.put("notifications", notificationsResponse);

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}

	@PutMapping("/profile")
	public ResponseEntity<?> updateProfile(
		@RequestHeader("Authorization") String authHeader,
		@RequestBody UpdateProfileRequest request) {

		try {
			UUID userId = extractUserIdFromToken(authHeader);

			Map<String, Object> updates = new HashMap<>();
			if (request.getName() != null) {
				updates.put("name", request.getName());
			}
			if (request.getZipCode() != null) {
				updates.put("zipCode", request.getZipCode());
			}
			if (request.getSearchRadiusMiles() != null) {
				updates.put("searchRadiusMiles", request.getSearchRadiusMiles());
			}

			userService.updateProfile(userId, updates);

			return ResponseEntity.ok(Map.of("success", true, "message", "Profile updated successfully"));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
		}
	}

	@PutMapping("/notifications")
	public ResponseEntity<?> updateNotifications(
		@RequestHeader("Authorization") String authHeader,
		@RequestBody UpdateNotificationsRequest request) {

		try {
			UUID userId = extractUserIdFromToken(authHeader);

			Map<String, Boolean> preferences = new HashMap<>();
			if (request.getPriceDrops() != null) {
				preferences.put("priceDrop", request.getPriceDrops());
			}
			if (request.getAnomalies() != null) {
				preferences.put("anomalyWarning", request.getAnomalies());
			}
			if (request.getDealDna() != null) {
				preferences.put("dealDna", request.getDealDna());
			}
			if (request.getForecastUpdates() != null) {
				preferences.put("forecastUpdate", request.getForecastUpdates());
			}
			if (request.getWeeklyEmail() != null) {
				preferences.put("weeklyEmail", request.getWeeklyEmail());
			}

			userService.updateNotificationPreferences(userId, preferences);

			return ResponseEntity.ok(Map.of("success", true, "message", "Notification preferences updated"));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
		}
	}

	@GetMapping("/notifications")
	public ResponseEntity<?> getNotifications(@RequestHeader("Authorization") String authHeader) {
		try {
			UUID userId = extractUserIdFromToken(authHeader);

			NotificationPreferences prefs = userService.getNotificationPreferences(userId);

			Map<String, Boolean> response = new HashMap<>();
			response.put("priceDrops", prefs.getPriceDrop());
			response.put("anomalies", prefs.getAnomalyWarning());
			response.put("dealDna", prefs.getDealDna());
			response.put("forecastUpdates", prefs.getForecastUpdate());
			response.put("weeklyEmail", prefs.getWeeklyEmail());

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}

	@GetMapping("/stores")
	public ResponseEntity<?> getUserStores(
			@RequestHeader("Authorization") String authHeader) {
		try {
			UUID userId = extractUserIdFromToken(authHeader);
			List<String> storeIds = userTrackedStoreRepository.findStoreIdsByUserId(userId);
			return ResponseEntity.ok(Map.of("storeIds", storeIds));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}

	@PostMapping("/stores/{storeId}")
	public ResponseEntity<Map<String, String>> addStore(
			@PathVariable String storeId,
			@RequestHeader("Authorization") String authHeader) {
		try {
			UUID userId = extractUserIdFromToken(authHeader);
			if (!userTrackedStoreRepository.existsByUserIdAndStoreId(userId, storeId)) {
				UserTrackedStores uts = new UserTrackedStores();
				UserTrackedStores.UserTrackedStoresKey key = new UserTrackedStores.UserTrackedStoresKey();
				key.setUserId(userId);
				key.setStoreId(storeId);
				uts.setId(key);
				uts.setAddedAt(LocalDateTime.now());
				userTrackedStoreRepository.save(uts);
			}
			return ResponseEntity.ok(Map.of("status", "added"));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}

	@DeleteMapping("/stores/{storeId}")
	public ResponseEntity<Map<String, String>> removeStore(
			@PathVariable String storeId,
			@RequestHeader("Authorization") String authHeader) {
		try {
			UUID userId = extractUserIdFromToken(authHeader);
			userTrackedStoreRepository.deleteByUserIdAndStoreId(userId, storeId);
			return ResponseEntity.ok(Map.of("status", "removed"));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}

	private UUID extractUserIdFromToken(String authHeader) {
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			throw new RuntimeException("Invalid or missing authorization header");
		}

		String token = authHeader.substring(7);
		return authService.getUserFromToken(token).getId();
	}

	public static class UpdateProfileRequest {
		private String name;
		private String zipCode;
		private Integer searchRadiusMiles;

		public String getName() { return name; }
		public void setName(String name) { this.name = name; }

		public String getZipCode() { return zipCode; }
		public void setZipCode(String zipCode) { this.zipCode = zipCode; }

		public Integer getSearchRadiusMiles() { return searchRadiusMiles; }
		public void setSearchRadiusMiles(Integer searchRadiusMiles) { this.searchRadiusMiles = searchRadiusMiles; }
	}

	public static class UpdateNotificationsRequest {
		private Boolean priceDrops;
		private Boolean anomalies;
		private Boolean dealDna;
		private Boolean forecastUpdates;
		private Boolean weeklyEmail;

		public Boolean getPriceDrops() { return priceDrops; }
		public void setPriceDrops(Boolean priceDrops) { this.priceDrops = priceDrops; }

		public Boolean getAnomalies() { return anomalies; }
		public void setAnomalies(Boolean anomalies) { this.anomalies = anomalies; }

		public Boolean getDealDna() { return dealDna; }
		public void setDealDna(Boolean dealDna) { this.dealDna = dealDna; }

		public Boolean getForecastUpdates() { return forecastUpdates; }
		public void setForecastUpdates(Boolean forecastUpdates) { this.forecastUpdates = forecastUpdates; }

		public Boolean getWeeklyEmail() { return weeklyEmail; }
		public void setWeeklyEmail(Boolean weeklyEmail) { this.weeklyEmail = weeklyEmail; }
	}
}
