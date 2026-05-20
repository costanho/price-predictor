package com.pricemonitor.repository;

import com.pricemonitor.model.CartOptimizations;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartOptimizationsRepository extends JpaRepository<CartOptimizations, UUID> {

	List<CartOptimizations> findByUserId(UUID userId);

	Optional<CartOptimizations> findByUserIdOrderByCalculatedAtDesc(UUID userId);

	@Query("SELECT c FROM CartOptimizations c WHERE c.userId = :userId AND c.expiresAt > CURRENT_TIMESTAMP ORDER BY c.calculatedAt DESC LIMIT 1")
	Optional<CartOptimizations> findLatestActiveOptimization(@Param("userId") UUID userId);

	List<CartOptimizations> findByCalculatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
}
