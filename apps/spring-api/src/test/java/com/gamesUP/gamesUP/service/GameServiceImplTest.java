package com.gamesUP.gamesUP.service;

import com.gamesUP.gamesUP.dto.GameDto;
import com.gamesUP.gamesUP.dto.CreateGameDto;
import com.gamesUP.gamesUP.dto.AuthorDto;
import com.gamesUP.gamesUP.dto.PublisherDto;
import com.gamesUP.gamesUP.model.Author;
import com.gamesUP.gamesUP.model.Game;
import com.gamesUP.gamesUP.model.Publisher;
import com.gamesUP.gamesUP.repository.GameRepository;
import com.gamesUP.gamesUP.repository.AuthorRepository;
import com.gamesUP.gamesUP.repository.PublisherRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameServiceImplTest {
    @Mock
    private GameRepository gameRepository;
    @Mock
    private AuthorRepository authorRepository;
    @Mock
    private PublisherRepository publisherRepository;

    private GameServiceImpl service() {
        return new GameServiceImpl(gameRepository, authorRepository, publisherRepository);
    }

    @Test
    void retourneLesJeuxDuRepository() {
        Game game = new Game();
        game.id = 1L;
        game.name = "Catan";
        game.genre = "Stratégie";
        game.editionNumber = 1;
        game.author = new Author();
        game.author.id = 2L;
        game.author.name = "Auteur";
        game.publisher = new Publisher();
        game.publisher.id = 3L;
        game.publisher.name = "Éditeur";
        GameDto gameDto = new GameDto(1L, "Catan", "Stratégie", 1,
                new AuthorDto(2L, "Auteur"), new PublisherDto(3L, "Éditeur"));
        when(gameRepository.findAll()).thenReturn(List.of(game));

        assertThat(service().getAllGames()).containsExactly(gameDto);
    }

    @Test
    void retourneUnJeuSansAuteurNiEditeur() {
        Game game = new Game();
        game.id = 1L;
        game.name = "Catan";
        game.genre = "Stratégie";
        game.editionNumber = 1;
        when(gameRepository.findAll()).thenReturn(List.of(game));

        assertThat(service().getAllGames())
                .containsExactly(new GameDto(1L, "Catan", "Stratégie", 1, null, null));
    }

    @Test
    void enregistreLeJeuDansLeRepository() {
        Author author = new Author();
        Publisher publisher = new Publisher();
        when(authorRepository.findById(2L)).thenReturn(Optional.of(author));
        when(publisherRepository.findById(3L)).thenReturn(Optional.of(publisher));

        service().addGame(new CreateGameDto("Catan", "Stratégie", 1, 2L, 3L));

        ArgumentCaptor<Game> saved = ArgumentCaptor.forClass(Game.class);
        verify(gameRepository).save(saved.capture());
        assertThat(saved.getValue().id).isNull();
        assertThat(saved.getValue().name).isEqualTo("Catan");
        assertThat(saved.getValue().genre).isEqualTo("Stratégie");
        assertThat(saved.getValue().editionNumber).isEqualTo(1);
        assertThat(saved.getValue().author).isSameAs(author);
        assertThat(saved.getValue().publisher).isSameAs(publisher);
    }

    @Test
    void enregistreSansAuteurNiEditeur() {
        service().addGame(new CreateGameDto("Catan", "Stratégie", 1, null, null));

        ArgumentCaptor<Game> saved = ArgumentCaptor.forClass(Game.class);
        verify(gameRepository).save(saved.capture());
        assertThat(saved.getValue().author).isNull();
        assertThat(saved.getValue().publisher).isNull();
        verifyNoInteractions(authorRepository, publisherRepository);
    }

    @Test
    void refuseUnAuteurInexistant() {
        when(authorRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service().addGame(new CreateGameDto("Catan", "Stratégie", 1, 2L, null)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Auteur introuvable");
        verifyNoInteractions(gameRepository);
    }

    @Test
    void refuseUnEditeurInexistant() {
        when(publisherRepository.findById(3L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service().addGame(new CreateGameDto("Catan", "Stratégie", 1, null, 3L)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Éditeur introuvable");
        verifyNoInteractions(gameRepository);
    }
}
