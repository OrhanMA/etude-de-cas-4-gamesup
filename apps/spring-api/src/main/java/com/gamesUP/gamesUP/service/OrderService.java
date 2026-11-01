package com.gamesUP.gamesUP.service;

import com.gamesUP.gamesUP.dto.OrderDto;
import com.gamesUP.gamesUP.dto.CreateOrderRequest;
import com.gamesUP.gamesUP.dto.UpdateOrderRequest;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

public interface OrderService {
    List<OrderDto> getAllOrders();
    OrderDto getOrder(Long id) throws ResponseStatusException;
    OrderDto createOrder(CreateOrderRequest request) throws ResponseStatusException;
    void updateOrder(Long id, UpdateOrderRequest request) throws ResponseStatusException;
    void deleteOrder(Long id) throws ResponseStatusException;
}
