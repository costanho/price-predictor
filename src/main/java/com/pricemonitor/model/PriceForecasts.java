package com.pricemonitor.model;

import lombok.Data;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "price_forecasts")
@Data
public class PriceForecasts {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private UUID id;

	@Column(name = "product_id")
	private UUID productId;

	@Column(name = "region_code")
	private String regionCode;

	@Column(name = "forecast_month", nullable = false)
	private LocalDate forecastMonth;

	@Column(name = "current_price", nullable = false)
	private BigDecimal currentPrice;

	@Column(name = "predicted_price", nullable = false)
	private BigDecimal predictedPrice;

	@Column(name = "percent_change", nullable = false)
	private BigDecimal percentChange;

	@Column(name = "recommendation", nullable = false)
	private String recommendation;

	@Column(name = "confidence_score")
	private Integer confidenceScore;

	@Column(name = "mape_error")
	private BigDecimal mapeError;

	@Column(name = "model_version")
	private String modelVersion;

	@Column(name = "generated_at")
	private LocalDateTime generatedAt;

	@Column(name = "expires_at")
	private LocalDateTime expiresAt;

	@Column(name = "is_active")
	private Boolean isActive;
}
