package com.gamesUP.gamesUP.controller;

import java.util.List;

import com.gamesUP.gamesUP.dto.AuthorDto;
import com.gamesUP.gamesUP.dto.CreateGameDto;
import com.gamesUP.gamesUP.dto.GameDto;
import com.gamesUP.gamesUP.dto.PublisherDto;
import com.gamesUP.gamesUP.service.GameService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
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
    private GameService gameService;

    @Test
    @WithMockUser
    void retourneLesJeuxDuCatalogue() throws Exception {
        GameDto game = new GameDto(1L, "Catan", "Stratégie", 1,
                new AuthorDto(2L, "Auteur"), new PublisherDto(3L, "Éditeur"));
        when(gameService.getAllGames()).thenReturn(List.of(game));

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

        verify(gameService).addGame(new CreateGameDto("Catan", "Stratégie", 1, null, null));
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

        verifyNoInteractions(gameService);
    }

    @Test
    @WithMockUser
    void rejetteUnIdentifiantAuteurInvalide() throws Exception {
        mockMvc.perform(post("/games")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Catan","genre":"Stratégie","editionNumber":1,"authorId":0}
                                """))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(gameService);
    }

    @Test
    @WithMockUser
    void rejetteUnAuteurInexistant() throws Exception {
        CreateGameDto request = new CreateGameDto("Catan", "Stratégie", 1, 2L, null);
        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Auteur introuvable"))
                .when(gameService).addGame(request);

        mockMvc.perform(post("/games")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Catan","genre":"Stratégie","editionNumber":1,"authorId":2}
                                """))
                .andExpect(status().isBadRequest());
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

        verifyNoInteractions(gameService);
    }
}
