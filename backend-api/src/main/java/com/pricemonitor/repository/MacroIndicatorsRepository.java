package com.pricemonitor.repository;

import com.pricemonitor.model.MacroIndicators;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MacroIndicatorsRepository extends JpaRepository<MacroIndicators, UUID> {

	Optional<MacroIndicators> findByIndicatorCode(String indicatorCode);

	List<MacroIndicators> findByIndicatorName(String indicatorName);

	List<MacroIndicators> findByFrequency(String frequency);

	@Query("SELECT m FROM MacroIndicators m WHERE m.indicatorDate BETWEEN :startDate AND :endDate ORDER BY m.indicatorDate DESC")
	List<MacroIndicators> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

	@Query("SELECT m FROM MacroIndicators m WHERE m.indicatorCode = :code ORDER BY m.indicatorDate DESC")
	List<MacroIndicators> findHistoryByIndicatorCode(@Param("code") String code);

	List<MacroIndicators> findByIndicatorCodeOrderByIndicatorDateDesc(String indicatorCode);
}
