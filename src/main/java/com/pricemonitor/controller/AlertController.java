package com.pricemonitor.controller;

import com.pricemonitor.model.Alerts;
import com.pricemonitor.service.AlertService;
import com.pricemonitor.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/alerts")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:19006"})
@RequiredArgsConstructor
public class AlertController {

	private final AlertService alertService;
	private final AuthService authService;

	@GetMapping
	public ResponseEntity<?> getAlerts(
		@RequestHeader("Authorization") String authHeader,
		@RequestParam(value = "unreadOnly", defaultValue = "false") boolean unreadOnly) {

		try {
			UUID userId = extractUserIdFromToken(authHeader);

			List<Alerts> alerts = unreadOnly
				? alertService.getUnreadAlerts(userId)
				: alertService.getAllAlerts(userId);

			List<Map<String, Object>> alertList = alerts.stream()
				.map(this::alertToMap)
				.collect(Collectors.toList());

			Map<String, Object> response = new HashMap<>();
			response.put("alerts", alertList);
			response.put("unreadCount", alertService.getUnreadCount(userId));

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}

	@GetMapping("/{id}")
	public ResponseEntity<?> getAlert(
		@PathVariable UUID id,
		@RequestHeader("Authorization") String authHeader) {

		try {
			UUID userId = extractUserIdFromToken(authHeader);

			return ResponseEntity.ok(Map.of("success", true));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}

	@PatchMapping("/{id}/read")
	public ResponseEntity<?> markAlertAsRead(
		@PathVariable UUID id,
		@RequestHeader("Authorization") String authHeader) {

		try {
			UUID userId = extractUserIdFromToken(authHeader);
			alertService.markAsRead(id);

			return ResponseEntity.ok(Map.of("success", true, "message", "Alert marked as read"));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
		}
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteAlert(
		@PathVariable UUID id,
		@RequestHeader("Authorization") String authHeader) {

		try {
			UUID userId = extractUserIdFromToken(authHeader);
			alertService.deleteAlert(id);

			return ResponseEntity.ok(Map.of("success", true, "message", "Alert deleted"));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
		}
	}

	@DeleteMapping
	public ResponseEntity<?> clearAllAlerts(@RequestHeader("Authorization") String authHeader) {
		try {
			UUID userId = extractUserIdFromToken(authHeader);

			return ResponseEntity.ok(Map.of("success", true, "message", "All alerts cleared"));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}

	private Map<String, Object> alertToMap(Alerts alert) {
		Map<String, Object> map = new HashMap<>();
		map.put("id", alert.getId());
		map.put("type", alert.getAlertType());
		map.put("title", alert.getTitle());
		map.put("description", alert.getDescription());
		map.put("actionText", alert.getActionText());
		map.put("isRead", alert.getIsRead());
		map.put("createdAt", alert.getCreatedAt());
		map.put("productId", alert.getProductId());
		map.put("storeId", alert.getStoreId());
		return map;
	}

	private UUID extractUserIdFromToken(String authHeader) {
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			throw new RuntimeException("Invalid or missing authorization header");
		}

		String token = authHeader.substring(7);
		return authService.getUserFromToken(token).getId();
	}
}
