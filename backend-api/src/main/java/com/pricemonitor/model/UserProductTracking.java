package com.pricemonitor.model;

import lombok.Data;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_product_tracking")
@Data
public class UserProductTracking {

	@EmbeddedId
	private UserProductTrackingKey id;

	@Column(name = "target_price")
	private BigDecimal targetPrice;

	@Column(name = "added_at")
	private LocalDateTime addedAt;

	@Embeddable
	@Data
	public static class UserProductTrackingKey {
		@Column(name = "user_id")
		private UUID userId;

		@Column(name = "product_id")
		private UUID productId;
	}
}
