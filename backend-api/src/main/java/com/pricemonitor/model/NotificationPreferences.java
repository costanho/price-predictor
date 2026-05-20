package com.pricemonitor.model;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notification_preferences")
@Data
public class NotificationPreferences {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private UUID id;

	@Column(name = "user_id")
	private UUID userId;

	@Column(name = "price_drop")
	private Boolean priceDrop;

	@Column(name = "anomaly_warning")
	private Boolean anomalyWarning;

	@Column(name = "deal_dna")
	private Boolean dealDna;

	@Column(name = "forecast_update")
	private Boolean forecastUpdate;

	@Column(name = "weekly_email")
	private Boolean weeklyEmail;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
}
