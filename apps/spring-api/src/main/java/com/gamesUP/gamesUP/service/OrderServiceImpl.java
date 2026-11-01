package com.gamesUP.gamesUP.service;

import com.gamesUP.gamesUP.dto.OrderDto;
import com.gamesUP.gamesUP.dto.OrderLineDto;
import com.gamesUP.gamesUP.dto.UserDto;
import com.gamesUP.gamesUP.dto.AuthorDto;
import com.gamesUP.gamesUP.dto.GameDto;
import com.gamesUP.gamesUP.dto.PublisherDto;
import com.gamesUP.gamesUP.dto.CreateOrderRequest;
import com.gamesUP.gamesUP.dto.CreateOrderLineRequest;
import com.gamesUP.gamesUP.dto.UpdateOrderRequest;
import com.gamesUP.gamesUP.model.Order;
import com.gamesUP.gamesUP.model.OrderGame;
import com.gamesUP.gamesUP.model.Game;
import com.gamesUP.gamesUP.model.User;
import com.gamesUP.gamesUP.repository.GameRepository;
import com.gamesUP.gamesUP.repository.OrderGameRepository;
import com.gamesUP.gamesUP.repository.OrderRepository;
import com.gamesUP.gamesUP.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderGameRepository orderGameRepository;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;

    public OrderServiceImpl(OrderRepository orderRepository, OrderGameRepository orderGameRepository,
                            UserRepository userRepository, GameRepository gameRepository) {
        this.orderRepository = orderRepository;
        this.orderGameRepository = orderGameRepository;
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
    }

    @Override
    public List<OrderDto> getAllOrders() {
        return orderRepository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    public OrderDto getOrder(Long id) {
        return orderRepository.findById(id).map(this::toDto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Commande introuvable"));
    }

    @Override
    public List<OrderDto> getMyOrders(String email) {
        return orderRepository.findByUserEmailIgnoreCaseOrderByCreatedAtDesc(email).stream()
                .map(this::toDto).toList();
    }

    @Override
    public OrderDto getMyOrder(Long id, String email) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Commande introuvable"));
        if (!order.user.getEmail().equalsIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Commande introuvable");
        }
        return toDto(order);
    }

    @Override
    @Transactional
    public OrderDto createOrder(String email, CreateOrderRequest request) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur introuvable"));
        Order order = new Order();
        order.user = user;
        order.createdAt = new Date();
        Order savedOrder = orderRepository.save(order);

        List<OrderGame> lines = buildLines(savedOrder, request.lines());
        orderGameRepository.saveAll(lines);
        savedOrder.orderGames.addAll(lines);
        return toDto(savedOrder, lines.stream().map(this::toLineDto).toList());
    }

    private List<OrderGame> buildLines(Order order, List<CreateOrderLineRequest> requests) {
        return requests.stream().map(line -> {
            Game game = gameRepository.findById(line.gameId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Jeu introuvable"));
            if (game.price == null) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Le prix de ce jeu n'est pas encore défini");
            }
            OrderGame orderGame = new OrderGame();
            orderGame.order = order;
            orderGame.game = game;
            orderGame.quantity = line.quantity();
            orderGame.unitPrice = game.price;
            return orderGame;
        }).toList();
    }

    @Override
    @Transactional
    public void updateOrder(Long id, UpdateOrderRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Commande introuvable"));
        if (request.delivered() && !request.paid()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Une commande non payée ne peut pas être livrée");
        }
        if (request.lines() != null) {
            if (order.paid || order.delivered) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Les lignes d'une commande payée ou livrée ne peuvent pas être modifiées");
            }
            orderGameRepository.deleteAll(orderGameRepository.findByOrderId(id));
            order.orderGames.clear();
            List<OrderGame> lines = buildLines(order, request.lines());
            orderGameRepository.saveAll(lines);
            order.orderGames.addAll(lines);
        }
        order.paid = request.paid();
        order.delivered = request.delivered();
        order.archived = request.archived();
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Commande introuvable"));
        if (order.paid || order.delivered) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Une commande payée ou livrée ne peut pas être supprimée");
        }
        orderGameRepository.deleteAll(orderGameRepository.findByOrderId(id));
        orderRepository.delete(order);
    }

    private OrderDto toDto(Order order) {
        UserDto user = new UserDto(order.user.getId(), order.user.getName(),
                order.user.getEmail(), order.user.getRole().name());
        List<OrderLineDto> lines = orderGameRepository.findByOrderId(order.id).stream()
                .map(this::toLineDto).toList();
        return toDto(order, lines);
    }

    private OrderDto toDto(Order order, List<OrderLineDto> lines) {
        UserDto user = new UserDto(order.user.getId(), order.user.getName(),
                order.user.getEmail(), order.user.getRole().name());
        return new OrderDto(order.id, user, order.createdAt, order.paid, order.delivered, order.archived, lines);
    }

    private OrderLineDto toLineDto(OrderGame line) {
        Game game = line.game;
        AuthorDto author = game.author == null ? null : new AuthorDto(game.author.id, game.author.name);
        PublisherDto publisher = game.publisher == null ? null : new PublisherDto(game.publisher.id, game.publisher.name);
        GameDto gameDto = new GameDto(game.id, game.name, game.genre, game.editionNumber, game.price, author, publisher);
        return new OrderLineDto(line.id, gameDto, line.quantity, line.unitPrice);
    }
}
