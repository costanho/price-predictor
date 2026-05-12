package com.pricemonitor.model;

import lombok.Data;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_cart_items")
@Data
public class UserCartItems {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private UUID id;

	@Column(name = "user_id")
	private UUID userId;

	@Column(name = "product_id")
	private UUID productId;

	@Column(name = "quantity")
	private BigDecimal quantity;

	@Column(name = "unit_override")
	private String unitOverride;

	@Column(name = "notes")
	private String notes;

	@Column(name = "added_at")
	private LocalDateTime addedAt;
}
