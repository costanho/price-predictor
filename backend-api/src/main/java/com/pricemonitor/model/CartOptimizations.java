package com.pricemonitor.model;

import lombok.Data;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cart_optimizations")
@Data
public class CartOptimizations {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private UUID id;

	@Column(name = "user_id")
	private UUID userId;

	@Column(name = "total_items", nullable = false)
	private Integer totalItems;

	@Column(name = "cheapest_single_total")
	private BigDecimal cheapestSingleTotal;

	@Column(name = "cheapest_single_store")
	private String cheapestSingleStore;

	@Column(name = "optimized_total")
	private BigDecimal optimizedTotal;

	@Column(name = "weekly_savings")
	private BigDecimal weeklySavings;

	@Column(name = "annual_projection")
	private BigDecimal annualProjection;

	@Column(name = "store_breakdown", nullable = false, columnDefinition = "jsonb")
	private String storeBreakdown;

	@Column(name = "wait_one_month_total")
	private BigDecimal waitOneMonthTotal;

	@Column(name = "wait_one_month_savings")
	private BigDecimal waitOneMonthSavings;

	@Column(name = "calculated_at")
	private LocalDateTime calculatedAt;

	@Column(name = "expires_at")
	private LocalDateTime expiresAt;
}
