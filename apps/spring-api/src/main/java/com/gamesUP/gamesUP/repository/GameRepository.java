package com.gamesUP.gamesUP.repository;

import com.gamesUP.gamesUP.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GameRepository extends JpaRepository<Game, Long> {
    List<Game> findByNameContainingIgnoreCaseOrGenreContainingIgnoreCase(String name, String genre);
    boolean existsByAuthorId(Long authorId);
    boolean existsByPublisherId(Long publisherId);
}
