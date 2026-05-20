package com.pricemonitor.controller;

import com.pricemonitor.dto.ForecastResponse;
import com.pricemonitor.model.Products;
import com.pricemonitor.repository.ProductsRepository;
import com.pricemonitor.service.AuthService;
import com.pricemonitor.service.ForecastService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/forecasts")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:19006"})
@RequiredArgsConstructor
public class ForecastController {

	private final ForecastService forecastService;
	private final AuthService authService;
	private final ProductsRepository productsRepository;

	@GetMapping("/summary")
	public ResponseEntity<?> getForecastSummary(
		@RequestParam(value = "region", defaultValue = "national") String region,
		@RequestParam(value = "page", defaultValue = "1") int page,
		@RequestParam(value = "pageSize", defaultValue = "50") int pageSize,
		@RequestHeader(value = "Authorization", required = false) String authHeader) {

		try {
			UUID userId = null;
			if (authHeader != null && authHeader.startsWith("Bearer ")) {
				String token = authHeader.substring(7);
				userId = authService.getUserFromToken(token).getId();
			}

			List<Products> allProducts = productsRepository.findByIsActiveTrue();
			List<Map<String, Object>> allSummaries = new ArrayList<>();

			// Fetch all forecasts
			for (Products product : allProducts) {
				try {
					ForecastResponse forecast = forecastService.getForecast(product.getId(), region, userId);

					Map<String, Object> item = new LinkedHashMap<>();
					item.put("productId", forecast.getProductId());
					item.put("productName", product.getName());
					item.put("currentPrice", forecast.getCurrentPrice());
					item.put("predictedPrice", forecast.getPredictedPrice());
					item.put("recommendation", forecast.getRecommendation());
					allSummaries.add(item);
				} catch (Exception e) {
					// Skip products that fail to forecast
				}
			}

			// Calculate pagination
			int totalProducts = allSummaries.size();
			int totalPages = (int) Math.ceil((double) totalProducts / pageSize);
			int startIndex = (page - 1) * pageSize;
			int endIndex = Math.min(startIndex + pageSize, totalProducts);

			List<Map<String, Object>> paginatedSummaries = new ArrayList<>();
			if (startIndex < totalProducts) {
				paginatedSummaries = allSummaries.subList(startIndex, endIndex);
			}

			Map<String, Object> response = new LinkedHashMap<>();
			response.put("region", region);
			response.put("page", page);
			response.put("pageSize", pageSize);
			response.put("totalProducts", totalProducts);
			response.put("totalPages", totalPages);
			response.put("returned", paginatedSummaries.size());
			response.put("forecasts", paginatedSummaries);

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}

	@GetMapping("/all")
	public ResponseEntity<?> getAllForecasts(
		@RequestParam(value = "region", defaultValue = "national") String region,
		@RequestHeader(value = "Authorization", required = false) String authHeader) {

		try {
			UUID userId = null;
			if (authHeader != null && authHeader.startsWith("Bearer ")) {
				String token = authHeader.substring(7);
				userId = authService.getUserFromToken(token).getId();
			}

			List<Products> allProducts = productsRepository.findByIsActiveTrue();
			List<Map<String, Object>> forecasts = new ArrayList<>();

			for (Products product : allProducts) {
				try {
					ForecastResponse forecast = forecastService.getForecast(product.getId(), region, userId);

					Map<String, Object> item = new LinkedHashMap<>();
					item.put("productId", forecast.getProductId());
					item.put("productName", product.getName());
					item.put("category", product.getCategory());
					item.put("currentPrice", forecast.getCurrentPrice());
					item.put("predictedPrice", forecast.getPredictedPrice());
					item.put("percentChange", forecast.getPercentChange());
					item.put("recommendation", forecast.getRecommendation());
					item.put("confidenceScore", forecast.getConfidenceScore());
					item.put("storePrices", forecast.getStorePrices());
					forecasts.add(item);
				} catch (Exception e) {
					// Skip products that fail to forecast
				}
			}

			Map<String, Object> response = new LinkedHashMap<>();
			response.put("region", region);
			response.put("totalProducts", forecasts.size());
			response.put("forecasts", forecasts);
			response.put("timestamp", java.time.LocalDateTime.now());

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}

	@GetMapping("/{productId}")
	public ResponseEntity<?> getForecast(
		@PathVariable UUID productId,
		@RequestParam(value = "region", defaultValue = "national") String region,
		@RequestHeader(value = "Authorization", required = false) String authHeader) {

		try {
			UUID userId = null;
			if (authHeader != null && authHeader.startsWith("Bearer ")) {
				String token = authHeader.substring(7);
				userId = authService.getUserFromToken(token).getId();
			}

			ForecastResponse forecast = forecastService.getForecast(productId, region, userId);

			Map<String, Object> response = new HashMap<>();
			response.put("productId", forecast.getProductId());
			response.put("regionCode", forecast.getRegionCode());
			response.put("currentPrice", forecast.getCurrentPrice());
			response.put("predictedPrice", forecast.getPredictedPrice());
			response.put("percentChange", forecast.getPercentChange());
			response.put("recommendation", forecast.getRecommendation());
			response.put("confidenceScore", forecast.getConfidenceScore());
			response.put("storePrices", forecast.getStorePrices());
			response.put("generatedAt", forecast.getGeneratedAt());

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}

	@GetMapping("/{productId}/history")
	public ResponseEntity<?> getForecastHistory(
		@PathVariable UUID productId,
		@RequestParam(value = "region", defaultValue = "national") String region,
		@RequestHeader(value = "Authorization", required = false) String authHeader) {

		try {
			if (authHeader == null || !authHeader.startsWith("Bearer ")) {
				return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
			}

			String token = authHeader.substring(7);
			authService.getUserFromToken(token);

			return ResponseEntity.ok(Map.of("forecasts", new Object[]{}));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}

	@GetMapping("/{productId}/comparison")
	public ResponseEntity<?> getComparison(
		@PathVariable UUID productId,
		@RequestParam(value = "region", defaultValue = "national") String region,
		@RequestHeader(value = "Authorization", required = false) String authHeader) {

		try {
			UUID userId = null;
			if (authHeader != null && authHeader.startsWith("Bearer ")) {
				String token = authHeader.substring(7);
				userId = authService.getUserFromToken(token).getId();
			}

			ForecastResponse forecast = forecastService.getForecast(productId, region, userId);
			Products product = productsRepository.findById(productId).orElse(null);

			Map<String, Object> comparison = new LinkedHashMap<>();
			comparison.put("productId", productId);
			comparison.put("productName", product != null ? product.getName() : "Unknown");
			comparison.put("regionCode", region);
			comparison.put("forecastPrice", forecast.getPredictedPrice());
			comparison.put("confidence", forecast.getConfidenceScore() + "%");
			comparison.put("recommendation", forecast.getRecommendation());

			// Calculate store comparison
			Map<String, Object> stores = new LinkedHashMap<>();
			BigDecimal minPrice = null;
			BigDecimal maxPrice = null;
			BigDecimal avgPrice = BigDecimal.ZERO;
			int count = 0;

			for (Map.Entry<String, BigDecimal> entry : forecast.getStorePrices().entrySet()) {
				BigDecimal price = entry.getValue();
				if (minPrice == null || price.compareTo(minPrice) < 0) minPrice = price;
				if (maxPrice == null || price.compareTo(maxPrice) > 0) maxPrice = price;
				avgPrice = avgPrice.add(price);
				count++;
			}

			if (count > 0) {
				avgPrice = avgPrice.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
			}

			for (Map.Entry<String, BigDecimal> entry : forecast.getStorePrices().entrySet()) {
				BigDecimal price = entry.getValue();
				BigDecimal savings = avgPrice.subtract(price);
				BigDecimal savingsPercent = savings.divide(avgPrice, 4, RoundingMode.HALF_UP)
					.multiply(BigDecimal.valueOf(100));

				Map<String, Object> storeInfo = new LinkedHashMap<>();
				storeInfo.put("price", price);
				storeInfo.put("savings", savings.setScale(2, RoundingMode.HALF_UP));
				storeInfo.put("savingsPercent", savingsPercent.setScale(2, RoundingMode.HALF_UP) + "%");
				storeInfo.put("isCheapest", price.equals(minPrice));
				storeInfo.put("isMostExpensive", price.equals(maxPrice));
				stores.put(entry.getKey(), storeInfo);
			}

			comparison.put("stores", stores);
			comparison.put("averagePrice", avgPrice);
			comparison.put("cheapest", minPrice);
			comparison.put("mostExpensive", maxPrice);
			comparison.put("savings", maxPrice.subtract(minPrice).setScale(2, RoundingMode.HALF_UP));

			return ResponseEntity.ok(comparison);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}
}
