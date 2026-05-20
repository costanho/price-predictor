package com.pricemonitor.repository;

import com.pricemonitor.model.AnomalyDetections;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
@Repository
public interface AnomalyDetectionRepository extends JpaRepository<AnomalyDetections, UUID> {
}
