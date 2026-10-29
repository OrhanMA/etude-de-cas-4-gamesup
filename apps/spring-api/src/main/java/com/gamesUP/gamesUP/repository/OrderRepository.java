package com.gamesUP.gamesUP.repository;

import com.gamesUP.gamesUP.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
