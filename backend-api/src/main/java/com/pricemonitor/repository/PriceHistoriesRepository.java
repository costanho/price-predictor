package com.pricemonitor.repository;

import com.pricemonitor.model.PriceHistories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface PriceHistoriesRepository extends JpaRepository<PriceHistories, UUID> {

	List<PriceHistories> findByProductId(UUID productId);

	List<PriceHistories> findByProductIdOrderByPriceDateDesc(UUID productId);

	List<PriceHistories> findByRegionCode(String regionCode);

	@Query("SELECT p FROM PriceHistories p WHERE p.productId = :productId AND p.priceDate BETWEEN :startDate AND :endDate ORDER BY p.priceDate DESC")
	List<PriceHistories> findByProductAndDateRange(@Param("productId") UUID productId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

	@Query("SELECT p FROM PriceHistories p WHERE p.priceDate BETWEEN :startDate AND :endDate ORDER BY p.priceDate DESC")
	List<PriceHistories> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

	List<PriceHistories> findByDataSource(String dataSource);
}
