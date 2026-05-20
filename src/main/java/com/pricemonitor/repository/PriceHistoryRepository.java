package com.pricemonitor.repository;

import com.pricemonitor.model.PriceHistories;
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
public interface PriceHistoryRepository extends JpaRepository<PriceHistories, UUID> {

	boolean existsByProductIdAndStoreIdAndPriceDateAndDataSource(
		UUID productId, String storeId, LocalDate priceDate, String dataSource);

	List<PriceHistories> findByProductIdOrderByPriceDateDesc(UUID productId);

	List<PriceHistories> findByProductIdAndStoreIdOrderByPriceDateDesc(UUID productId, String storeId);

	// Get price history for specific stores (for store-aware predictions)
	@Query("SELECT ph FROM PriceHistories ph " +
		   "WHERE ph.productId = :productId " +
		   "AND ph.regionCode = :region " +
		   "AND ph.storeId IN :storeIds " +
		   "ORDER BY ph.priceDate DESC")
	List<PriceHistories> findByProductAndRegionAndStores(
		@Param("productId") UUID productId,
		@Param("region") String region,
		@Param("storeIds") List<String> storeIds
	);

	// Get price history without store filter (fallback to BLS)
	@Query("SELECT ph FROM PriceHistories ph " +
		   "WHERE ph.productId = :productId " +
		   "AND ph.regionCode = :region " +
		   "ORDER BY ph.priceDate DESC")
	List<PriceHistories> findRecentByProductAndRegion(
		@Param("productId") UUID productId,
		@Param("region") String region
	);

	// Latest price for a product at a specific store
	@Query("SELECT ph FROM PriceHistories ph " +
		   "WHERE ph.productId = :productId " +
		   "AND ph.storeId = :storeId " +
		   "ORDER BY ph.priceDate DESC")
	Optional<PriceHistories> findLatestByProductAndStore(
		@Param("productId") UUID productId,
		@Param("storeId") String storeId
	);

	// Recent prices for price drop calculation (last N days)
	@Query(value = "SELECT store_id, price FROM price_history " +
		   "WHERE product_id = :productId AND store_id = :storeId " +
		   "AND price_date >= CURRENT_DATE - :days " +
		   "ORDER BY price_date DESC LIMIT 1",
		   nativeQuery = true)
	List<Object[]> findPricesLastNDays(
		@Param("productId") UUID productId,
		@Param("storeId") String storeId,
		@Param("days") int days
	);
}
