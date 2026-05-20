package com.pricemonitor.service;

import com.pricemonitor.model.*;
import com.pricemonitor.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PriceSyncService {

    @Autowired private ProductsRepository productRepo;
    @Autowired private PriceHistoryRepository priceHistoryRepo;
    @Autowired private PriceForecastRepository forecastRepo;
    @Autowired private KrogerApiClient krogerClient;
    @Autowired private SpoonacularApiClient spoonacularClient;
    @Autowired private AlertGenerationService alertService;

    @Value("${price.sync.enabled:true}")
    private boolean syncEnabled;

    // ── Main scheduled job — runs daily at 6am ───────────────────────────────
    @Scheduled(cron = "${price.sync.cron:0 0 6 * * ?}")
    @Transactional
    public void runDailySync() {
        if (!syncEnabled) {
            System.out.println("Price sync disabled — skipping");
            return;
        }
        System.out.println("=== Daily price sync starting: " + LocalDateTime.now() + " ===");

        List<Products> products = productRepo.findByIsActiveTrue();
        int krogerSuccess = 0, spoonacularSuccess = 0, failed = 0;

        for (Products product : products) {
            try {
                // 1 — Kroger (most accurate for Kroger store)
                boolean krogerOk = syncKrogerPrice(product);
                if (krogerOk) krogerSuccess++;

                // 2 — Spoonacular (Walmart, Target, Whole Foods)
                int spoonCount = syncSpoonacularPrices(product);
                spoonacularSuccess += spoonCount;

                // 3 — Invalidate cached forecasts for this product
                // so next frontend request triggers fresh ML prediction
                forecastRepo.deactivateByProductId(product.getId());

            } catch (Exception e) {
                System.err.println("Sync failed for product "
                    + product.getName() + ": " + e.getMessage());
                failed++;
            }

            // Rate limit protection — Spoonacular free tier is 150 req/day
            // 60 products × 2 calls each = 120 calls. Add 500ms pause to be safe.
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        }

        System.out.println("=== Sync complete. Kroger: " + krogerSuccess
            + " Spoonacular: " + spoonacularSuccess + " Failed: " + failed + " ===");
    }

    // ── Sync one product from Kroger ─────────────────────────────────────────
    private boolean syncKrogerPrice(Products product) {
        try {
            BigDecimal price = krogerClient.getProductPrice(product.getShortName());
            if (price == null) return false;

            // Check if we already have today's price from Kroger
            boolean alreadySynced = priceHistoryRepo.existsByProductIdAndStoreIdAndPriceDateAndDataSource(
                product.getId(), "kroger", LocalDate.now(), "kroger_api"
            );
            if (alreadySynced) return true;

            // Insert into price_history
            PriceHistories ph = new PriceHistories();
            ph.setProductId(product.getId());
            ph.setStoreId("kroger");
            ph.setRegionCode("national"); // Kroger is location-specific but we use national for ML
            ph.setPrice(price);
            ph.setPriceDate(LocalDate.now());
            ph.setDataSource("kroger_api");
            priceHistoryRepo.save(ph);

            // Check for price drop and target price alerts
            alertService.checkPriceDrop(product.getId(), "kroger", price);
            alertService.checkTargetPriceReached(product.getId(), "kroger", price);

            return true;
        } catch (Exception e) {
            System.err.println("Kroger sync error for " + product.getName() + ": " + e.getMessage());
            return false;
        }
    }

    // ── Sync one product from Spoonacular (multiple stores) ──────────────────
    private int syncSpoonacularPrices(Products product) {
        int count = 0;
        try {
            List<SpoonacularApiClient.StorePriceResult> results =
                spoonacularClient.searchProductPrices(product.getShortName());

            for (SpoonacularApiClient.StorePriceResult result : results) {
                // Skip Kroger from Spoonacular — we have the official API for that
                if ("kroger".equals(result.storeId)) continue;

                // Check for today's existing record
                boolean exists = priceHistoryRepo.existsByProductIdAndStoreIdAndPriceDateAndDataSource(
                    product.getId(), result.storeId, LocalDate.now(), "spoonacular"
                );
                if (exists) { count++; continue; }

                // Insert
                PriceHistories ph = new PriceHistories();
                ph.setProductId(product.getId());
                ph.setStoreId(result.storeId);
                ph.setRegionCode("national");
                ph.setPrice(result.price);
                ph.setPriceDate(LocalDate.now());
                ph.setDataSource("spoonacular");
                priceHistoryRepo.save(ph);

                // Check for price drop and target price alerts
                alertService.checkPriceDrop(product.getId(), result.storeId, result.price);
                alertService.checkTargetPriceReached(product.getId(), result.storeId, result.price);
                count++;
            }
        } catch (Exception e) {
            System.err.println("Spoonacular sync error for " + product.getName() + ": " + e.getMessage());
        }
        return count;
    }

    // ── Manual trigger (call from a test endpoint or admin panel) ────────────
    public void triggerManualSync() {
        runDailySync();
    }
}
