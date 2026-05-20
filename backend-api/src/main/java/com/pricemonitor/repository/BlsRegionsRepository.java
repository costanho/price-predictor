package com.pricemonitor.repository;

import com.pricemonitor.model.BlsRegions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BlsRegionsRepository extends JpaRepository<BlsRegions, String> {

	Optional<BlsRegions> findByCode(String code);

	Optional<BlsRegions> findByName(String name);

	Optional<BlsRegions> findByBlsSuffix(String blsSuffix);
}
