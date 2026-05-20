package com.pricemonitor.controller;

import com.pricemonitor.dto.CartOptimizationResponse;
import com.pricemonitor.model.UserCartItems;
import com.pricemonitor.service.AuthService;
import com.pricemonitor.service.CartService;
import com.pricemonitor.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/cart")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:19006"})
@RequiredArgsConstructor
public class CartController {

	private final CartService cartService;
	private final AuthService authService;
	private final ProductService productService;

	@GetMapping
	public ResponseEntity<?> getCart(@RequestHeader("Authorization") String authHeader) {
		try {
			UUID userId = extractUserIdFromToken(authHeader);

			List<UserCartItems> items = cartService.getUserCart(userId);
			List<Map<String, Object>> cartItems = items.stream()
				.map(item -> {
					Map<String, Object> itemMap = new HashMap<>();
					itemMap.put("id", item.getId());
					itemMap.put("productId", item.getProductId());
					itemMap.put("quantity", item.getQuantity());
					itemMap.put("unitOverride", item.getUnitOverride());

					productService.getProductById(item.getProductId()).ifPresent(product -> {
						itemMap.put("productName", product.getName());
						itemMap.put("unit", product.getUnit());
						itemMap.put("category", product.getCategory());
					});

					return itemMap;
				})
				.collect(Collectors.toList());

			Map<String, Object> response = new HashMap<>();
			response.put("items", cartItems);
			response.put("totalItems", cartItems.size());

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}

	@PostMapping("/add")
	public ResponseEntity<?> addToCart(
		@RequestHeader("Authorization") String authHeader,
		@RequestBody AddToCartRequest request) {

		try {
			UUID userId = extractUserIdFromToken(authHeader);

			cartService.addToCart(userId, request.getProductId(), request.getQuantity());

			return ResponseEntity.ok(Map.of("success", true, "message", "Item added to cart"));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
		}
	}

	@DeleteMapping("/{itemId}")
	public ResponseEntity<?> removeFromCart(
		@PathVariable UUID itemId,
		@RequestHeader("Authorization") String authHeader) {

		try {
			UUID userId = extractUserIdFromToken(authHeader);
			cartService.removeFromCart(userId, itemId);

			return ResponseEntity.ok(Map.of("success", true, "message", "Item removed from cart"));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
		}
	}

	@PostMapping("/optimize")
	public ResponseEntity<?> optimizeCart(
		@RequestHeader("Authorization") String authHeader,
		@RequestParam(value = "region", defaultValue = "national") String region) {

		try {
			UUID userId = extractUserIdFromToken(authHeader);

			CartOptimizationResponse optimization = cartService.optimizeCart(userId, region);

			Map<String, Object> response = new HashMap<>();
			response.put("cheapestSingleStore", Map.of(
				"store", "Walmart",
				"total", optimization.getCheapestSingleTotal()
			));
			response.put("optimizedTotal", optimization.getOptimizedTotal());
			response.put("weeklySavings", optimization.getSavingsAmount());
			response.put("annualProjection", optimization.getSavingsAmount().multiply(BigDecimal.valueOf(52)));
			response.put("breakdown", Map.of(
				"Aldi", Map.of("items", new Object[]{}, "subtotal", 0),
				"Kroger", Map.of("items", new Object[]{}, "subtotal", 0)
			));

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}

	@DeleteMapping
	public ResponseEntity<?> clearCart(@RequestHeader("Authorization") String authHeader) {
		try {
			UUID userId = extractUserIdFromToken(authHeader);
			cartService.clearCart(userId);

			return ResponseEntity.ok(Map.of("success", true, "message", "Cart cleared"));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
		}
	}

	private UUID extractUserIdFromToken(String authHeader) {
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			throw new RuntimeException("Invalid or missing authorization header");
		}

		String token = authHeader.substring(7);
		return authService.getUserFromToken(token).getId();
	}

	public static class AddToCartRequest {
		private UUID productId;
		private BigDecimal quantity;

		public UUID getProductId() { return productId; }
		public void setProductId(UUID productId) { this.productId = productId; }

		public BigDecimal getQuantity() { return quantity; }
		public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
	}
}
