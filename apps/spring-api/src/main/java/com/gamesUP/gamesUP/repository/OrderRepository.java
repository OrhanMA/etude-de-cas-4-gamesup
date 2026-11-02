package com.gamesUP.gamesUP.repository;

import com.gamesUP.gamesUP.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    boolean existsByUserId(Long userId);
    List<Order> findByUserEmailIgnoreCaseOrderByCreatedAtDesc(String email);

    @Query("select distinct purchaseOrder from Order purchaseOrder "
            + "join fetch purchaseOrder.user "
            + "left join fetch purchaseOrder.orderGames orderLine "
            + "left join fetch orderLine.game "
            + "where purchaseOrder.paid = true or purchaseOrder.delivered = true")
    List<Order> findAllCompletedOrdersWithLines();
}
