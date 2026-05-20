WIRE ALERT GENERATION INTO EXISTING SERVICES
7A — Update PriceSyncService.java
Add AlertGenerationService injection and call it after each sync:
// Add this field to PriceSyncService:
@Autowired private AlertGenerationService alertService;

// In syncKrogerPrice() — add after priceHistoryRepo.save(ph):
alertService.checkPriceDrop(product.getId(), "kroger", price);
alertService.checkTargetPriceReached(product.getId(), "kroger", price);

// In syncSpoonacularPrices() — add after each priceHistoryRepo.save(ph):
alertService.checkPriceDrop(product.getId(), result.storeId, result.price);
alertService.checkTargetPriceReached(product.getId(), result.storeId, result.price);

Update ForecastService.java
Add alert generation after saving a new forecast:


java
// Add this field to ForecastService:
@Autowired private AlertGenerationService alertService;

// In getForecast() — add after forecastRepo.save(forecast):
alertService.checkForecastAlert(
productId,
region,
forecast.getCurrentPrice(),
forecast.getPredictedPrice(),
forecast.getRecommendation()
);


Add repository methods needed by AlertGenerationService
Add to UserProductTrackingRepository.java:


java
// Find all user IDs who track a specific product
@Query("SELECT upt.userId FROM UserProductTracking upt " +
"WHERE upt.productId = :productId")
List<UUID> findUserIdsByProductId(@Param("productId") UUID productId);

// Find users who have set a target price for this product
@Query("SELECT upt.userId, upt.targetPrice FROM UserProductTracking upt " +
"WHERE upt.productId = :productId AND upt.targetPrice IS NOT NULL")
List<Object[]> findUsersWithTargetPrice(@Param("productId") UUID productId);


Add to PriceHistoryRepository.java:


java
// Recent prices for price drop calculation
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


