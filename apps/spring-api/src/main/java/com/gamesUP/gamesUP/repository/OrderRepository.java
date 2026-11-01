package com.gamesUP.gamesUP.repository;

import com.gamesUP.gamesUP.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    boolean existsByUserId(Long userId);
    List<Order> findByUserEmailIgnoreCaseOrderByCreatedAtDesc(String email);
}
