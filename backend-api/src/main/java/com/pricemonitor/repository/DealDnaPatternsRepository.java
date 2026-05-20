package com.pricemonitor.repository;

import com.pricemonitor.model.DealDnaPatterns;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface DealDnaPatternsRepository extends JpaRepository<DealDnaPatterns, UUID> {

	List<DealDnaPatterns> findByProductId(UUID productId);

	List<DealDnaPatterns> findByStoreId(String storeId);

	List<DealDnaPatterns> findByIsActiveTrueOrderByConfidenceScoreDesc();

	List<DealDnaPatterns> findByProductIdAndStoreId(UUID productId, String storeId);

	@Query("SELECT d FROM DealDnaPatterns d WHERE d.nextPredictedSale <= :targetDate AND d.isActive = true ORDER BY d.nextPredictedSale ASC")
	List<DealDnaPatterns> findUpcomingSalesBeforeDate(@Param("targetDate") LocalDate targetDate);

	List<DealDnaPatterns> findByConfidenceScoreGreaterThanEqual(Integer minScore);
}
