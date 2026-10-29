package com.gamesUP.gamesUP.model;

import java.math.BigDecimal;
import java.util.Date;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
class RelationsJpaTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    void retrouveLesRelationsDansLesDeuxSens() {
        Author author = new Author();
        author.name = "Auteur";
        Publisher publisher = new Publisher();
        publisher.name = "Éditeur";
        User user = new User();
        ReflectionTestUtils.setField(user, "name", "Client");
        ReflectionTestUtils.setField(user, "email", "client@example.test");
        ReflectionTestUtils.setField(user, "passwordHash", "empreinte");

        Game game = new Game();
        game.name = "Catan";
        game.genre = "Stratégie";
        game.editionNumber = 1;
        game.author = author;
        game.publisher = publisher;

        Order order = new Order();
        order.user = user;
        order.createdAt = new Date();

        OrderGame orderGame = new OrderGame();
        orderGame.order = order;
        orderGame.game = game;
        orderGame.quantity = 1;
        orderGame.unitPrice = new BigDecimal("29.90");

        entityManager.persist(author);
        entityManager.persist(publisher);
        entityManager.persist(user);
        entityManager.persist(game);
        entityManager.persist(order);
        entityManager.persist(orderGame);
        entityManager.flush();

        Long userId = (Long) ReflectionTestUtils.getField(user, "id");
        Long authorId = author.id;
        Long publisherId = publisher.id;
        Long gameId = game.id;
        Long orderId = order.id;
        Long orderGameId = orderGame.id;
        entityManager.clear();

        assertThat(entityManager.find(Author.class, authorId).games)
                .extracting(relatedGame -> relatedGame.id).containsExactly(gameId);
        assertThat(entityManager.find(Publisher.class, publisherId).games)
                .extracting(relatedGame -> relatedGame.id).containsExactly(gameId);
        assertThat(entityManager.find(User.class, userId).orders)
                .extracting(relatedOrder -> relatedOrder.id).containsExactly(orderId);
        assertThat(entityManager.find(Order.class, orderId).orderGames)
                .extracting(relatedLine -> relatedLine.id).containsExactly(orderGameId);
        assertThat(entityManager.find(Game.class, gameId).orderGames)
                .extracting(relatedLine -> relatedLine.id).containsExactly(orderGameId);

        OrderGame loadedLine = entityManager.find(OrderGame.class, orderGameId);
        assertThat(loadedLine.order.id).isEqualTo(orderId);
        assertThat(loadedLine.game.id).isEqualTo(gameId);
        assertThat(loadedLine.game.author.id).isEqualTo(authorId);
        assertThat(loadedLine.game.publisher.id).isEqualTo(publisherId);
    }
}
