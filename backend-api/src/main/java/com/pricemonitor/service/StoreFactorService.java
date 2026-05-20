package com.pricemonitor.service;

import com.pricemonitor.model.StorePriceFactors;
import com.pricemonitor.repository.StorePriceFactorsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreFactorService {

	private final StorePriceFactorsRepository storePriceFactorsRepository;

	public BigDecimal getFactor(String storeId) {
		List<StorePriceFactors> factors = storePriceFactorsRepository.findByStoreId(storeId);

		if (factors.isEmpty()) {
			return BigDecimal.ONE;
		}

		BigDecimal sum = BigDecimal.ZERO;
		for (StorePriceFactors factor : factors) {
			sum = sum.add(factor.getFactor());
		}

		return sum.divide(BigDecimal.valueOf(factors.size()), 4, java.math.RoundingMode.HALF_UP);
	}

	public BigDecimal getFactorForCategory(String storeId, String category) {
		List<StorePriceFactors> factors = storePriceFactorsRepository.findByStoreIdAndCategory(storeId, category);

		if (factors.isEmpty()) {
			return getFactor(storeId);
		}

		if (factors.size() == 1) {
			return factors.get(0).getFactor();
		}

		BigDecimal sum = BigDecimal.ZERO;
		for (StorePriceFactors factor : factors) {
			sum = sum.add(factor.getFactor());
		}

		return sum.divide(BigDecimal.valueOf(factors.size()), 4, java.math.RoundingMode.HALF_UP);
	}
}
