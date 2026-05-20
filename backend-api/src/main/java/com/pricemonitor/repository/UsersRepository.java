package com.pricemonitor.repository;

import com.pricemonitor.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsersRepository extends JpaRepository<Users, UUID> {

	Optional<Users> findByEmail(String email);

	List<Users> findByIsActiveTrue();

	List<Users> findByBlsRegion(String blsRegion);

	@Query("SELECT u FROM Users u WHERE u.isActive = true AND u.email = :email")
	Optional<Users> findActiveUserByEmail(@Param("email") String email);

	List<Users> findByZipCode(String zipCode);

	@Query("SELECT u FROM Users u WHERE u.isActive = true ORDER BY u.createdAt DESC")
	List<Users> findAllActive();

	@Query("SELECT COUNT(u) FROM Users u WHERE u.isActive = true")
	Integer countActiveUsers();
}
