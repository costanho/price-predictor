package com.pricemonitor.repository;

import com.pricemonitor.model.Products;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductsRepository extends JpaRepository<Products, UUID> {

	List<Products> findByIsActiveTrue();

	List<Products> findByCategory(String category);

	List<Products> findByCategoryAndIsActiveTrue(String category);

	Optional<Products> findByBlsSeriesId(String blsSeriesId);

	Optional<Products> findByBlsItemCode(String blsItemCode);

	@Query("SELECT p FROM Products p WHERE p.isActive = true AND " +
		   "(LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
		   "LOWER(p.shortName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
		   "LOWER(p.category) LIKE LOWER(CONCAT('%', :query, '%')))")
	List<Products> searchProducts(@Param("query") String query);

	@Query("SELECT p FROM Products p WHERE p.isActive = true ORDER BY p.name ASC")
	List<Products> findAllActive();

	List<Products> findByUnit(String unit);
}
