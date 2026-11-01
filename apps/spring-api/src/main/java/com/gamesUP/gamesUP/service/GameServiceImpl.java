package com.gamesUP.gamesUP.service;

import com.gamesUP.gamesUP.dto.AuthorDto;
import com.gamesUP.gamesUP.dto.CreateGameDto;
import com.gamesUP.gamesUP.dto.GameDto;
import com.gamesUP.gamesUP.dto.PublisherDto;
import com.gamesUP.gamesUP.model.Game;
import com.gamesUP.gamesUP.repository.GameRepository;
import com.gamesUP.gamesUP.repository.AuthorRepository;
import com.gamesUP.gamesUP.repository.PublisherRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;
    private final AuthorRepository authorRepository;
    private final PublisherRepository publisherRepository;

    public GameServiceImpl(GameRepository gameRepository, AuthorRepository authorRepository,
                           PublisherRepository publisherRepository) {
        this.gameRepository = gameRepository;
        this.authorRepository = authorRepository;
        this.publisherRepository = publisherRepository;
    }

    @Override
    public void addGame(CreateGameDto gameDto) {
        Game game = new Game();
        game.name = gameDto.name();
        game.genre = gameDto.genre();
        game.editionNumber = gameDto.editionNumber();
        game.price = gameDto.price();
        if (gameDto.authorId() != null) {
            game.author = authorRepository.findById(gameDto.authorId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Auteur introuvable"));
        }
        if (gameDto.publisherId() != null) {
            game.publisher = publisherRepository.findById(gameDto.publisherId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Éditeur introuvable"));
        }

        gameRepository.save(game);
    }

    @Override
    public List<GameDto> getAllGames() {
        return gameRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public GameDto getGameById(Long id) {
        return gameRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Jeu introuvable"));
    }

    @Override
    public List<GameDto> searchGames(String query) {
        if (query == null || query.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le terme de recherche est obligatoire");
        }

        String term = query.trim();
        return gameRepository.findByNameContainingIgnoreCaseOrGenreContainingIgnoreCase(term, term).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public void updateGame(Long id, CreateGameDto gameDto) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Jeu introuvable"));

        game.name = gameDto.name();
        game.genre = gameDto.genre();
        game.editionNumber = gameDto.editionNumber();
        game.price = gameDto.price();
        game.author = gameDto.authorId() == null
                ? null
                : authorRepository.findById(gameDto.authorId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Auteur introuvable"));
        game.publisher = gameDto.publisherId() == null
                ? null
                : publisherRepository.findById(gameDto.publisherId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Éditeur introuvable"));

        gameRepository.save(game);
    }

    private GameDto toDto(Game game) {
        AuthorDto author = game.author == null
                ? null
                : new AuthorDto(game.author.id, game.author.name);
        PublisherDto publisher = game.publisher == null
                ? null
                : new PublisherDto(game.publisher.id, game.publisher.name);

        return new GameDto(game.id, game.name, game.genre, game.editionNumber, game.price, author, publisher);
    }

    @Override
    public void deleteGame(Long id) throws ResponseStatusException {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Jeu introuvable"));
        gameRepository.delete(game);
    }
}
