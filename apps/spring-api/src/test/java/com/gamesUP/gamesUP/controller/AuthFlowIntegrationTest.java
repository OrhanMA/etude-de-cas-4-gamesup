package com.gamesUP.gamesUP.controller;

import com.gamesUP.gamesUP.model.Game;
import com.gamesUP.gamesUP.model.Author;
import com.gamesUP.gamesUP.model.Order;
import com.gamesUP.gamesUP.model.Publisher;
import com.gamesUP.gamesUP.model.Role;
import com.gamesUP.gamesUP.model.User;
import com.gamesUP.gamesUP.dto.RecommendationDto;
import com.gamesUP.gamesUP.repository.AuthorRepository;
import com.gamesUP.gamesUP.repository.GameRepository;
import com.gamesUP.gamesUP.repository.OrderRepository;
import com.gamesUP.gamesUP.repository.PublisherRepository;
import com.gamesUP.gamesUP.repository.UserRepository;
import com.gamesUP.gamesUP.service.RecommendationService;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:auth-flow;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "app.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY="
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private PublisherRepository publisherRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private RecommendationService recommendationService;

    @Test
    void inscriptionConnexionPuisAccesAvecBearerReussissent() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Alice","email":"alice.auth@example.test","password":"secret123"}
                                """))
                .andExpect(status().isOk());

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"alice.auth@example.test","password":"secret123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andReturn();

        String token = JsonPath.read(
                loginResult.getResponse().getContentAsString(),
                "$.accessToken"
        );

        mockMvc.perform(get("/games")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void refuseUneRequeteAnonymeVersLeCatalogue() throws Exception {
        mockMvc.perform(get("/games"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unClientAuthentifiePeutDemanderSesRecommandations() throws Exception {
        String email = "reader.recommendations@example.test";
        String token = registerAndLogin(email, Role.ROLE_USER);
        when(recommendationService.getRecommendations(email))
                .thenReturn(new RecommendationDto("knn-hybrid", java.util.List.of()));

        mockMvc.perform(get("/api/recommendations")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.strategy").value("knn-hybrid"))
                .andExpect(jsonPath("$.recommendations").isArray());

        verify(recommendationService).getRecommendations(email);
    }

    @Test
    void retourneUnJeuParSonIdentifiantEtPermetLaRecherche() throws Exception {
        Game game = createGame();
        String token = registerAndLogin("reader.games@example.test", Role.ROLE_USER);

        mockMvc.perform(get("/games/{id}", game.id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jeu de test"));

        mockMvc.perform(get("/games/search").queryParam("query", "stratégie")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(game.id));
    }

    @Test
    void unAdministrateurPeutModifierUnJeu() throws Exception {
        Game game = createGame();
        String token = registerAndLogin("admin.update@example.test", Role.ROLE_ADMIN);

        mockMvc.perform(put("/games/{id}", game.id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Jeu modifié","genre":"Familial","editionNumber":2,"price":24.50}
                                """))
                .andExpect(status().isNoContent());

        assertThat(gameRepository.findById(game.id)).get()
                .extracting(saved -> saved.name).isEqualTo("Jeu modifié");
    }

    @Test
    void unUtilisateurNePeutPasCreerUnJeu() throws Exception {
        String token = registerAndLogin("user.create@example.test", Role.ROLE_USER);

        mockMvc.perform(post("/games")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Jeu interdit","genre":"Familial","editionNumber":1,"price":24.50}
                                """))
                .andExpect(status().isForbidden());

        assertThat(gameRepository.findAll()).isEmpty();
    }

    @Test
    void unAdministrateurPeutCreerUnJeu() throws Exception {
        String token = registerAndLogin("admin.create@example.test", Role.ROLE_ADMIN);

        mockMvc.perform(post("/games")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Jeu admin","genre":"Familial","editionNumber":1,"price":24.50}
                                """))
                .andExpect(status().isOk());

        assertThat(gameRepository.findAll()).hasSize(1);
    }

    @Test
    void unUtilisateurNePeutPasModifierUnJeu() throws Exception {
        Game game = createGame();
        String token = registerAndLogin("user.update@example.test", Role.ROLE_USER);

        mockMvc.perform(put("/games/{id}", game.id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Tentative","genre":"Familial","editionNumber":2,"price":24.50}
                                """))
                .andExpect(status().isForbidden());

        assertThat(gameRepository.findById(game.id)).get()
                .extracting(saved -> saved.name).isEqualTo("Jeu de test");
    }

    @Test
    void refuseUneConnexionAvecUnMauvaisMotDePasse() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Bob","email":"bob.auth@example.test","password":"secret123"}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"bob.auth@example.test","password":"incorrect"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refuseUneInscriptionAvecUnEmailDejaUtiliseSansTenirCompteDeLaCasse() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Alice","email":"alice.duplicate@example.test","password":"secret123"}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Alice bis","email":"ALICE.DUPLICATE@example.test","password":"secret123"}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void refuseLaSuppressionAnonyme() throws Exception {
        Game game = createGame();

        mockMvc.perform(delete("/games/{id}", game.id))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refuseLaSuppressionAvecUnCompteUtilisateur() throws Exception {
        Game game = createGame();
        String token = registerAndLogin("user.delete@example.test", Role.ROLE_USER);

        mockMvc.perform(delete("/games/{id}", game.id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden());

        assertThat(gameRepository.existsById(game.id)).isTrue();
    }

    @Test
    void unAdministrateurPeutSupprimerUnJeu() throws Exception {
        Game game = createGame();
        String token = registerAndLogin("admin.delete@example.test", Role.ROLE_ADMIN);

        mockMvc.perform(delete("/games/{id}", game.id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNoContent());

        assertThat(gameRepository.existsById(game.id)).isFalse();
    }

    @Test
    void suppressionParUnAdministrateurRetourne404SiLeJeuNExistePas() throws Exception {
        String token = registerAndLogin("admin.missing@example.test", Role.ROLE_ADMIN);

        mockMvc.perform(delete("/games/{id}", Long.MAX_VALUE)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void unAdministrateurPeutGererLesAuteursEtUnUtilisateurNePeutPasLesModifier() throws Exception {
        String userToken = registerAndLogin("user.authors@example.test", Role.ROLE_USER);
        mockMvc.perform(post("/authors")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Auteur refusé\"}"))
                .andExpect(status().isForbidden());

        String adminToken = registerAndLogin("admin.authors@example.test", Role.ROLE_ADMIN);
        MvcResult created = mockMvc.perform(post("/authors")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Auteur test\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Auteur test"))
                .andReturn();
        Number authorId = JsonPath.read(created.getResponse().getContentAsString(), "$.id");
        Long id = authorId.longValue();

        mockMvc.perform(get("/authors")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id));
        mockMvc.perform(put("/authors/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Auteur modifié\"}"))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/authors/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Auteur modifié"));
        mockMvc.perform(delete("/authors/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void unAdministrateurPeutGererLesEditeurs() throws Exception {
        String adminToken = registerAndLogin("admin.publishers@example.test", Role.ROLE_ADMIN);
        MvcResult created = mockMvc.perform(post("/publishers")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Éditeur test\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        Number publisherId = JsonPath.read(created.getResponse().getContentAsString(), "$.id");
        Long id = publisherId.longValue();

        mockMvc.perform(get("/publishers")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id));
        mockMvc.perform(put("/publishers/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Éditeur modifié\"}"))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/publishers/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Éditeur modifié"));
        mockMvc.perform(delete("/publishers/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void lesClientsEtCommandesSontConsultablesParUnAdministrateurUniquement() throws Exception {
        String adminToken = registerAndLogin("admin.read@example.test", Role.ROLE_ADMIN);
        User admin = userRepository.findByEmailIgnoreCase("admin.read@example.test").orElseThrow();
        Order order = new Order();
        order.user = admin;
        order.createdAt = new Date();
        Order savedOrder = orderRepository.save(order);

        mockMvc.perform(get("/api/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].passwordHash").doesNotExist());
        mockMvc.perform(get("/api/orders/{id}", savedOrder.id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedOrder.id))
                .andExpect(jsonPath("$.user.email").value("admin.read@example.test"));
        mockMvc.perform(get("/api/orders")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(savedOrder.id));

        String userToken = registerAndLogin("user.read@example.test", Role.ROLE_USER);
        mockMvc.perform(get("/api/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/orders")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void unAdministrateurPeutMettreAJourEtSupprimerUnClientSansCommandes() throws Exception {
        registerAndLogin("client.manage@example.test", Role.ROLE_USER);
        User client = userRepository.findByEmailIgnoreCase("client.manage@example.test").orElseThrow();
        String adminToken = registerAndLogin("admin.clients@example.test", Role.ROLE_ADMIN);

        mockMvc.perform(put("/api/users/{id}", client.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Client renommé","email":"client.renamed@example.test"}
                                """))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/users/{id}", client.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("client.renamed@example.test"));
        mockMvc.perform(delete("/api/users/{id}", client.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void unClientCommandeEtNeVoitQueSesCommandesPuisUnAdminGereLeStatut() throws Exception {
        String ownerToken = registerAndLogin("order.owner@example.test", Role.ROLE_USER);
        User owner = userRepository.findByEmailIgnoreCase("order.owner@example.test").orElseThrow();
        String adminToken = registerAndLogin("admin.orders@example.test", Role.ROLE_ADMIN);
        Long adminId = userRepository.findByEmailIgnoreCase("admin.orders@example.test").orElseThrow().getId();
        Game game = createGame();
        String createOrderJson = """
                {"userId":%d,"lines":[{"gameId":%d,"quantity":2,"unitPrice":0.01}]}
                """.formatted(adminId, game.id);

        MvcResult created = mockMvc.perform(post("/api/orders")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createOrderJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.lines[0].quantity").value(2))
                .andExpect(jsonPath("$.lines[0].unitPrice").value(12.50))
                .andExpect(jsonPath("$.user.email").value("order.owner@example.test"))
                .andReturn();
        Number orderId = JsonPath.read(created.getResponse().getContentAsString(), "$.id");
        Long id = orderId.longValue();

        game.price = new BigDecimal("50.00");
        gameRepository.save(game);

        mockMvc.perform(get("/api/orders/mine")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id))
                .andExpect(jsonPath("$[0].lines[0].unitPrice").value(12.50));
        mockMvc.perform(get("/api/orders/mine/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ownerToken))
                .andExpect(status().isOk());

        String otherUserToken = registerAndLogin("other.owner@example.test", Role.ROLE_USER);
        mockMvc.perform(get("/api/orders/mine/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + otherUserToken))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/orders")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ownerToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/orders/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"paid\":true,\"delivered\":false,\"archived\":false,"
                                + "\"lines\":[{\"gameId\":" + game.id
                                + ",\"quantity\":3}]}"))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/orders/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lines[0].quantity").value(3))
                .andExpect(jsonPath("$.lines[0].unitPrice").value(50.00));
        mockMvc.perform(delete("/api/orders/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isConflict());

        mockMvc.perform(delete("/api/orders/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ownerToken))
                .andExpect(status().isForbidden());
        assertThat(orderRepository.findById(id)).isPresent();
    }

    @Test
    void refuseDeSupprimerUnAuteurOuEditeurLieAUnJeu() throws Exception {
        Author author = new Author();
        author.name = "Auteur lié";
        author = authorRepository.save(author);
        Publisher publisher = new Publisher();
        publisher.name = "Éditeur lié";
        publisher = publisherRepository.save(publisher);
        Game game = createGame();
        game.author = author;
        game.publisher = publisher;
        gameRepository.save(game);
        String adminToken = registerAndLogin("admin.references@example.test", Role.ROLE_ADMIN);

        mockMvc.perform(delete("/authors/{id}", author.id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isConflict());
        mockMvc.perform(delete("/publishers/{id}", publisher.id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isConflict());
    }

    private Game createGame() {
        Game game = new Game();
        game.name = "Jeu de test";
        game.genre = "Stratégie";
        game.editionNumber = 1;
        game.price = new java.math.BigDecimal("12.50");
        return gameRepository.save(game);
    }

    private String registerAndLogin(String email, Role role) throws Exception {
        User user = new User();
        user.setName("Utilisateur de test");
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode("secret123"));
        user.setRole(role);
        userRepository.save(user);

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"secret123\"}"))
                .andExpect(status().isOk())
                .andReturn();

        return JsonPath.read(loginResult.getResponse().getContentAsString(), "$.accessToken");
    }
}
