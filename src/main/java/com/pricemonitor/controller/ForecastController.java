package com.pricemonitor.controller;

import com.pricemonitor.dto.ForecastResponse;
import com.pricemonitor.service.AuthService;
import com.pricemonitor.service.ForecastService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/forecasts")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:19006"})
@RequiredArgsConstructor
public class ForecastController {

	private final ForecastService forecastService;
	private final AuthService authService;

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

			ForecastResponse forecast = forecastService.getForecastForUser(productId, region, userId);

			Map<String, Object> response = new HashMap<>();
			response.put("productId", forecast.getProductId());
			response.put("forecastDate", forecast.getForecastDate());
			response.put("currentPrice", forecast.getPredictedPrice());
			response.put("predictedPrice", forecast.getPredictedPrice());
			response.put("percentChange", forecast.getConfidence());
			response.put("recommendation", "hold");
			response.put("confidenceScore", forecast.getConfidence().intValue());
			response.put("generatedAt", forecast.getCreatedAt());

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
}
