package com.gamesUP.gamesUP.controller;

import java.util.List;

import com.gamesUP.gamesUP.dto.GameDto;
import com.gamesUP.gamesUP.dto.CreateGameDto;
import com.gamesUP.gamesUP.service.GameService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/search")
    public List<GameDto> searchGames(@RequestParam String query) {
        return gameService.searchGames(query);
    }

    @GetMapping("/{id}")
    public GameDto getGameById(@PathVariable Long id) {
        return gameService.getGameById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public void addGame(@Valid @RequestBody CreateGameDto game) {
        gameService.addGame(game);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateGame(@PathVariable Long id, @Valid @RequestBody CreateGameDto game) {
        gameService.updateGame(id, game);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGame(@PathVariable Long id) {
        gameService.deleteGame(id);
    }
}
