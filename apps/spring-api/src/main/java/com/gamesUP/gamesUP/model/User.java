package com.gamesUP.gamesUP.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
public class User {

    @Getter
    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

    @Getter
    @Setter
    @NotBlank
    private String name;

    @Getter
    @Setter
    @NotBlank(message = "Email is required")
    @Email(message = "Email is not valid")
    @Column(unique = true, nullable = false)
    private String email;

    @Getter
    @Setter
    @NotBlank
    private String passwordHash;

    @Setter
    @Getter
    @Enumerated(EnumType.STRING)
    public Role role;

    @JsonIgnore
    @OneToMany(mappedBy = "user")
    public List<Order> orders = new ArrayList<>();

}
