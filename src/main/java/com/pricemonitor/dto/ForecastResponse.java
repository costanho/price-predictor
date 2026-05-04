package com.pricemonitor.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class ForecastResponse {

	private UUID id;
	private UUID productId;
	private LocalDateTime forecastDate;
	private BigDecimal predictedPrice;
	private BigDecimal confidence;
	private LocalDateTime createdAt;

	public ForecastResponse() {
	}

	public ForecastResponse(UUID id, UUID productId, LocalDateTime forecastDate, BigDecimal predictedPrice,
						   BigDecimal confidence, LocalDateTime createdAt) {
		this.id = id;
		this.productId = productId;
		this.forecastDate = forecastDate;
		this.predictedPrice = predictedPrice;
		this.confidence = confidence;
		this.createdAt = createdAt;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public UUID getProductId() {
		return productId;
	}

	public void setProductId(UUID productId) {
		this.productId = productId;
	}

	public LocalDateTime getForecastDate() {
		return forecastDate;
	}

	public void setForecastDate(LocalDateTime forecastDate) {
		this.forecastDate = forecastDate;
	}

	public BigDecimal getPredictedPrice() {
		return predictedPrice;
	}

	public void setPredictedPrice(BigDecimal predictedPrice) {
		this.predictedPrice = predictedPrice;
	}

	public BigDecimal getConfidence() {
		return confidence;
	}

	public void setConfidence(BigDecimal confidence) {
		this.confidence = confidence;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
}
