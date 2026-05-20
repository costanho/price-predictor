package com.pricemonitor.repository;

import com.pricemonitor.model.StorePriceFactors;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StorePriceFactorsRepository extends JpaRepository<StorePriceFactors, UUID> {

	List<StorePriceFactors> findByStoreId(String storeId);

	List<StorePriceFactors> findByCategory(String category);

	List<StorePriceFactors> findByStoreIdAndCategory(String storeId, String category);

	List<StorePriceFactors> findBySource(String source);
}
