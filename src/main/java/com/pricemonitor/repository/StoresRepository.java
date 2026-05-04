package com.pricemonitor.repository;

import com.pricemonitor.model.Stores;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoresRepository extends JpaRepository<Stores, String> {

	List<Stores> findByIsActiveTrue();

	Optional<Stores> findByName(String name);

	@Query("SELECT s FROM Stores s WHERE s.isActive = true ORDER BY s.name ASC")
	List<Stores> findAllActive();

	List<Stores> findByNameContainingIgnoreCase(String name);
}
