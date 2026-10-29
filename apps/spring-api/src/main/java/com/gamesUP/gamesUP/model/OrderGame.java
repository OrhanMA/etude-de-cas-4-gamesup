package com.gamesUP.gamesUP.model;

import java.math.BigDecimal;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

@Entity
public class OrderGame {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Long id;

    @NotNull
    @ManyToOne(optional = false)
    public Order order;

    @NotNull
    @ManyToOne(optional = false)
    public Game game;

    @Positive
    public int quantity;

    @NotNull
    @PositiveOrZero
    public BigDecimal unitPrice;

}
