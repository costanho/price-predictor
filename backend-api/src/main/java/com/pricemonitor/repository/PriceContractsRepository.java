package com.pricemonitor.repository;

import com.pricemonitor.model.PriceForecasts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PriceContractsRepository extends JpaRepository<PriceForecasts, UUID> {

	List<PriceForecasts> findByProductId(UUID productId);

	List<PriceForecasts> findByProductIdAndIsActiveTrue(UUID productId);

	List<PriceForecasts> findByRegionCode(String regionCode);

	Optional<PriceForecasts> findByProductIdAndRegionCodeAndForecastMonth(UUID productId, String regionCode, LocalDate forecastMonth);

	@Query("SELECT p FROM PriceForecasts p WHERE p.productId = :productId AND p.regionCode = :regionCode AND p.isActive = true AND p.expiresAt > :now")
	Optional<PriceForecasts> findByProductIdAndRegionCodeAndIsActiveTrueAndExpiresAtGreaterThan(
		@Param("productId") UUID productId,
		@Param("regionCode") String regionCode,
		@Param("now") LocalDateTime now
	);

	@Query("SELECT p FROM PriceForecasts p WHERE p.forecastMonth BETWEEN :startDate AND :endDate AND p.isActive = true ORDER BY p.forecastMonth ASC")
	List<PriceForecasts> findForecastsInDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

	List<PriceForecasts> findByRecommendation(String recommendation);

	@Query("SELECT p FROM PriceForecasts p WHERE p.isActive = true AND p.expiresAt > CURRENT_TIMESTAMP ORDER BY p.generatedAt DESC")
	List<PriceForecasts> findActiveForecasts();
}
