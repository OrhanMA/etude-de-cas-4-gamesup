package com.gamesUP.gamesUP.service;

import com.gamesUP.gamesUP.dto.GameDto;
import com.gamesUP.gamesUP.dto.CreateGameDto;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

public interface GameService {
    List<GameDto> getAllGames();
    GameDto getGameById(Long id) throws ResponseStatusException;
    List<GameDto> searchGames(String query) throws ResponseStatusException;
    void addGame(CreateGameDto game);
    void updateGame(Long id, CreateGameDto game) throws ResponseStatusException;
    void deleteGame(Long id) throws ResponseStatusException;
}

