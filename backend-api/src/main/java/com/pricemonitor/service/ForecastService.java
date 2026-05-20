package com.pricemonitor.service;

import com.pricemonitor.dto.ForecastResponse;
import com.pricemonitor.model.PriceForecasts;
import com.pricemonitor.model.PriceHistories;
import com.pricemonitor.repository.PriceForecastRepository;
import com.pricemonitor.repository.PriceHistoryRepository;
import com.pricemonitor.repository.UserTrackedStoresRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ForecastService {

	private final PriceForecastRepository forecastRepo;
	private final PriceHistoryRepository priceHistoryRepo;
	private final UserTrackedStoresRepository userTrackedStoreRepository;
	private final StoreFactorService storeFactorService;
	private final AlertGenerationService alertGenerationService;
	private final RestTemplate restTemplate;

	@Value("${model.server.url}")
	private String modelServerUrl;

	@Value("${model.server.api.key}")
	private String apiKey;

	public ForecastResponse getForecast(UUID productId, String region, UUID userId) {

		// 1 — Get user's tracked stores (or use all stores if not logged in)
		List<String> userStoreIds = new java.util.ArrayList<>();
		if (userId != null) {
			userStoreIds = userTrackedStoreRepository.findStoreIdsByUserId(userId);
		}

		// If user has no stores selected or not logged in, use all stores
		String storesParam = userStoreIds.isEmpty()
			? "all"
			: String.join(",", userStoreIds);

		// 2 — Check cache (cache is now store-aware)
		Optional<PriceForecasts> cached = forecastRepo
			.findByProductIdAndRegionCodeAndIsActiveTrueAndExpiresAtAfter(
				productId, region, LocalDateTime.now()
			);

		if (cached.isPresent()) {
			// If no user, use all stores
			if (userStoreIds.isEmpty()) {
				userStoreIds = Arrays.asList("walmart", "kroger", "aldi", "target", "whole_foods", "trader_joes");
			}
			return mapToResponseWithStorePrices(cached.get(), userStoreIds, productId);
		}

		// 3 — Cache miss — call Python server with stores parameter
		String url = modelServerUrl + "/predict/" + productId
			+ "?region=" + region
			+ "&stores=" + storesParam;

		HttpHeaders headers = new HttpHeaders();
		headers.set("X-API-Key", apiKey);
		HttpEntity<String> entity = new HttpEntity<>(headers);

		ResponseEntity<Map> response = restTemplate.exchange(
			url, HttpMethod.GET, entity, Map.class
		);
		Map<String, Object> data = response.getBody();

		// 4 — Save forecast
		PriceForecasts forecast = new PriceForecasts();
		forecast.setProductId(productId);
		forecast.setRegionCode(region);
		forecast.setPredictedPrice(toBigDecimal(data.get("predicted_price")));
		forecast.setCurrentPrice(toBigDecimal(data.get("current_price")));
		forecast.setPercentChange(toBigDecimal(data.get("percent_change")));
		forecast.setRecommendation((String) data.get("recommendation"));
		forecast.setConfidenceScore(((Number) data.get("confidence_score")).intValue());
		forecast.setForecastMonth(YearMonth.now().atDay(1));
		forecast.setGeneratedAt(LocalDateTime.now());
		forecast.setExpiresAt(LocalDateTime.now().plusDays(7));
		forecast.setIsActive(true);
		forecastRepo.save(forecast);

		// Check for significant price change alerts
		alertGenerationService.checkForecastAlert(
			productId,
			region,
			forecast.getCurrentPrice(),
			forecast.getPredictedPrice(),
			forecast.getRecommendation()
		);

		// 5 — Get current real prices per store for Compare screen
		// If no user, show all stores
		if (userStoreIds.isEmpty()) {
			userStoreIds = java.util.Arrays.asList("walmart", "kroger", "aldi", "target", "whole_foods", "trader_joes");
		}
		return mapToResponseWithStorePrices(forecast, userStoreIds, productId);
	}

	// Helper: build response with per-store prices
	private ForecastResponse mapToResponseWithStorePrices(
			PriceForecasts forecast, List<String> storeIds, UUID productId) {

		ForecastResponse res = new ForecastResponse();
		res.setId(forecast.getId());
		res.setProductId(forecast.getProductId());
		res.setRegionCode(forecast.getRegionCode());
		res.setCurrentPrice(forecast.getCurrentPrice());
		res.setPredictedPrice(forecast.getPredictedPrice());
		res.setPercentChange(forecast.getPercentChange());
		res.setRecommendation(forecast.getRecommendation());
		res.setConfidenceScore(forecast.getConfidenceScore());
		res.setGeneratedAt(forecast.getGeneratedAt());

		// Add per-store current prices
		Map<String, BigDecimal> storePrices = new HashMap<>();
		for (String storeId : storeIds) {
			Optional<PriceHistories> latest = priceHistoryRepo
				.findLatestByProductAndStore(productId, storeId);
			if (latest.isPresent()) {
				storePrices.put(storeId, latest.get().getPrice());
			} else {
				// Fallback: apply differential factor to BLS price
				BigDecimal factor = storeFactorService.getFactor(storeId);
				storePrices.put(storeId, forecast.getCurrentPrice().multiply(factor));
			}
		}
		res.setStorePrices(storePrices);

		return res;
	}

	private BigDecimal toBigDecimal(Object value) {
		if (value == null) return BigDecimal.ZERO;
		return new BigDecimal(value.toString());
	}
}
