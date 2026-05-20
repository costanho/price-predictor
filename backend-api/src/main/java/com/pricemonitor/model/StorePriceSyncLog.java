package com.pricemonitor.model;

import lombok.Data;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "store_price_sync_log")
@Data
public class StorePriceSyncLog {

	@Id
	private UUID id;

	@Column(name = "store_id")
	private String storeId;

	@Column(name = "product_id")
	private UUID productId;

	@Column(name = "sync_date")
	private LocalDate syncDate;

	@Column(name = "source")
	private String source;

	@Column(name = "status")
	private String status;

	@Column(name = "price_found")
	private BigDecimal priceFound;

	@Column(name = "error_message")
	private String errorMessage;

	@Column(name = "created_at")
	private LocalDateTime createdAt;
}
