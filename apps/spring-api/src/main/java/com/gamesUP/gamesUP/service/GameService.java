package com.gamesUP.gamesUP.service;

import com.gamesUP.gamesUP.dto.GameDto;
import com.gamesUP.gamesUP.dto.CreateGameDto;

import java.util.List;

public interface GameService {
    List<GameDto> getAllGames();
    void addGame(CreateGameDto game);
}

