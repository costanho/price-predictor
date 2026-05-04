package com.pricemonitor.service;

import com.pricemonitor.dto.ForecastResponse;
import com.pricemonitor.model.PriceForecasts;
import com.pricemonitor.repository.PriceContractsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ForecastService {

	private final PriceContractsRepository forecastRepository;
	private final RestTemplate restTemplate;

	@Value("${model.server.url}")
	private String modelServerUrl;

	@Value("${model.server.api.key}")
	private String apiKey;

	public ForecastResponse getForecast(UUID productId, String region) {
		LocalDateTime now = LocalDateTime.now();

		Optional<PriceForecasts> cached = forecastRepository
			.findByProductIdAndRegionCodeAndIsActiveTrueAndExpiresAtGreaterThan(productId, region, now);

		if (cached.isPresent()) {
			return mapToResponse(cached.get());
		}

		try {
			String url = modelServerUrl + "/predict/" + productId + "?region=" + region;
			HttpHeaders headers = new HttpHeaders();
			headers.set("X-API-Key", apiKey);

			HttpEntity<String> entity = new HttpEntity<>(headers);
			ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

			if (response.getBody() == null) {
				throw new RuntimeException("Empty response from model server");
			}

			Map<String, Object> data = response.getBody();

			PriceForecasts forecast = new PriceForecasts();
			forecast.setProductId(productId);
			forecast.setRegionCode(region);
			forecast.setForecastMonth(LocalDate.now());
			forecast.setPredictedPrice(new BigDecimal(data.get("predicted_price").toString()));
			forecast.setCurrentPrice(new BigDecimal(data.get("current_price").toString()));
			forecast.setPercentChange(new BigDecimal(data.get("percent_change").toString()));
			forecast.setRecommendation((String) data.get("recommendation"));
			forecast.setConfidenceScore(((Number) data.get("confidence_score")).intValue());
			forecast.setGeneratedAt(LocalDateTime.now());
			forecast.setExpiresAt(LocalDateTime.now().plusDays(7));
			forecast.setIsActive(true);

			forecastRepository.save(forecast);

			return mapToResponse(forecast);
		} catch (Exception e) {
			throw new RuntimeException("Failed to get forecast from model server: " + e.getMessage(), e);
		}
	}

	public ForecastResponse getForecastForUser(UUID productId, String region, UUID userId) {
		return getForecast(productId, region);
	}

	private ForecastResponse mapToResponse(PriceForecasts forecast) {
		return new ForecastResponse(
			forecast.getId(),
			forecast.getProductId(),
			forecast.getForecastMonth().atStartOfDay(),
			forecast.getPredictedPrice(),
			BigDecimal.valueOf(forecast.getConfidenceScore()),
			forecast.getGeneratedAt()
		);
	}
}
