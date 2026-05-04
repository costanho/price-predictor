package com.pricemonitor.repository;

import com.pricemonitor.model.Alerts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AlertsRepository extends JpaRepository<Alerts, UUID> {

	List<Alerts> findByUserId(UUID userId);

	List<Alerts> findByUserIdAndIsReadFalse(UUID userId);

	List<Alerts> findByProductId(UUID productId);

	List<Alerts> findByAlertType(String alertType);

	@Query("SELECT a FROM Alerts a WHERE a.userId = :userId ORDER BY a.createdAt DESC")
	List<Alerts> findRecentAlertsForUser(@Param("userId") UUID userId);
}
