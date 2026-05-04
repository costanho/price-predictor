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
}
