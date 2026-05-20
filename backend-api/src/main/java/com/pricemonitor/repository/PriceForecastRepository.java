package com.pricemonitor.repository;

import com.pricemonitor.model.PriceForecasts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PriceForecastRepository extends JpaRepository<PriceForecasts, UUID> {

	@Modifying
	@Query("UPDATE PriceForecasts pf SET pf.isActive = false " +
		   "WHERE pf.productId = :productId")
	void deactivateByProductId(@Param("productId") UUID productId);

	List<PriceForecasts> findByProductIdAndIsActiveTrue(UUID productId);

	List<PriceForecasts> findByProductIdOrderByGeneratedAtDesc(UUID productId);

	Optional<PriceForecasts> findByProductIdAndRegionCodeAndIsActiveTrueAndExpiresAtAfter(
		UUID productId, String regionCode, LocalDateTime expiresAt);
}
