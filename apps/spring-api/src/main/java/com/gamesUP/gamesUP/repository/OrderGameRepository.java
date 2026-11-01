package com.gamesUP.gamesUP.repository;

import com.gamesUP.gamesUP.model.OrderGame;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderGameRepository extends JpaRepository<OrderGame, Long> {
    List<OrderGame> findByOrderId(Long orderId);
}
