package com.pricemonitor.service;

import com.pricemonitor.model.Alerts;
import com.pricemonitor.repository.AlertsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AlertService {

	private final AlertsRepository alertsRepository;

	public List<Alerts> getUnreadAlerts(UUID userId) {
		return alertsRepository.findUnreadByUserId(userId, LocalDateTime.now());
	}

	public List<Alerts> getAllAlerts(UUID userId) {
		return alertsRepository.findActiveByUserId(userId, LocalDateTime.now());
	}

	public Alerts markAsRead(UUID alertId) {
		Alerts alert = alertsRepository.findById(alertId)
			.orElseThrow(() -> new RuntimeException("Alert not found"));

		alert.setIsRead(true);
		return alertsRepository.save(alert);
	}

	public Alerts generatePriceDropAlert(UUID userId, UUID productId, String productName, BigDecimal oldPrice, BigDecimal newPrice) {
		BigDecimal dropAmount = oldPrice.subtract(newPrice);
		BigDecimal dropPercent = dropAmount.divide(oldPrice, 4, java.math.RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));

		Alerts alert = new Alerts();
		alert.setUserId(userId);
		alert.setProductId(productId);
		alert.setAlertType("price_drop");
		alert.setTitle("Price Drop: " + productName);
		alert.setDescription("Price dropped from $" + oldPrice + " to $" + newPrice + " (" + dropPercent + "% off)");
		alert.setActionText("View Product");
		alert.setIsRead(false);
		alert.setCreatedAt(LocalDateTime.now());
		alert.setExpiresAt(LocalDateTime.now().plusDays(7));

		return alertsRepository.save(alert);
	}

	public Alerts generateAnomalyAlert(UUID userId, UUID productId, String productName, String anomalyReason) {
		Alerts alert = new Alerts();
		alert.setUserId(userId);
		alert.setProductId(productId);
		alert.setAlertType("anomaly_warning");
		alert.setTitle("Price Anomaly: " + productName);
		alert.setDescription(anomalyReason);
		alert.setActionText("Review Details");
		alert.setIsRead(false);
		alert.setCreatedAt(LocalDateTime.now());
		alert.setExpiresAt(LocalDateTime.now().plusDays(7));

		return alertsRepository.save(alert);
	}

	public Alerts generateDealAlert(UUID userId, UUID productId, String productName, String storeName) {
		Alerts alert = new Alerts();
		alert.setUserId(userId);
		alert.setProductId(productId);
		alert.setAlertType("deal_dna");
		alert.setTitle("Deal Coming: " + productName);
		alert.setDescription("Expected sale at " + storeName + " based on historical patterns");
		alert.setActionText("Track Deal");
		alert.setIsRead(false);
		alert.setCreatedAt(LocalDateTime.now());
		alert.setExpiresAt(LocalDateTime.now().plusDays(7));

		return alertsRepository.save(alert);
	}

	public void deleteAlert(UUID alertId) {
		Alerts alert = alertsRepository.findById(alertId)
			.orElseThrow(() -> new RuntimeException("Alert not found"));
		alert.setDeletedAt(LocalDateTime.now());
		alertsRepository.save(alert);
	}

	public Long getUnreadCount(UUID userId) {
		return alertsRepository.countUnreadByUserId(userId, LocalDateTime.now());
	}

	// Soft delete expired alerts (scheduled job)
	public void cleanupExpiredAlerts() {
		alertsRepository.softDeleteExpired(LocalDateTime.now());
	}

	// Hard delete old soft-deleted alerts (7+ days)
	public void purgeDeletedAlerts() {
		alertsRepository.hardDeleteOldDeleted(LocalDateTime.now().minusDays(7));
	}
}
