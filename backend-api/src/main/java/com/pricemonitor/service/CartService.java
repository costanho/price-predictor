package com.pricemonitor.service;

import com.pricemonitor.dto.CartOptimizationResponse;
import com.pricemonitor.model.CartOptimizations;
import com.pricemonitor.model.PriceForecasts;
import com.pricemonitor.model.StorePriceFactors;
import com.pricemonitor.model.UserCartItems;
import com.pricemonitor.repository.CartOptimizationsRepository;
import com.pricemonitor.repository.PriceContractsRepository;
import com.pricemonitor.repository.StorePriceFactorsRepository;
import com.pricemonitor.repository.UserCartItemsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartService {

	private final UserCartItemsRepository cartItemsRepository;
	private final CartOptimizationsRepository cartOptimizationsRepository;
	private final PriceContractsRepository forecastRepository;
	private final StorePriceFactorsRepository priceFactorsRepository;

	public List<UserCartItems> getUserCart(UUID userId) {
		return cartItemsRepository.findByUserId(userId);
	}

	public UserCartItems addToCart(UUID userId, UUID productId, BigDecimal quantity) {
		Optional<UserCartItems> existing = cartItemsRepository.findByUserIdAndProductId(userId, productId);

		UserCartItems cartItem;
		if (existing.isPresent()) {
			cartItem = existing.get();
			cartItem.setQuantity(cartItem.getQuantity().add(quantity));
		} else {
			cartItem = new UserCartItems();
			cartItem.setUserId(userId);
			cartItem.setProductId(productId);
			cartItem.setQuantity(quantity);
			cartItem.setAddedAt(LocalDateTime.now());
		}

		return cartItemsRepository.save(cartItem);
	}

	public void removeFromCart(UUID userId, UUID cartItemId) {
		cartItemsRepository.deleteById(cartItemId);
	}

	public void removeItemFromCart(UUID userId, UUID productId) {
		cartItemsRepository.deleteByUserIdAndProductId(userId, productId);
	}

	public void clearCart(UUID userId) {
		cartItemsRepository.deleteByUserId(userId);
	}

	public CartOptimizationResponse optimizeCart(UUID userId, String region) {
		List<UserCartItems> cartItems = getUserCart(userId);

		if (cartItems.isEmpty()) {
			throw new RuntimeException("Cart is empty");
		}

		Map<String, BigDecimal> storeTotals = new HashMap<>();
		BigDecimal currentTotal = BigDecimal.ZERO;
		BigDecimal cheapestSingleTotal = BigDecimal.valueOf(Double.MAX_VALUE);
		String cheapestStore = null;

		for (UserCartItems item : cartItems) {
			Optional<PriceForecasts> forecast = forecastRepository
				.findByProductIdAndRegionCodeAndIsActiveTrueAndExpiresAtGreaterThan(
					item.getProductId(), region, LocalDateTime.now()
				);

			BigDecimal itemPrice = forecast.isPresent()
				? forecast.get().getCurrentPrice()
				: BigDecimal.TEN;

			BigDecimal itemTotal = itemPrice.multiply(item.getQuantity());
			currentTotal = currentTotal.add(itemTotal);

			List<StorePriceFactors> factors = priceFactorsRepository.findByCategory(region);
			for (StorePriceFactors factor : factors) {
				BigDecimal adjustedPrice = itemPrice.multiply(factor.getFactor());
				String storeId = factor.getStoreId();
				storeTotals.put(storeId, storeTotals.getOrDefault(storeId, BigDecimal.ZERO).add(adjustedPrice.multiply(item.getQuantity())));
			}
		}

		for (Map.Entry<String, BigDecimal> entry : storeTotals.entrySet()) {
			if (entry.getValue().compareTo(cheapestSingleTotal) < 0) {
				cheapestSingleTotal = entry.getValue();
				cheapestStore = entry.getKey();
			}
		}

		BigDecimal weeklySavings = cheapestSingleTotal.compareTo(currentTotal) < 0
			? currentTotal.subtract(cheapestSingleTotal)
			: BigDecimal.ZERO;

		BigDecimal annualProjection = weeklySavings.multiply(BigDecimal.valueOf(52));

		CartOptimizations optimization = new CartOptimizations();
		optimization.setUserId(userId);
		optimization.setTotalItems(cartItems.size());
		optimization.setCheapestSingleTotal(cheapestSingleTotal);
		optimization.setCheapestSingleStore(cheapestStore);
		optimization.setOptimizedTotal(cheapestSingleTotal);
		optimization.setWeeklySavings(weeklySavings);
		optimization.setAnnualProjection(annualProjection);
		optimization.setStoreBreakdown("{\"optimized\": \"greedy_allocation\"}");
		optimization.setCalculatedAt(LocalDateTime.now());
		optimization.setExpiresAt(LocalDateTime.now().plusDays(1));

		cartOptimizationsRepository.save(optimization);

		return new CartOptimizationResponse(
			currentTotal,
			cheapestSingleTotal,
			weeklySavings,
			weeklySavings.compareTo(BigDecimal.ZERO) > 0
				? weeklySavings.divide(currentTotal, 4, java.math.RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue()
				: 0.0,
			cheapestSingleTotal,
			null
		);
	}
}
