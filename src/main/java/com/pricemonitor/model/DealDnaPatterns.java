package com.pricemonitor.model;

import lombok.Data;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "deal_dna_patterns")
@Data
public class DealDnaPatterns {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private UUID id;

	@Column(name = "product_id")
	private UUID productId;

	@Column(name = "store_id")
	private String storeId;

	@Column(name = "sale_frequency_days")
	private Integer saleFrequencyDays;

	@Column(name = "typical_discount_pct")
	private BigDecimal typicalDiscountPct;

	@Column(name = "last_sale_detected")
	private LocalDate lastSaleDetected;

	@Column(name = "next_predicted_sale")
	private LocalDate nextPredictedSale;

	@Column(name = "confidence_score")
	private Integer confidenceScore;

	@Column(name = "pattern_notes")
	private String patternNotes;

	@Column(name = "is_active")
	private Boolean isActive;

	@Column(name = "last_calculated")
	private LocalDate lastCalculated;
}
