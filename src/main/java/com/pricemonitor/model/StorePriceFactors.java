package com.pricemonitor.model;

import lombok.Data;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "store_price_factors")
@Data
public class StorePriceFactors {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private UUID id;

	@Column(name = "store_id")
	private String storeId;

	@Column(name = "category", nullable = false)
	private String category;

	@Column(name = "factor", nullable = false)
	private BigDecimal factor;

	@Column(name = "source")
	private String source;

	@Column(name = "last_updated")
	private LocalDate lastUpdated;
}
