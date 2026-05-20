package com.pricemonitor.model;

import lombok.Data;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "price_history")
@Data
public class PriceHistories {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private UUID id;

	@Column(name = "product_id")
	private UUID productId;

	@Column(name = "store_id")
	private String storeId;

	@Column(name = "region_code")
	private String regionCode;

	@Column(name = "price", nullable = false)
	private BigDecimal price;

	@Column(name = "price_date", nullable = false)
	private LocalDate priceDate;

	@Column(name = "data_source", nullable = false)
	private String dataSource;

	@Column(name = "created_at")
	private LocalDateTime createdAt;
}
