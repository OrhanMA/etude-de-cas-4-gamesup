package com.gamesUP.gamesUP.repository;

import com.gamesUP.gamesUP.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<Game, Long> {
}
