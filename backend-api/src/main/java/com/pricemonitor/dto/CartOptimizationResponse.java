package com.pricemonitor.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class CartOptimizationResponse {

	private BigDecimal currentTotal;
	private BigDecimal optimizedTotal;
	private BigDecimal savingsAmount;
	private Double savingsPercentage;
	private BigDecimal cheapestSingleTotal;
	private List<CartItemSuggestion> suggestions;

	public CartOptimizationResponse() {
	}

	public CartOptimizationResponse(BigDecimal currentTotal, BigDecimal optimizedTotal, BigDecimal savingsAmount,
								   Double savingsPercentage, BigDecimal cheapestSingleTotal, List<CartItemSuggestion> suggestions) {
		this.currentTotal = currentTotal;
		this.optimizedTotal = optimizedTotal;
		this.savingsAmount = savingsAmount;
		this.savingsPercentage = savingsPercentage;
		this.cheapestSingleTotal = cheapestSingleTotal;
		this.suggestions = suggestions;
	}

	public BigDecimal getCurrentTotal() {
		return currentTotal;
	}

	public void setCurrentTotal(BigDecimal currentTotal) {
		this.currentTotal = currentTotal;
	}

	public BigDecimal getOptimizedTotal() {
		return optimizedTotal;
	}

	public void setOptimizedTotal(BigDecimal optimizedTotal) {
		this.optimizedTotal = optimizedTotal;
	}

	public BigDecimal getSavingsAmount() {
		return savingsAmount;
	}

	public void setSavingsAmount(BigDecimal savingsAmount) {
		this.savingsAmount = savingsAmount;
	}

	public Double getSavingsPercentage() {
		return savingsPercentage;
	}

	public void setSavingsPercentage(Double savingsPercentage) {
		this.savingsPercentage = savingsPercentage;
	}

	public BigDecimal getCheapestSingleTotal() {
		return cheapestSingleTotal;
	}

	public void setCheapestSingleTotal(BigDecimal cheapestSingleTotal) {
		this.cheapestSingleTotal = cheapestSingleTotal;
	}

	public List<CartItemSuggestion> getSuggestions() {
		return suggestions;
	}

	public void setSuggestions(List<CartItemSuggestion> suggestions) {
		this.suggestions = suggestions;
	}

	public static class CartItemSuggestion {
		private UUID productId;
		private String suggestion;

		public CartItemSuggestion(UUID productId, String suggestion) {
			this.productId = productId;
			this.suggestion = suggestion;
		}

		public UUID getProductId() {
			return productId;
		}

		public void setProductId(UUID productId) {
			this.productId = productId;
		}

		public String getSuggestion() {
			return suggestion;
		}

		public void setSuggestion(String suggestion) {
			this.suggestion = suggestion;
		}
	}
}
