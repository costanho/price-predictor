package com.pricemonitor.model;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_tracked_stores")
@Data
public class UserTrackedStores {

	@EmbeddedId
	private UserTrackedStoresKey id;

	@Column(name = "added_at")
	private LocalDateTime addedAt;

	@Embeddable
	@Data
	public static class UserTrackedStoresKey {
		@Column(name = "user_id")
		private UUID userId;

		@Column(name = "store_id")
		private String storeId;
	}
}
