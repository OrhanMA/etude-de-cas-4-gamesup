package com.gamesUP.gamesUP.service;

import com.gamesUP.gamesUP.client.RecommendationClient;
import com.gamesUP.gamesUP.dto.PythonGameRecommendation;
import com.gamesUP.gamesUP.dto.PythonRecommendationRequest;
import com.gamesUP.gamesUP.dto.PythonRecommendationResponse;
import com.gamesUP.gamesUP.model.Game;
import com.gamesUP.gamesUP.model.Order;
import com.gamesUP.gamesUP.model.OrderGame;
import com.gamesUP.gamesUP.model.User;
import com.gamesUP.gamesUP.repository.GameRepository;
import com.gamesUP.gamesUP.repository.OrderRepository;
import com.gamesUP.gamesUP.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceImplTest {
    @Mock private RecommendationClient recommendationClient;
    @Mock private UserRepository userRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private GameRepository gameRepository;

    private RecommendationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new RecommendationServiceImpl(
                recommendationClient, userRepository, orderRepository, gameRepository, 5);
    }

    @Test
    void envoieAuPythonLeCatalogueEtLesAchatsPayesDeTousLesClients() {
        User target = user(1L, "target@example.test");
        User other = user(2L, "other@example.test");
        when(userRepository.findByEmailIgnoreCase(target.getEmail())).thenReturn(Optional.of(target));
        when(orderRepository.findAllCompletedOrdersWithLines()).thenReturn(List.of(
                order(target, true, line(1L, 3)),
                order(other, true, line(2L, 1))));
        Game ownedGame = game(1L, "Jeu possédé", "Stratégie");
        Game recommendedGame = game(2L, "Jeu conseillé", "Stratégie");
        when(gameRepository.findAll()).thenReturn(List.of(ownedGame, recommendedGame));
        when(recommendationClient.recommend(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new PythonRecommendationResponse(
                        target.getId(), "knn-hybrid",
                        List.of(new PythonGameRecommendation(2L, "Jeu conseillé", "Stratégie", 0.91))));

        var result = service.getRecommendations(target.getEmail());

        ArgumentCaptor<PythonRecommendationRequest> requestCaptor =
                ArgumentCaptor.forClass(PythonRecommendationRequest.class);
        verify(recommendationClient).recommend(requestCaptor.capture());
        PythonRecommendationRequest sent = requestCaptor.getValue();
        assertThat(sent.userId()).isEqualTo(1L);
        assertThat(sent.interactions()).containsExactlyInAnyOrder(
                new com.gamesUP.gamesUP.dto.PythonUserInteraction(1L, 1L, 3),
                new com.gamesUP.gamesUP.dto.PythonUserInteraction(2L, 2L, 1));
        assertThat(sent.catalog()).extracting("gameId").containsExactly(1L, 2L);
        assertThat(sent.limit()).isEqualTo(5);
        assertThat(result.strategy()).isEqualTo("knn-hybrid");
        assertThat(result.recommendations()).hasSize(1);
        assertThat(result.recommendations().getFirst().game().id()).isEqualTo(2L);
        assertThat(result.recommendations().getFirst().score()).isEqualTo(0.91);
    }

    @Test
    void transformeUneIndisponibiliteDePythonEnErreur503() {
        User target = user(1L, "target@example.test");
        when(userRepository.findByEmailIgnoreCase(target.getEmail())).thenReturn(Optional.of(target));
        when(orderRepository.findAllCompletedOrdersWithLines()).thenReturn(List.of());
        when(gameRepository.findAll()).thenReturn(List.of());
        when(recommendationClient.recommend(org.mockito.ArgumentMatchers.any()))
                .thenThrow(new ResourceAccessException("Python unavailable"));

        assertThatThrownBy(() -> service.getRecommendations(target.getEmail()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("503 SERVICE_UNAVAILABLE");
    }

    @Test
    void refuseUneReponsePythonSansListeDeRecommandations() {
        User target = user(1L, "target@example.test");
        when(userRepository.findByEmailIgnoreCase(target.getEmail())).thenReturn(Optional.of(target));
        when(orderRepository.findAllCompletedOrdersWithLines()).thenReturn(List.of());
        when(gameRepository.findAll()).thenReturn(List.of());
        when(recommendationClient.recommend(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new PythonRecommendationResponse(target.getId(), "knn-hybrid", null));

        assertThatThrownBy(() -> service.getRecommendations(target.getEmail()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("502 BAD_GATEWAY");
    }

    private static User user(Long id, String email) {
        User user = new User();
        user.setId(id);
        user.setName("Client test");
        user.setEmail(email);
        return user;
    }

    private static Game game(Long id, String name, String genre) {
        Game game = new Game();
        game.id = id;
        game.name = name;
        game.genre = genre;
        game.editionNumber = 1;
        game.price = new BigDecimal("20.00");
        return game;
    }

    private static Order order(User user, boolean paid, OrderGame... lines) {
        Order order = new Order();
        order.user = user;
        order.paid = paid;
        order.orderGames.addAll(List.of(lines));
        for (OrderGame line : lines) line.order = order;
        return order;
    }

    private static OrderGame line(Long gameId, int quantity) {
        OrderGame line = new OrderGame();
        line.game = game(gameId, "Jeu " + gameId, "Stratégie");
        line.quantity = quantity;
        return line;
    }
}
