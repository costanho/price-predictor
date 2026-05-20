package com.pricemonitor.service;

import com.pricemonitor.model.Alerts;
import com.pricemonitor.repository.AlertsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertCleanupService {

	private final AlertsRepository alertsRepository;

	// ── Runs every night at midnight ──────────────────────────────────────────
	@Scheduled(cron = "0 0 0 * * ?")
	@Transactional
	public void runNightlyCleanup() {
		System.out.println("=== Alert cleanup starting: " + LocalDateTime.now() + " ===");

		LocalDateTime now = LocalDateTime.now();

		// 1 — Soft delete expired alerts
		List<Alerts> expired = alertsRepository.findExpired(now);
		for (Alerts alert : expired) {
			alertsRepository.softDelete(alert.getId(), now);
		}
		System.out.println("Soft deleted " + expired.size() + " expired alerts");

		// 2 — Hard delete read alerts older than 90 days
		// These are safe to permanently remove — user already saw them
		LocalDateTime cutoff = now.minusDays(90);
		int hardDeleted = alertsRepository.deleteOldReadAlerts(cutoff);
		System.out.println("Hard deleted " + hardDeleted + " old read alerts");

		// 3 — Hard delete soft-deleted alerts older than 7 days
		// (grace period before permanent removal)
		LocalDateTime softDeleteCutoff = now.minusDays(7);
		int purged = alertsRepository.hardDeleteOldDeleted(softDeleteCutoff);
		System.out.println("Purged " + purged + " old soft-deleted alerts");

		System.out.println("=== Alert cleanup complete ===");
	}

	// ── Alert expiry rules reference ──────────────────────────────────────────
	// price_drop alert       → expires in 3 days  (set in AlertGenerationService)
	// forecast_update alert  → expires in 7 days
	// anomaly alert          → expires in 2 days (high severity)
	// target_met alert       → expires in 1 day
	// deal_dna alert         → expires in 5 days
	// volatility_warning     → expires in 3 days
	// Read alerts            → hard deleted after 90 days regardless of type
	// Soft-deleted alerts    → hard deleted after 7 days (grace period)
}
