package com.pricemonitor.controller;

import com.pricemonitor.model.Products;
import com.pricemonitor.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/products")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:19006"})
@RequiredArgsConstructor
public class ProductController {

	private final ProductService productService;

	@GetMapping
	public ResponseEntity<?> getAllProducts() {
		try {
			List<Products> products = productService.getAllActiveProducts();
			List<Map<String, Object>> response = products.stream()
				.map(this::productToMap)
				.collect(Collectors.toList());

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}

	@GetMapping("/{id}")
	public ResponseEntity<?> getProductById(@PathVariable UUID id) {
		try {
			return productService.getProductById(id)
				.map(product -> ResponseEntity.ok(productToMap(product)))
				.orElse(ResponseEntity.notFound().build());
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}

	@GetMapping("/category/{category}")
	public ResponseEntity<?> getProductsByCategory(@PathVariable String category) {
		try {
			List<Products> products = productService.getProductsByCategory(category);
			List<Map<String, Object>> response = products.stream()
				.map(this::productToMap)
				.collect(Collectors.toList());

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}

	@GetMapping("/search")
	public ResponseEntity<?> searchProducts(@RequestParam(value = "q", required = true) String query) {
		try {
			List<Products> products = productService.searchProducts(query);
			List<Map<String, Object>> response = products.stream()
				.map(this::productToMap)
				.collect(Collectors.toList());

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}

	@GetMapping("/{id}/shield-score")
	public ResponseEntity<?> getShieldScore(@PathVariable UUID id) {
		try {
			return productService.getProductById(id)
				.map(product -> ResponseEntity.ok(Map.of(
					"productId", id,
					"productName", product.getName(),
					"shieldScore", 85, // Placeholder - integrate with InflationShieldScores if available
					"inflationRisk", "moderate",
					"recommendation", "Buy now - prices expected to rise 3-5%"
				)))
				.orElse(ResponseEntity.notFound().build());
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}

	private Map<String, Object> productToMap(Products product) {
		Map<String, Object> map = new HashMap<>();
		map.put("id", product.getId());
		map.put("name", product.getName());
		map.put("shortName", product.getShortName());
		map.put("category", product.getCategory());
		map.put("unit", product.getUnit());
		map.put("imageUrl", product.getImageUrl());
		map.put("blsSeriesId", product.getBlsSeriesId());
		map.put("blsItemCode", product.getBlsItemCode());
		map.put("isActive", product.getIsActive());
		map.put("createdAt", product.getCreatedAt());
		map.put("updatedAt", product.getUpdatedAt());
		return map;
	}
}
