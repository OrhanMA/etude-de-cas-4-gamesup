package com.gamesUP.gamesUP.controller;

import com.gamesUP.gamesUP.model.Game;
import com.gamesUP.gamesUP.model.Role;
import com.gamesUP.gamesUP.model.User;
import com.gamesUP.gamesUP.repository.GameRepository;
import com.gamesUP.gamesUP.repository.UserRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;

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
    private PasswordEncoder passwordEncoder;

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
                                {"name":"Jeu modifié","genre":"Familial","editionNumber":2}
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
                                {"name":"Jeu interdit","genre":"Familial","editionNumber":1}
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
                                {"name":"Jeu admin","genre":"Familial","editionNumber":1}
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
                                {"name":"Tentative","genre":"Familial","editionNumber":2}
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

    private Game createGame() {
        Game game = new Game();
        game.name = "Jeu de test";
        game.genre = "Stratégie";
        game.editionNumber = 1;
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
