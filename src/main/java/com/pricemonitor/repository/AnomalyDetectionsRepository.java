package com.pricemonitor.repository;

import com.pricemonitor.model.AnomalyDetections;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface AnomalyDetectionsRepository extends JpaRepository<AnomalyDetections, UUID> {

	List<AnomalyDetections> findByProductId(UUID productId);

	List<AnomalyDetections> findByRegionCode(String regionCode);

	List<AnomalyDetections> findByIsAnomalousTrueOrderByReconstructionErrorDesc();

	@Query("SELECT a FROM AnomalyDetections a WHERE a.detectionDate BETWEEN :startDate AND :endDate ORDER BY a.detectionDate DESC")
	List<AnomalyDetections> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

	@Query("SELECT a FROM AnomalyDetections a WHERE a.productId = :productId AND a.detectionDate BETWEEN :startDate AND :endDate")
	List<AnomalyDetections> findByProductAndDateRange(@Param("productId") UUID productId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

	List<AnomalyDetections> findBySeverity(String severity);
}
