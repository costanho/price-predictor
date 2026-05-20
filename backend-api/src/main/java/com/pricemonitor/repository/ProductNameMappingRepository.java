package com.pricemonitor.repository;

import com.pricemonitor.model.ProductNameMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductNameMappingRepository extends JpaRepository<ProductNameMapping, UUID> {

	Optional<ProductNameMapping> findByExternalNameAndSource(String externalName, String source);

	List<ProductNameMapping> findByProductId(UUID productId);

	List<ProductNameMapping> findBySource(String source);

	@Query("SELECT m FROM ProductNameMapping m WHERE m.source = :source AND m.productId IS NOT NULL")
	List<ProductNameMapping> findAllMappedBySource(@Param("source") String source);

	boolean existsByExternalNameAndSource(String externalName, String source);
}
