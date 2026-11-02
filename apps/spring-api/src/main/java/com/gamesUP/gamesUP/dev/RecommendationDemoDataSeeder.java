package com.gamesUP.gamesUP.dev;

import com.gamesUP.gamesUP.model.Author;
import com.gamesUP.gamesUP.model.Game;
import com.gamesUP.gamesUP.model.Order;
import com.gamesUP.gamesUP.model.OrderGame;
import com.gamesUP.gamesUP.model.Publisher;
import com.gamesUP.gamesUP.model.Role;
import com.gamesUP.gamesUP.model.User;
import com.gamesUP.gamesUP.repository.AuthorRepository;
import com.gamesUP.gamesUP.repository.GameRepository;
import com.gamesUP.gamesUP.repository.OrderGameRepository;
import com.gamesUP.gamesUP.repository.OrderRepository;
import com.gamesUP.gamesUP.repository.PublisherRepository;
import com.gamesUP.gamesUP.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

/** Crée des données synthétiques locales lorsque le profil recommendation-demo est activé. */
@Component
@Profile("recommendation-demo")
public class RecommendationDemoDataSeeder implements CommandLineRunner {
    public static final String TARGET_EMAIL = "knn.target@gamesup.local";
    private final String demoPassword;
    private static final int GROUP_COUNT = 8;
    private static final int USERS_PER_GROUP = 10;
    private static final int CORE_GAMES_PER_GROUP = 10;
    private static final Logger LOGGER = Logger.getLogger(RecommendationDemoDataSeeder.class.getName());

    private final AuthorRepository authorRepository;
    private final PublisherRepository publisherRepository;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderGameRepository orderGameRepository;
    private final PasswordEncoder passwordEncoder;

    public RecommendationDemoDataSeeder(
            AuthorRepository authorRepository,
            PublisherRepository publisherRepository,
            GameRepository gameRepository,
            UserRepository userRepository,
            OrderRepository orderRepository,
            OrderGameRepository orderGameRepository,
            PasswordEncoder passwordEncoder,
            @Value("${recommendation.demo.password:}") String demoPassword
    ) {
        this.authorRepository = authorRepository;
        this.publisherRepository = publisherRepository;
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.orderGameRepository = orderGameRepository;
        this.passwordEncoder = passwordEncoder;
        this.demoPassword = demoPassword;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.findByEmailIgnoreCase(TARGET_EMAIL).isPresent()) {
            LOGGER.info("Le jeu de données de démonstration existe déjà ; amorçage ignoré.");
            return;
        }
        if (demoPassword.isBlank()) {
            throw new IllegalStateException("Définir RECOMMENDATION_DEMO_PASSWORD avant l'amorçage.");
        }

        List<Author> authors = createAuthors();
        List<Publisher> publishers = createPublishers();
        List<List<Game>> coreGames = new ArrayList<>();
        List<List<Game>> extraGames = new ArrayList<>();
        for (int group = 0; group < GROUP_COUNT; group++) {
            coreGames.add(createCoreGames(group, authors, publishers));
            extraGames.add(createExtraGames(group, coreGames.get(group)));
        }

        String encodedPassword = passwordEncoder.encode(demoPassword);
        for (int group = 0; group < GROUP_COUNT; group++) {
            for (int member = 0; member < USERS_PER_GROUP; member++) {
                User user = createUser(group, member, encodedPassword);
                createPaidOrder(user, group, member, coreGames.get(group), extraGames.get(group));
            }
        }

        LOGGER.info("Jeu de données local créé : 80 clients, 96 jeux et 872 lignes d'achat."
                + " Compte cible : " + TARGET_EMAIL);
    }

    private List<Author> createAuthors() {
        List<Author> authors = new ArrayList<>();
        for (int index = 1; index <= 12; index++) {
            Author author = new Author();
            author.name = String.format(Locale.ROOT, "KNN Demo Author %02d", index);
            authors.add(author);
        }
        return authorRepository.saveAll(authors);
    }

    private List<Publisher> createPublishers() {
        List<Publisher> publishers = new ArrayList<>();
        for (int index = 1; index <= 8; index++) {
            Publisher publisher = new Publisher();
            publisher.name = String.format(Locale.ROOT, "KNN Demo Publisher %02d", index);
            publishers.add(publisher);
        }
        return publisherRepository.saveAll(publishers);
    }

    private List<Game> createCoreGames(int group, List<Author> authors, List<Publisher> publishers) {
        List<Game> games = new ArrayList<>();
        for (int index = 0; index < CORE_GAMES_PER_GROUP; index++) {
            Game game = createGame(group, index, authors, publishers);
            games.add(game);
        }
        return gameRepository.saveAll(games);
    }

    private List<Game> createExtraGames(int group, List<Game> core) {
        List<Game> extraGames = new ArrayList<>();
        for (int index = 0; index < 2; index++) {
            Game anchor = core.get(index * 5);
            Game game = new Game();
            game.name = String.format(Locale.ROOT, "KNN Demo G%02d Trend %02d", group + 1, index + 1);
            game.genre = anchor.genre;
            game.editionNumber = 1;
            game.price = new BigDecimal("29.90");
            game.author = anchor.author;
            game.publisher = anchor.publisher;
            extraGames.add(game);
        }
        return gameRepository.saveAll(extraGames);
    }

    private Game createGame(int group, int index, List<Author> authors, List<Publisher> publishers) {
        Game game = new Game();
        game.name = String.format(Locale.ROOT, "KNN Demo G%02d Core %02d", group + 1, index + 1);
        game.genre = "KNN Demo Genre " + String.format(Locale.ROOT, "%02d", group + 1);
        game.editionNumber = 1 + index % 3;
        game.price = new BigDecimal("14.90").add(new BigDecimal("2.00").multiply(BigDecimal.valueOf(index)));
        game.author = authors.get((group * 3 + index) % authors.size());
        game.publisher = publishers.get((group + index / 2) % publishers.size());
        return game;
    }

    private User createUser(int group, int member, String encodedPassword) {
        User user = new User();
        user.setName(String.format(Locale.ROOT, "KNN Demo User G%02d U%02d", group + 1, member + 1));
        String email = group == 0 && member == 0
                ? TARGET_EMAIL
                : String.format(Locale.ROOT, "knn.demo.g%02d.u%02d@gamesup.local", group + 1, member + 1);
        user.setEmail(email);
        user.setPasswordHash(encodedPassword);
        user.setRole(Role.ROLE_USER);
        return userRepository.save(user);
    }

    private void createPaidOrder(User user, int group, int member, List<Game> core, List<Game> extras) {
        Order order = new Order();
        order.user = user;
        order.createdAt = new Date();
        order.paid = true;
        Order savedOrder = orderRepository.save(order);

        List<OrderGame> lines = new ArrayList<>();
        for (Game game : core) {
            lines.add(createOrderLine(savedOrder, game));
        }
        // Le premier membre du groupe est la cible avec exactement dix achats.
        // Les autres membres ajoutent un titre qui sert de signal de recommandation.
        if (member > 0) {
            lines.add(createOrderLine(savedOrder, extras.get((member - 1) % extras.size())));
        }
        orderGameRepository.saveAll(lines);
        savedOrder.orderGames.addAll(lines);
    }

    private OrderGame createOrderLine(Order order, Game game) {
        OrderGame line = new OrderGame();
        line.order = order;
        line.game = game;
        line.quantity = 1;
        line.unitPrice = game.price;
        return line;
    }
}
