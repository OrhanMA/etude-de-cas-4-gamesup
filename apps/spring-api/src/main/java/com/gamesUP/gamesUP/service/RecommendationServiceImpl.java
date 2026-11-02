package com.gamesUP.gamesUP.service;

import com.gamesUP.gamesUP.client.RecommendationClient;
import com.gamesUP.gamesUP.dto.AuthorDto;
import com.gamesUP.gamesUP.dto.GameDto;
import com.gamesUP.gamesUP.dto.PublisherDto;
import com.gamesUP.gamesUP.dto.PythonCatalogGame;
import com.gamesUP.gamesUP.dto.PythonGameRecommendation;
import com.gamesUP.gamesUP.dto.PythonRecommendationRequest;
import com.gamesUP.gamesUP.dto.PythonRecommendationResponse;
import com.gamesUP.gamesUP.dto.PythonUserInteraction;
import com.gamesUP.gamesUP.dto.RecommendationDto;
import com.gamesUP.gamesUP.dto.RecommendedGameDto;
import com.gamesUP.gamesUP.model.Game;
import com.gamesUP.gamesUP.model.Order;
import com.gamesUP.gamesUP.model.OrderGame;
import com.gamesUP.gamesUP.repository.GameRepository;
import com.gamesUP.gamesUP.repository.OrderRepository;
import com.gamesUP.gamesUP.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class RecommendationServiceImpl implements RecommendationService {
    private final RecommendationClient recommendationClient;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final GameRepository gameRepository;
    private final int recommendationsLimit;

    public RecommendationServiceImpl(
            RecommendationClient recommendationClient,
            UserRepository userRepository,
            OrderRepository orderRepository,
            GameRepository gameRepository,
            @Value("${recommendation.api.limit:5}") int recommendationsLimit
    ) {
        this.recommendationClient = recommendationClient;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.gameRepository = gameRepository;
        this.recommendationsLimit = recommendationsLimit;
    }

    @Override
    public RecommendationDto getRecommendations(String userEmail) {
        var user = userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur introuvable"));
        List<Order> orders = orderRepository.findAllCompletedOrdersWithLines();

        Map<UserGameKey, Integer> interactionCounts = new HashMap<>();
        for (Order order : orders) {
            for (OrderGame line : order.orderGames) {
                interactionCounts.merge(
                        new UserGameKey(order.user.getId(), line.game.id),
                        line.quantity,
                        Integer::sum);
            }
        }

        List<Game> games = gameRepository.findAll();
        List<PythonUserInteraction> interactions = interactionCounts.entrySet().stream()
                .map(entry -> new PythonUserInteraction(
                        entry.getKey().userId(), entry.getKey().gameId(), entry.getValue()))
                .toList();
        List<PythonCatalogGame> catalog = games.stream()
                .map(this::toPythonCatalogGame)
                .toList();
        PythonRecommendationRequest request = new PythonRecommendationRequest(
                user.getId(), interactions, catalog, recommendationsLimit);

        PythonRecommendationResponse response;
        try {
            response = recommendationClient.recommend(request);
        } catch (RestClientException exception) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Le service de recommandation est temporairement indisponible",
                    exception);
        }

        if (response == null
                || !user.getId().equals(response.userId())
                || !java.util.Set.of("knn-hybrid", "cold-start", "no-candidates")
                        .contains(response.strategy())
                || response.recommendations() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Le service de recommandation a retourné une réponse invalide");
        }

        Map<Long, Game> gamesById = games.stream().collect(
                java.util.stream.Collectors.toMap(game -> game.id, game -> game));
        List<RecommendedGameDto> recommendations = response.recommendations().stream()
                .filter(java.util.Objects::nonNull)
                .map(item -> toRecommendation(item, gamesById))
                .filter(java.util.Objects::nonNull)
                .toList();
        return new RecommendationDto(response.strategy(), recommendations);
    }

    private PythonCatalogGame toPythonCatalogGame(Game game) {
        return new PythonCatalogGame(
                game.id,
                game.name,
                game.genre,
                game.editionNumber,
                game.price,
                game.author == null ? null : game.author.name,
                game.publisher == null ? null : game.publisher.name);
    }

    private RecommendedGameDto toRecommendation(
            PythonGameRecommendation recommendation,
            Map<Long, Game> gamesById
    ) {
        Game game = gamesById.get(recommendation.gameId());
        if (game == null) {
            return null;
        }
        AuthorDto author = game.author == null ? null : new AuthorDto(game.author.id, game.author.name);
        PublisherDto publisher = game.publisher == null
                ? null
                : new PublisherDto(game.publisher.id, game.publisher.name);
        GameDto gameDto = new GameDto(
                game.id, game.name, game.genre, game.editionNumber, game.price, author, publisher);
        return new RecommendedGameDto(gameDto, recommendation.score());
    }

    private record UserGameKey(Long userId, Long gameId) {}
}
