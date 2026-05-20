package com.pricemonitor.repository;

import com.pricemonitor.model.UserCartItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;import java.util.Optional;

@Repository
public interface UserCartItemRepository extends JpaRepository<UserCartItems, UUID> {
}
