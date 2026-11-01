package com.gamesUP.gamesUP.model;

import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMin;
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

    @DecimalMin("0.00")
    @Column(precision = 10, scale = 2)
    public BigDecimal price;

    @ManyToOne
    public Author author;

    @ManyToOne
    public Publisher publisher;

    @JsonIgnore
    @OneToMany(mappedBy = "game")
    public List<OrderGame> orderGames = new ArrayList<>();

}
