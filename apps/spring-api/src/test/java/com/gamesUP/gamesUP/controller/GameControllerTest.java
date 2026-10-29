package com.gamesUP.gamesUP.controller;

import java.util.List;

import com.gamesUP.gamesUP.model.Author;
import com.gamesUP.gamesUP.model.Game;
import com.gamesUP.gamesUP.model.Publisher;
import com.gamesUP.gamesUP.repository.GameRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GameController.class)
class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GameRepository gameRepository;

    @Test
    @WithMockUser
    void retourneLesJeuxDuCatalogue() throws Exception {
        Game game = new Game();
        game.id = 1L;
        game.name = "Catan";
        game.genre = "Stratégie";
        game.editionNumber = 1;
        Author author = new Author();
        author.id = 2L;
        author.name = "Auteur";
        author.games.add(game);
        game.author = author;
        Publisher publisher = new Publisher();
        publisher.id = 3L;
        publisher.name = "Éditeur";
        publisher.games.add(game);
        game.publisher = publisher;
        when(gameRepository.findAll()).thenReturn(List.of(game));

        mockMvc.perform(get("/games"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Catan"))
                .andExpect(jsonPath("$[0].author.name").value("Auteur"))
                .andExpect(jsonPath("$[0].publisher.name").value("Éditeur"))
                .andExpect(jsonPath("$[0].author.games").doesNotExist())
                .andExpect(jsonPath("$[0].publisher.games").doesNotExist())
                .andExpect(jsonPath("$[0].orderGames").doesNotExist());
    }

    @Test
    void refuseUneRequeteAnonyme() throws Exception {
        mockMvc.perform(get("/games").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void enregistreUnJeuValide() throws Exception {
        mockMvc.perform(post("/games")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Catan","genre":"Stratégie","editionNumber":1}
                                """))
                .andExpect(status().isOk());

        verify(gameRepository).save(any(Game.class));
    }

    @Test
    @WithMockUser
    void rejetteUnJeuInvalideSansEnregistrement() throws Exception {
        mockMvc.perform(post("/games")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","genre":"Stratégie","editionNumber":1}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(gameRepository);
    }

    @Test
    @WithMockUser
    void refuseLaCreationSansJetonCsrf() throws Exception {
        mockMvc.perform(post("/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Catan","genre":"Stratégie","editionNumber":1}
                                """))
                .andExpect(status().isForbidden());

        verifyNoInteractions(gameRepository);
    }
}
