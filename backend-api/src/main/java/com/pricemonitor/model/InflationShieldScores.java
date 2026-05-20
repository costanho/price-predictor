package com.pricemonitor.model;

import lombok.Data;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "inflation_shield_scores")
@Data
public class InflationShieldScores {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private UUID id;

	@Column(name = "product_id")
	private UUID productId;

	@Column(name = "region_code")
	private String regionCode;

	@Column(name = "score", nullable = false)
	private Integer score;

	@Column(name = "volatility_12m")
	private BigDecimal volatility12m;

	@Column(name = "price_change_12m")
	private BigDecimal priceChange12m;

	@Column(name = "max_swing_12m")
	private BigDecimal maxSwing12m;

	@Column(name = "calculated_at")
	private LocalDateTime calculatedAt;
}
