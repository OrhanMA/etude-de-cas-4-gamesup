package com.gamesUP.gamesUP.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @NotNull
    @ManyToOne(optional = false)
    public User user;

	@NotNull
	@Temporal(TemporalType.TIMESTAMP)
	public Date createdAt;

	public boolean paid;

	public boolean delivered;

	public boolean archived;

    @JsonIgnore
    @OneToMany(mappedBy = "order")
    public List<OrderGame> orderGames = new ArrayList<>();

}
