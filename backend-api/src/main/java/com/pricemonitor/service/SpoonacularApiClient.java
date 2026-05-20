package com.pricemonitor.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.util.*;

@Service
public class SpoonacularApiClient {

    @Value("${spoonacular.api.key}")
    private String apiKey;

    @Value("${spoonacular.api.base.url}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    // Store name mappings from Spoonacular names to your store IDs
    private static final Map<String, String> STORE_MAP = Map.of(
        "Walmart",        "walmart",
        "Kroger",         "kroger",
        "Whole Foods",    "whole_foods",
        "Whole Foods Market", "whole_foods",
        "Target",         "target",
        "Trader Joe's",   "trader_joes",
        "Aldi",           "aldi"
    );

    // Result DTO
    public static class StorePriceResult {
        public String storeId;
        public BigDecimal price;
        public String productTitle;
        public StorePriceResult(String storeId, BigDecimal price, String title) {
            this.storeId = storeId;
            this.price = price;
            this.productTitle = title;
        }
    }

    // ── Search for a product — returns prices from all stores found ──────────
    public List<StorePriceResult> searchProductPrices(String productName) {
        List<StorePriceResult> results = new ArrayList<>();

        try {
            String url = baseUrl + "/food/products/search"
                + "?query=" + productName.replace(" ", "%20")
                + "&number=10"
                + "&apiKey=" + apiKey;

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> body = response.getBody();
            if (body == null) return results;

            List<Map<String, Object>> products =
                (List<Map<String, Object>>) body.get("products");
            if (products == null) return results;

            // For each result, get detailed pricing
            for (Map<String, Object> product : products.subList(0, Math.min(3, products.size()))) {
                Integer id = (Integer) product.get("id");
                if (id == null) continue;

                StorePriceResult detail = getProductDetail(id);
                if (detail != null) results.add(detail);
            }

        } catch (Exception e) {
            System.err.println("Spoonacular search failed for '" + productName
                + "': " + e.getMessage());
        }

        return results;
    }

    // ── Get detailed pricing for one product ID ──────────────────────────────
    private StorePriceResult getProductDetail(Integer productId) {
        try {
            String url = baseUrl + "/food/products/" + productId
                + "?apiKey=" + apiKey;

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> body = response.getBody();
            if (body == null) return null;

            String title = (String) body.get("title");

            // Spoonacular returns price in price field (dollars)
            Object priceObj = body.get("price");
            if (priceObj == null) return null;

            BigDecimal price = new BigDecimal(priceObj.toString());

            // Price sanity check — Spoonacular sometimes returns cents not dollars
            // Grocery items should be between $0.50 and $50
            if (price.doubleValue() > 50) price = price.divide(BigDecimal.valueOf(100));
            if (price.doubleValue() < 0.10) return null;

            // Try to determine store from product title or aisle
            String storeId = inferStore(body, title);
            if (storeId == null) return null;

            return new StorePriceResult(storeId, price, title);

        } catch (Exception e) {
            return null;
        }
    }

    // ── Infer store from Spoonacular response fields ──────────────────────────
    private String inferStore(Map<String, Object> body, String title) {
        // Check explicit store field if present
        String storeStr = (String) body.get("storeAffiliation");
        if (storeStr != null) {
            for (Map.Entry<String, String> entry : STORE_MAP.entrySet()) {
                if (storeStr.toLowerCase().contains(entry.getKey().toLowerCase())) {
                    return entry.getValue();
                }
            }
        }

        // Check badges
        List<Map> badges = (List<Map>) body.get("badges");
        if (badges != null) {
            for (Map badge : badges) {
                String name = (String) badge.get("name");
                if (name == null) continue;
                for (Map.Entry<String, String> entry : STORE_MAP.entrySet()) {
                    if (name.toLowerCase().contains(entry.getKey().toLowerCase())) {
                        return entry.getValue();
                    }
                }
            }
        }

        // Default to walmart as Spoonacular data skews Walmart
        return "walmart";
    }
}
