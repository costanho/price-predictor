package com.pricemonitor.repository;

import com.pricemonitor.model.Alerts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AlertsRepository extends JpaRepository<Alerts, UUID> {

	// All active (not deleted, not expired) alerts for a user
	@Query("SELECT a FROM Alerts a WHERE a.userId = :userId " +
		   "AND a.deletedAt IS NULL " +
		   "AND (a.expiresAt IS NULL OR a.expiresAt > :now) " +
		   "ORDER BY a.createdAt DESC")
	List<Alerts> findActiveByUserId(
		@Param("userId") UUID userId,
		@Param("now") LocalDateTime now
	);

	// Unread only
	@Query("SELECT a FROM Alerts a WHERE a.userId = :userId " +
		   "AND a.isRead = false " +
		   "AND a.deletedAt IS NULL " +
		   "AND (a.expiresAt IS NULL OR a.expiresAt > :now) " +
		   "ORDER BY a.createdAt DESC")
	List<Alerts> findUnreadByUserId(
		@Param("userId") UUID userId,
		@Param("now") LocalDateTime now
	);

	// Count unread (for badge on nav tab)
	@Query("SELECT COUNT(a) FROM Alerts a WHERE a.userId = :userId " +
		   "AND a.isRead = false " +
		   "AND a.deletedAt IS NULL " +
		   "AND (a.expiresAt IS NULL OR a.expiresAt > :now)")
	long countUnreadByUserId(
		@Param("userId") UUID userId,
		@Param("now") LocalDateTime now
	);

	// Check if alert already exists today (dedup check)
	@Query("SELECT a FROM Alerts a WHERE a.userId = :userId " +
		   "AND a.productId = :productId " +
		   "AND a.alertType = :alertType " +
		   "AND a.deletedAt IS NULL " +
		   "AND a.createdAt >= :startOfDay")
	Optional<Alerts> findTodaysAlert(
		@Param("userId") UUID userId,
		@Param("productId") UUID productId,
		@Param("alertType") String alertType,
		@Param("startOfDay") LocalDateTime startOfDay
	);

	// Cleanup — find all expired alerts (for scheduled cleanup job)
	@Query("SELECT a FROM Alerts a WHERE a.expiresAt IS NOT NULL " +
		   "AND a.expiresAt < :now AND a.deletedAt IS NULL")
	List<Alerts> findExpired(@Param("now") LocalDateTime now);

	// Hard delete old read alerts (older than 90 days)
	@Modifying
	@Query("DELETE FROM Alerts a WHERE a.isRead = true " +
		   "AND a.createdAt < :cutoff")
	int deleteOldReadAlerts(@Param("cutoff") LocalDateTime cutoff);

	// Soft delete one alert
	@Modifying
	@Query("UPDATE Alerts a SET a.deletedAt = :now WHERE a.id = :id")
	void softDelete(@Param("id") UUID id, @Param("now") LocalDateTime now);

	// Soft delete all expired alerts
	@Modifying
	@Query("UPDATE Alerts a SET a.deletedAt = :now " +
		   "WHERE a.expiresAt IS NOT NULL AND a.expiresAt < :now AND a.deletedAt IS NULL")
	void softDeleteExpired(@Param("now") LocalDateTime now);

	// Hard delete old soft-deleted alerts (older than cutoff)
	@Modifying
	@Query("DELETE FROM Alerts a WHERE a.deletedAt IS NOT NULL AND a.deletedAt < :cutoff")
	int hardDeleteOldDeleted(@Param("cutoff") LocalDateTime cutoff);
}
