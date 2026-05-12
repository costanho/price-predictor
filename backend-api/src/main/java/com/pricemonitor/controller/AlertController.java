package com.pricemonitor.controller;

import com.pricemonitor.model.Alerts;
import com.pricemonitor.repository.AlertsRepository;
import com.pricemonitor.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/alerts")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:19006"})
@RequiredArgsConstructor
public class AlertController {

	private final AlertsRepository alertsRepository;
	private final JwtUtil jwtUtil;

	// ── GET /alerts — all active alerts for the user ─────────────────────────
	// Query params:
	//   unreadOnly=true  → only unread
	//   limit=3          → cap results (for dashboard strip)
	@GetMapping
	public ResponseEntity<Map<String, Object>> getAlerts(
			@RequestHeader("Authorization") String authHeader,
			@RequestParam(defaultValue = "false") boolean unreadOnly,
			@RequestParam(defaultValue = "50") int limit) {

		String token = authHeader.replace("Bearer ", "");
		UUID userId = UUID.fromString(jwtUtil.getUserIdFromToken(token));
		LocalDateTime now = LocalDateTime.now();

		List<Alerts> alerts = unreadOnly
			? alertsRepository.findUnreadByUserId(userId, now)
			: alertsRepository.findActiveByUserId(userId, now);

		// Apply limit
		if (alerts.size() > limit) {
			alerts = alerts.subList(0, limit);
		}

		long unreadCount = alertsRepository.countUnreadByUserId(userId, now);

		return ResponseEntity.ok(Map.of(
			"alerts", alerts.stream().map(this::mapToResponse).toList(),
			"unreadCount", unreadCount,
			"total", alerts.size()
		));
	}

	// ── PATCH /alerts/{id}/read — mark one alert as read ────────────────────
	@PatchMapping("/{id}/read")
	public ResponseEntity<Map<String, Object>> markRead(
			@PathVariable UUID id,
			@RequestHeader("Authorization") String authHeader) {

		String token = authHeader.replace("Bearer ", "");
		UUID userId = UUID.fromString(jwtUtil.getUserIdFromToken(token));

		Optional<Alerts> alertOpt = alertsRepository.findById(id);
		if (alertOpt.isEmpty()) {
			return ResponseEntity.notFound().build();
		}

		Alerts alert = alertOpt.get();
		// Security — users can only mark their own alerts
		if (!alert.getUserId().equals(userId)) {
			return ResponseEntity.status(403).build();
		}

		alert.setIsRead(true);
		alertsRepository.save(alert);

		long newUnreadCount = alertsRepository.countUnreadByUserId(
			userId, LocalDateTime.now()
		);

		return ResponseEntity.ok(Map.of(
			"success", true,
			"unreadCount", newUnreadCount
		));
	}

	// ── PATCH /alerts/read-all — mark all alerts as read ────────────────────
	@PatchMapping("/read-all")
	public ResponseEntity<Map<String, Object>> markAllRead(
			@RequestHeader("Authorization") String authHeader) {

		String token = authHeader.replace("Bearer ", "");
		UUID userId = UUID.fromString(jwtUtil.getUserIdFromToken(token));
		List<Alerts> unread = alertsRepository.findUnreadByUserId(userId, LocalDateTime.now());

		for (Alerts alert : unread) {
			alert.setIsRead(true);
		}
		alertsRepository.saveAll(unread);

		return ResponseEntity.ok(Map.of(
			"success", true,
			"markedRead", unread.size(),
			"unreadCount", 0
		));
	}

	// ── DELETE /alerts/{id} — soft delete one alert ──────────────────────────
	@DeleteMapping("/{id}")
	public ResponseEntity<Map<String, Object>> deleteAlert(
			@PathVariable UUID id,
			@RequestHeader("Authorization") String authHeader) {

		String token = authHeader.replace("Bearer ", "");
		UUID userId = UUID.fromString(jwtUtil.getUserIdFromToken(token));
		Optional<Alerts> alertOpt = alertsRepository.findById(id);

		if (alertOpt.isEmpty()) return ResponseEntity.notFound().build();

		Alerts alert = alertOpt.get();
		if (!alert.getUserId().equals(userId)) {
			return ResponseEntity.status(403).build();
		}

		alertsRepository.softDelete(id, LocalDateTime.now());
		return ResponseEntity.ok(Map.of("success", true));
	}

	// ── GET /alerts/count — just the unread count (for nav badge) ───────────
	@GetMapping("/count")
	public ResponseEntity<Map<String, Long>> getUnreadCount(
			@RequestHeader("Authorization") String authHeader) {

		String token = authHeader.replace("Bearer ", "");
		UUID userId = UUID.fromString(jwtUtil.getUserIdFromToken(token));
		long count = alertsRepository.countUnreadByUserId(userId, LocalDateTime.now());
		return ResponseEntity.ok(Map.of("unreadCount", count));
	}

	// ── Map entity to response ────────────────────────────────────────────────
	private Map<String, Object> mapToResponse(Alerts alert) {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("id", alert.getId());
		map.put("productId", alert.getProductId());
		map.put("storeId", alert.getStoreId());
		map.put("alertType", alert.getAlertType());
		map.put("title", alert.getTitle());
		map.put("description", alert.getDescription());
		map.put("actionText", alert.getActionText());
		map.put("isRead", alert.getIsRead());
		map.put("createdAt", alert.getCreatedAt());
		map.put("expiresAt", alert.getExpiresAt());
		return map;
	}
}
