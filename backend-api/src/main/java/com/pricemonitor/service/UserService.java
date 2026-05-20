package com.pricemonitor.service;

import com.pricemonitor.model.NotificationPreferences;
import com.pricemonitor.model.Users;
import com.pricemonitor.repository.NotificationPreferencesRepository;
import com.pricemonitor.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UsersRepository usersRepository;
	private final NotificationPreferencesRepository notificationPreferencesRepository;

	public Users getProfile(UUID userId) {
		return usersRepository.findById(userId)
			.orElseThrow(() -> new RuntimeException("User not found"));
	}

	public Users updateProfile(UUID userId, Map<String, Object> updates) {
		Users user = usersRepository.findById(userId)
			.orElseThrow(() -> new RuntimeException("User not found"));

		if (updates.containsKey("name")) {
			user.setName((String) updates.get("name"));
		}
		if (updates.containsKey("zipCode")) {
			user.setZipCode((String) updates.get("zipCode"));
		}
		if (updates.containsKey("blsRegion")) {
			user.setBlsRegion((String) updates.get("blsRegion"));
		}
		if (updates.containsKey("searchRadiusMiles")) {
			user.setSearchRadiusMiles(((Number) updates.get("searchRadiusMiles")).intValue());
		}
		if (updates.containsKey("isActive")) {
			user.setIsActive((Boolean) updates.get("isActive"));
		}

		user.setUpdatedAt(LocalDateTime.now());
		return usersRepository.save(user);
	}

	public NotificationPreferences getNotificationPreferences(UUID userId) {
		return notificationPreferencesRepository.findByUserId(userId)
			.orElseGet(() -> {
				NotificationPreferences prefs = new NotificationPreferences();
				prefs.setUserId(userId);
				prefs.setPriceDrop(true);
				prefs.setAnomalyWarning(true);
				prefs.setDealDna(true);
				prefs.setForecastUpdate(true);
				prefs.setWeeklyEmail(true);
				prefs.setUpdatedAt(LocalDateTime.now());
				return notificationPreferencesRepository.save(prefs);
			});
	}

	public NotificationPreferences updateNotificationPreferences(UUID userId, Map<String, Boolean> preferences) {
		NotificationPreferences prefs = getNotificationPreferences(userId);

		if (preferences.containsKey("priceDrop")) {
			prefs.setPriceDrop(preferences.get("priceDrop"));
		}
		if (preferences.containsKey("anomalyWarning")) {
			prefs.setAnomalyWarning(preferences.get("anomalyWarning"));
		}
		if (preferences.containsKey("dealDna")) {
			prefs.setDealDna(preferences.get("dealDna"));
		}
		if (preferences.containsKey("forecastUpdate")) {
			prefs.setForecastUpdate(preferences.get("forecastUpdate"));
		}
		if (preferences.containsKey("weeklyEmail")) {
			prefs.setWeeklyEmail(preferences.get("weeklyEmail"));
		}

		prefs.setUpdatedAt(LocalDateTime.now());
		return notificationPreferencesRepository.save(prefs);
	}

	public Optional<Users> getUserByEmail(String email) {
		return usersRepository.findByEmail(email);
	}

	public void deleteUser(UUID userId) {
		usersRepository.deleteById(userId);
	}

	public Map<String, Object> getUserProfileData(UUID userId) {
		Users user = getProfile(userId);
		NotificationPreferences prefs = getNotificationPreferences(userId);

		Map<String, Object> data = new HashMap<>();
		data.put("id", user.getId());
		data.put("email", user.getEmail());
		data.put("name", user.getName());
		data.put("zipCode", user.getZipCode());
		data.put("blsRegion", user.getBlsRegion());
		data.put("searchRadiusMiles", user.getSearchRadiusMiles());
		data.put("isActive", user.getIsActive());
		data.put("createdAt", user.getCreatedAt());
		data.put("lastLoginAt", user.getLastLoginAt());
		data.put("notificationPreferences", Map.of(
			"priceDrop", prefs.getPriceDrop(),
			"anomalyWarning", prefs.getAnomalyWarning(),
			"dealDna", prefs.getDealDna(),
			"forecastUpdate", prefs.getForecastUpdate(),
			"weeklyEmail", prefs.getWeeklyEmail()
		));

		return data;
	}
}
