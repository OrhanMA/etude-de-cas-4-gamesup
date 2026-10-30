package com.gamesUP.gamesUP.controller;

import java.util.List;

import com.gamesUP.gamesUP.dto.GameDto;
import com.gamesUP.gamesUP.dto.CreateGameDto;
import com.gamesUP.gamesUP.service.GameService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/games")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping
    public List<GameDto> getAllGames() {
        return gameService.getAllGames();
    }

    @PostMapping
    public void addGame(@Valid @RequestBody CreateGameDto game) {
        gameService.addGame(game);
    }
}
