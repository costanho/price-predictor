package com.pricemonitor.repository;

import com.pricemonitor.model.UserTrackedStores;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserTrackedStoresRepository extends JpaRepository<UserTrackedStores, UserTrackedStores.UserTrackedStoresKey> {

	@Query("SELECT u FROM UserTrackedStores u WHERE u.id.userId = :userId")
	List<UserTrackedStores> findByUserId(@Param("userId") UUID userId);

	@Query("SELECT u FROM UserTrackedStores u WHERE u.id.storeId = :storeId")
	List<UserTrackedStores> findByStoreId(@Param("storeId") String storeId);

	@Query("SELECT u FROM UserTrackedStores u WHERE u.id.userId = :userId AND u.id.storeId = :storeId")
	Optional<UserTrackedStores> findByUserIdAndStoreId(@Param("userId") UUID userId, @Param("storeId") String storeId);

	@Query("SELECT COUNT(u) FROM UserTrackedStores u WHERE u.id.userId = :userId")
	Integer countByUserId(@Param("userId") UUID userId);

	@Query("SELECT u.id.storeId FROM UserTrackedStores u WHERE u.id.userId = :userId")
	List<String> findStoreIdsByUserId(@Param("userId") UUID userId);

	@Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM UserTrackedStores u WHERE u.id.userId = :userId AND u.id.storeId = :storeId")
	boolean existsByUserIdAndStoreId(@Param("userId") UUID userId, @Param("storeId") String storeId);

	@Modifying
	@Transactional
	@Query("DELETE FROM UserTrackedStores u WHERE u.id.userId = :userId AND u.id.storeId = :storeId")
	void deleteByUserIdAndStoreId(@Param("userId") UUID userId, @Param("storeId") String storeId);
}
