package com.gamesUP.gamesUP.controller;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

import org.springframework.web.bind.annotation.*;

import com.gamesUP.gamesUP.model.Game;

public class GameController {

    private final DataSource dataSource;

    public GameController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping
    public List<Game> getAllJeux() {
        List<Game> jeux = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM jeux")) {

            while (rs.next()) {
                Game game = new Game();
                game.id=rs.getInt("id");
                game.nom = rs.getString("nom");
                game.auteur=rs.getString("auteur");
                jeux.add(game);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return jeux;
    }

    @PostMapping
    public void ajouterJeu(@RequestBody Game game) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement("INSERT INTO jeux (nom, auteur) VALUES (?, ?)")) {

            stmt.setString(1, game.nom);
            stmt.setString(2, game.auteur);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
