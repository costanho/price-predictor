package com.pricemonitor.model;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "alerts")
@Data
public class Alerts {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private UUID id;

	@Column(name = "user_id")
	private UUID userId;

	@Column(name = "product_id")
	private UUID productId;

	@Column(name = "store_id")
	private String storeId;

	@Column(name = "alert_type", nullable = false)
	private String alertType;

	@Column(name = "title", nullable = false)
	private String title;

	@Column(name = "description", nullable = false)
	private String description;

	@Column(name = "action_text")
	private String actionText;

	@Column(name = "is_read")
	private Boolean isRead;

	@Column(name = "created_at")
	private LocalDateTime createdAt;
}
