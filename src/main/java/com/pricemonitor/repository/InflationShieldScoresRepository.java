package com.pricemonitor.repository;

import com.pricemonitor.model.InflationShieldScores;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InflationShieldScoresRepository extends JpaRepository<InflationShieldScores, UUID> {

	List<InflationShieldScores> findByProductId(UUID productId);

	List<InflationShieldScores> findByRegionCode(String regionCode);

	Optional<InflationShieldScores> findByProductIdAndRegionCode(UUID productId, String regionCode);

	@Query("SELECT i FROM InflationShieldScores i WHERE i.score >= :minScore ORDER BY i.score DESC")
	List<InflationShieldScores> findHighScoresAboveThreshold(@Param("minScore") Integer minScore);

	List<InflationShieldScores> findByScoreGreaterThanOrderByScoreDesc(Integer score);
}
