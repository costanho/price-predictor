package com.pricemonitor.service;

import com.pricemonitor.model.ProductNameMapping;
import com.pricemonitor.model.StorePriceSyncLog;
import com.pricemonitor.repository.ProductNameMappingRepository;
import com.pricemonitor.repository.StorePriceSyncLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalPriceSyncService {

	private final StorePriceSyncLogRepository syncLogRepository;
	private final ProductNameMappingRepository productNameMappingRepository;
	private final KrogerApiClient krogerApiClient;
	private final SpoonacularApiClient spoonacularApiClient;

	@Scheduled(fixedRate = 3600000) // Every hour
	public void syncPricesFromExternalAPIs() {
		syncKrogerPrices();
		syncSpoonacularPrices();
	}

	private void syncKrogerPrices() {
		String source = "kroger_api";
		String storeId = "store-kroger-001";

		try {
			List<ProductNameMapping> mappedProducts = productNameMappingRepository.findAllMappedBySource(source);

			for (ProductNameMapping mapping : mappedProducts) {
				try {
					BigDecimal price = krogerApiClient.getProductPrice(mapping.getExternalName());

					if (price != null) {
						logSyncAttempt(storeId, mapping.getProductId(), source, price, "success", null);
					} else {
						logSyncAttempt(storeId, mapping.getProductId(), source, null, "failed", "Product not found or price unavailable");
					}
				} catch (Exception e) {
					log.error("Error syncing Kroger product {}: {}", mapping.getExternalName(), e.getMessage());
					logSyncAttempt(storeId, mapping.getProductId(), source, null, "failed", e.getMessage());
				}
			}
		} catch (Exception e) {
			log.error("Kroger price sync failed: {}", e.getMessage());
		}
	}

	private void syncSpoonacularPrices() {
		String source = "spoonacular";

		try {
			List<ProductNameMapping> mappedProducts = productNameMappingRepository.findAllMappedBySource(source);

			for (ProductNameMapping mapping : mappedProducts) {
				try {
					List<SpoonacularApiClient.StorePriceResult> results = spoonacularApiClient.searchProductPrices(mapping.getExternalName());

					if (results != null && !results.isEmpty()) {
						for (SpoonacularApiClient.StorePriceResult result : results) {
							String storeId = "store-" + result.storeId + "-001";
							logSyncAttempt(storeId, mapping.getProductId(), source, result.price, "success", null);
						}
					} else {
						logSyncAttempt("store-walmart-001", mapping.getProductId(), source, null, "failed", "No prices found");
					}
				} catch (Exception e) {
					log.error("Error syncing Spoonacular product {}: {}", mapping.getExternalName(), e.getMessage());
					logSyncAttempt("store-walmart-001", mapping.getProductId(), source, null, "failed", e.getMessage());
				}
			}
		} catch (Exception e) {
			log.error("Spoonacular price sync failed: {}", e.getMessage());
		}
	}

	private void logSyncAttempt(String storeId, UUID productId, String source,
								BigDecimal priceFound, String status, String errorMessage) {

		StorePriceSyncLog log = new StorePriceSyncLog();
		log.setStoreId(storeId);
		log.setProductId(productId);
		log.setSource(source);
		log.setStatus(status);
		log.setPriceFound(priceFound);
		log.setErrorMessage(errorMessage);
		log.setSyncDate(LocalDate.now());
		log.setCreatedAt(LocalDateTime.now());

		syncLogRepository.save(log);
	}

	public void logSyncResult(String storeId, UUID productId, String source,
							 BigDecimal price, String status, String errorMsg) {
		logSyncAttempt(storeId, productId, source, price, status, errorMsg);
	}

}
