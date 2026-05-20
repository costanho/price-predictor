package com.pricemonitor.model;

import lombok.Data;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "anomaly_detections")
@Data
public class AnomalyDetections {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private UUID id;

	@Column(name = "product_id")
	private UUID productId;

	@Column(name = "region_code")
	private String regionCode;

	@Column(name = "detection_date", nullable = false)
	private LocalDate detectionDate;

	@Column(name = "reconstruction_error", nullable = false)
	private BigDecimal reconstructionError;

	@Column(name = "threshold_used", nullable = false)
	private BigDecimal thresholdUsed;

	@Column(name = "is_anomalous", nullable = false)
	private Boolean isAnomalous;

	@Column(name = "severity")
	private String severity;

	@Column(name = "notes")
	private String notes;

	@Column(name = "created_at")
	private LocalDateTime createdAt;
}
