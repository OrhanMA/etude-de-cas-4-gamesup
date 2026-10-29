package com.gamesUP.gamesUP.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@Entity
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @NotBlank
    public String name;

    @NotBlank
    public String genre;

    @Positive
    public int editionNumber;

    @ManyToOne
    public Author author;

    @ManyToOne
    public Publisher publisher;

    @JsonIgnore
    @OneToMany(mappedBy = "game")
    public List<OrderGame> orderGames = new ArrayList<>();

}
