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

    private GameDto toDto(Game game) {
        AuthorDto author = game.author == null
                ? null
                : new AuthorDto(game.author.id, game.author.name);
        PublisherDto publisher = game.publisher == null
                ? null
                : new PublisherDto(game.publisher.id, game.publisher.name);

        return new GameDto(game.id, game.name, game.genre, game.editionNumber, author, publisher);
    }
}
