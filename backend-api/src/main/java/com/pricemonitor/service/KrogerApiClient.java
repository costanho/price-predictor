package com.pricemonitor.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class KrogerApiClient {

    @Value("${kroger.api.client.id}")
    private String clientId;

    @Value("${kroger.api.client.secret}")
    private String clientSecret;

    @Value("${kroger.api.base.url}")
    private String baseUrl;

    @Value("${kroger.api.token.url}")
    private String tokenUrl;

    @Value("${kroger.api.location.id}")
    private String locationId;

    private String cachedToken = null;
    private LocalDateTime tokenExpiry = null;

    private final RestTemplate restTemplate = new RestTemplate();

    // ── Get OAuth2 token (cached until expiry) ──────────────────────────────
    private String getAccessToken() {
        if (cachedToken != null && tokenExpiry != null
                && LocalDateTime.now().isBefore(tokenExpiry)) {
            return cachedToken;
        }

        String credentials = Base64.getEncoder().encodeToString(
            (clientId + ":" + clientSecret).getBytes()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Basic " + credentials);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("scope", "product.compact");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                tokenUrl, request, Map.class
            );
            Map<String, Object> data = response.getBody();
            cachedToken = (String) data.get("access_token");
            // Kroger tokens last 1800 seconds — cache for 25 minutes to be safe
            tokenExpiry = LocalDateTime.now().plusMinutes(25);
            return cachedToken;
        } catch (Exception e) {
            throw new RuntimeException("Kroger token fetch failed: " + e.getMessage());
        }
    }

    // ── Search for a product and return its price ───────────────────────────
    // Returns null if product not found or API error
    public BigDecimal getProductPrice(String productName) {
        try {
            String token = getAccessToken();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            headers.set("Accept", "application/json");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            String url = baseUrl + "/products"
                + "?filter.term=" + productName.replace(" ", "%20")
                + "&filter.locationId=" + locationId
                + "&filter.limit=5";

            ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, Map.class
            );

            Map<String, Object> body = response.getBody();
            if (body == null) return null;

            List<Map<String, Object>> products =
                (List<Map<String, Object>>) body.get("data");
            if (products == null || products.isEmpty()) return null;

            // Take first result — best match
            Map<String, Object> product = products.get(0);
            List<Map<String, Object>> items =
                (List<Map<String, Object>>) product.get("items");
            if (items == null || items.isEmpty()) return null;

            Map<String, Object> item = items.get(0);
            Map<String, Object> price = (Map<String, Object>) item.get("price");
            if (price == null) return null;

            Object regular = price.get("regular");
            if (regular == null) return null;

            return new BigDecimal(regular.toString());

        } catch (Exception e) {
            // Log but do not crash — return null so scheduler continues
            System.err.println("Kroger price fetch failed for '" + productName
                + "': " + e.getMessage());
            return null;
        }
    }

    // ── Find a location ID near a zip code ──────────────────────────────────
    // Call this once during setup to find locationId for your test zip code
    // Then hardcode the result in application.properties
    public String findLocationId(String zipCode) {
        try {
            String token = getAccessToken();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            String url = baseUrl + "/locations?filter.zipCode=" + zipCode + "&filter.limit=1";
            ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, Map.class
            );
            Map body = response.getBody();
            List<Map> locations = (List<Map>) body.get("data");
            if (locations == null || locations.isEmpty()) return null;
            return (String) locations.get(0).get("locationId");
        } catch (Exception e) {
            System.err.println("Kroger location fetch failed: " + e.getMessage());
            return null;
        }
    }
}
