package com.gamesUP.gamesUP.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

enum Role {
    ADMIN,
    USER
}

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

    @NotBlank
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email is not valid")
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    public Role role;

    @JsonIgnore
    @OneToMany(mappedBy = "user")
    public List<Order> orders = new ArrayList<>();
}
