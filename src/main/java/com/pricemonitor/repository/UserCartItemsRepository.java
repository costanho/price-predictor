package com.pricemonitor.repository;

import com.pricemonitor.model.UserCartItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserCartItemsRepository extends JpaRepository<UserCartItems, UUID> {

	List<UserCartItems> findByUserId(UUID userId);

	Optional<UserCartItems> findByUserIdAndProductId(UUID userId, UUID productId);

	void deleteByUserIdAndProductId(UUID userId, UUID productId);

	void deleteByUserId(UUID userId);

	@Query("SELECT COUNT(u) FROM UserCartItems u WHERE u.userId = :userId")
	Integer countByUserId(@Param("userId") UUID userId);
}
