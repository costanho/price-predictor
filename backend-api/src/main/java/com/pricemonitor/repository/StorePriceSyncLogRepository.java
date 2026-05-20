package com.pricemonitor.repository;

import com.pricemonitor.model.StorePriceSyncLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StorePriceSyncLogRepository extends JpaRepository<StorePriceSyncLog, UUID> {

	List<StorePriceSyncLog> findByStoreIdAndSyncDate(String storeId, LocalDate syncDate);

	List<StorePriceSyncLog> findByProductIdAndSource(UUID productId, String source);

	Optional<StorePriceSyncLog> findByStoreIdAndProductIdAndSyncDateAndSource(
		String storeId, UUID productId, LocalDate syncDate, String source);

	@Query("SELECT l FROM StorePriceSyncLog l WHERE l.status = 'failed' ORDER BY l.createdAt DESC")
	List<StorePriceSyncLog> findFailedSyncs();

	@Query("SELECT l FROM StorePriceSyncLog l WHERE l.source = :source AND l.syncDate = :syncDate")
	List<StorePriceSyncLog> findBySyncDateAndSource(@Param("syncDate") LocalDate syncDate, @Param("source") String source);

	long countBySourceAndStatus(String source, String status);
}
