package com.pricemonitor.repository;

import com.pricemonitor.model.UserProductTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserProductTrackingRepository extends JpaRepository<UserProductTracking, UserProductTracking.UserProductTrackingKey> {

	@Query("SELECT u FROM UserProductTracking u WHERE u.id.userId = :userId")
	List<UserProductTracking> findByUserId(@Param("userId") UUID userId);

	@Query("SELECT u FROM UserProductTracking u WHERE u.id.productId = :productId")
	List<UserProductTracking> findByProductId(@Param("productId") UUID productId);

	@Query("SELECT u FROM UserProductTracking u WHERE u.id.userId = :userId AND u.id.productId = :productId")
	Optional<UserProductTracking> findByUserIdAndProductId(@Param("userId") UUID userId, @Param("productId") UUID productId);

	@Query("SELECT COUNT(u) FROM UserProductTracking u WHERE u.id.userId = :userId")
	Integer countByUserId(@Param("userId") UUID userId);

	// Find all user IDs who track a specific product
	@Query("SELECT u.id.userId FROM UserProductTracking u WHERE u.id.productId = :productId")
	List<UUID> findUserIdsByProductId(@Param("productId") UUID productId);

	// Find users who have set a target price for this product
	@Query("SELECT u.id.userId, u.targetPrice FROM UserProductTracking u " +
		   "WHERE u.id.productId = :productId AND u.targetPrice IS NOT NULL")
	List<Object[]> findUsersWithTargetPrice(@Param("productId") UUID productId);
}
