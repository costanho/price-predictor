package com.pricemonitor.model;

import lombok.Data;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "macro_indicators")
@Data
public class MacroIndicators {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private UUID id;

	@Column(name = "indicator_code", nullable = false)
	private String indicatorCode;

	@Column(name = "indicator_name", nullable = false)
	private String indicatorName;

	@Column(name = "value", nullable = false)
	private BigDecimal value;

	@Column(name = "indicator_date", nullable = false)
	private LocalDate indicatorDate;

	@Column(name = "frequency", nullable = false)
	private String frequency;

	@Column(name = "created_at")
	private LocalDateTime createdAt;
}
