package com.pricemonitor.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ForecastResponse {

	private UUID id;
	private UUID productId;
	private String regionCode;
	private LocalDateTime forecastDate;
	private BigDecimal currentPrice;
	private BigDecimal predictedPrice;
	private BigDecimal percentChange;
	private String recommendation;
	private Integer confidenceScore;
	private LocalDateTime generatedAt;
	private LocalDateTime createdAt;
	private Map<String, BigDecimal> storePrices = new HashMap<>();

	public ForecastResponse() {
	}

	public ForecastResponse(UUID id, UUID productId, LocalDateTime forecastDate, BigDecimal predictedPrice,
						   BigDecimal confidence, LocalDateTime createdAt) {
		this.id = id;
		this.productId = productId;
		this.forecastDate = forecastDate;
		this.predictedPrice = predictedPrice;
		this.confidenceScore = confidence.intValue();
		this.createdAt = createdAt;
	}

	public UUID getId() { return id; }
	public void setId(UUID id) { this.id = id; }

	public UUID getProductId() { return productId; }
	public void setProductId(UUID productId) { this.productId = productId; }

	public String getRegionCode() { return regionCode; }
	public void setRegionCode(String regionCode) { this.regionCode = regionCode; }

	public LocalDateTime getForecastDate() { return forecastDate; }
	public void setForecastDate(LocalDateTime forecastDate) { this.forecastDate = forecastDate; }

	public BigDecimal getCurrentPrice() { return currentPrice; }
	public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }

	public BigDecimal getPredictedPrice() { return predictedPrice; }
	public void setPredictedPrice(BigDecimal predictedPrice) { this.predictedPrice = predictedPrice; }

	public BigDecimal getPercentChange() { return percentChange; }
	public void setPercentChange(BigDecimal percentChange) { this.percentChange = percentChange; }

	public String getRecommendation() { return recommendation; }
	public void setRecommendation(String recommendation) { this.recommendation = recommendation; }

	public Integer getConfidenceScore() { return confidenceScore; }
	public void setConfidenceScore(Integer confidenceScore) { this.confidenceScore = confidenceScore; }

	public LocalDateTime getGeneratedAt() { return generatedAt; }
	public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }

	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

	public Map<String, BigDecimal> getStorePrices() { return storePrices; }
	public void setStorePrices(Map<String, BigDecimal> storePrices) { this.storePrices = storePrices; }
}
