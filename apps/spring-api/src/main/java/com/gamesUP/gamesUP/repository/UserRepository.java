package com.gamesUP.gamesUP.repository;

import com.gamesUP.gamesUP.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // Optional car l'email peut ne correspondre à aucun utilisateur
    Optional<User> findByEmail(String email);

    Optional<User> findByEmailIgnoreCase(String email);
}
