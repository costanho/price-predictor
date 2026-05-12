package com.pricemonitor.service;

import com.pricemonitor.model.Alerts;
import com.pricemonitor.model.Products;
import com.pricemonitor.model.NotificationPreferences;
import com.pricemonitor.repository.AlertsRepository;
import com.pricemonitor.repository.ProductsRepository;
import com.pricemonitor.repository.NotificationPreferencesRepository;
import com.pricemonitor.repository.PriceHistoryRepository;
import com.pricemonitor.repository.UserTrackedStoresRepository;
import com.pricemonitor.repository.UserProductTrackingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AlertGenerationService {

	private final AlertsRepository alertsRepository;
	private final ProductsRepository productsRepository;
	private final NotificationPreferencesRepository notificationPreferencesRepository;
	private final PriceHistoryRepository priceHistoryRepository;
	private final UserTrackedStoresRepository userTrackedStoresRepository;

	// ── TRIGGER 1: Called by PriceSyncService after each product sync ─────────
	// Checks if the new price is a significant drop and generates alert
	@Transactional
	public void checkPriceDrop(UUID productId, String storeId, BigDecimal newPrice) {
		// Get price from 7 days ago to compare
		List<Object[]> recentPrices = priceHistoryRepository.findPricesLastNDays(productId, storeId, 7);
		if (recentPrices.isEmpty()) return;

		BigDecimal previousPrice = (BigDecimal) recentPrices.get(0)[1];
		if (previousPrice == null || previousPrice.compareTo(BigDecimal.ZERO) == 0) return;

		BigDecimal dropPercent = previousPrice.subtract(newPrice)
			.divide(previousPrice, 4, RoundingMode.HALF_UP)
			.multiply(BigDecimal.valueOf(100));

		// Only alert if price dropped 3% or more
		if (dropPercent.compareTo(BigDecimal.valueOf(3)) < 0) return;

		String productName = productsRepository.findById(productId)
			.map(Products::getName).orElse("Product");
		String storeName = storeId.replace("_", " ");

		// For now, generate one alert per product per store drop
		// (Full user tracking would require UserProductTrackingRepository)
		Alerts alert = new Alerts();
		alert.setAlertType("price_drop");
		alert.setTitle(productName + " price dropped at " + capitalize(storeName));
		alert.setDescription(String.format(
			"Down %.1f%% to $%.2f — was $%.2f",
			dropPercent.doubleValue(),
			newPrice.doubleValue(),
			previousPrice.doubleValue()
		));
		alert.setActionText("Stock up now");
		alert.setExpiresAt(LocalDateTime.now().plusDays(3));
	}

	// ── TRIGGER 2: Called by ForecastService after generating a forecast ──────
	// Checks if predicted price change is significant enough to alert
	@Transactional
	public void checkForecastAlert(
			UUID productId,
			String regionCode,
			BigDecimal currentPrice,
			BigDecimal predictedPrice,
			String recommendation) {

		BigDecimal changePercent = predictedPrice.subtract(currentPrice)
			.divide(currentPrice, 4, RoundingMode.HALF_UP)
			.multiply(BigDecimal.valueOf(100));

		// Only alert for meaningful changes — drop > 5% or rise > 8%
		boolean significantDrop = changePercent.compareTo(BigDecimal.valueOf(-5)) < 0;
		boolean significantRise = changePercent.compareTo(BigDecimal.valueOf(8)) > 0;
		if (!significantDrop && !significantRise) return;

		String productName = productsRepository.findById(productId)
			.map(Products::getName).orElse("Product");

		Alerts alert = new Alerts();
		alert.setProductId(productId);
		alert.setAlertType("forecast_update");
		alert.setExpiresAt(LocalDateTime.now().plusDays(7));

		if (significantDrop) {
			alert.setTitle(productName + " predicted to drop");
			alert.setDescription(String.format(
				"AI forecast: %.1f%% price drop next month. Buy before it rises again.",
				Math.abs(changePercent.doubleValue())
			));
			alert.setActionText("Buy now at $" + String.format("%.2f", currentPrice.doubleValue()));
		} else {
			alert.setTitle(productName + " price rising soon");
			alert.setDescription(String.format(
				"AI forecast: %.1f%% price increase expected next month.",
				changePercent.doubleValue()
			));
			alert.setActionText("Stock up before the increase");
		}
	}

	// ── TRIGGER 3: Called by AnomalyDetectionService ──────────────────────────
	@Transactional
	public void checkAnomalyAlert(
			UUID productId,
			String regionCode,
			String severity,
			double reconstructionError) {

		// Only alert for medium and high severity
		if ("none".equals(severity) || "low".equals(severity)) return;

		String productName = productsRepository.findById(productId)
			.map(Products::getName).orElse("Product");

		Alerts alert = new Alerts();
		alert.setProductId(productId);
		alert.setAlertType("anomaly");
		alert.setExpiresAt(LocalDateTime.now().plusDays(2));

		if ("high".equals(severity)) {
			alert.setTitle("⚠ Unusual price spike: " + productName);
			alert.setDescription(
				"Our AI detected an abnormal price pattern — " +
				"possibly a supply disruption or data error. " +
				"Verify before purchasing."
			);
			alert.setActionText("View price history");
		} else {
			alert.setTitle("Price irregularity: " + productName);
			alert.setDescription(
				"Minor unusual price movement detected. " +
				"May return to normal soon."
			);
			alert.setActionText("Monitor this item");
		}
	}

	// ── TRIGGER 4: Called when user's target price is reached ─────────────────
	@Transactional
	public void checkTargetPriceReached(
			UUID productId,
			String storeId,
			BigDecimal currentPrice) {

		String productName = productsRepository.findById(productId)
			.map(Products::getName).orElse("Product");

		Alerts alert = new Alerts();
		alert.setProductId(productId);
		alert.setStoreId(storeId);
		alert.setAlertType("target_met");
		alert.setTitle("Your target price reached: " + productName);
		alert.setDescription(String.format(
			"Now $%.2f at %s",
			currentPrice.doubleValue(),
			capitalize(storeId.replace("_", " "))
		));
		alert.setActionText("Buy now");
		alert.setExpiresAt(LocalDateTime.now().plusDays(1));
	}

	private String capitalize(String s) {
		if (s == null || s.isEmpty()) return s;
		return Character.toUpperCase(s.charAt(0)) + s.substring(1);
	}
}
