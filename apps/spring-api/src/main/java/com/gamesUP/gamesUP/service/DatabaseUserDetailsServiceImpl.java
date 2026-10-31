package com.gamesUP.gamesUP.service;

import com.gamesUP.gamesUP.model.User;
import com.gamesUP.gamesUP.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DatabaseUserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public DatabaseUserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByEmailIgnoreCase(email);

        User databaseUser = user.orElseThrow(
                () -> new UsernameNotFoundException("User not found")
        );

        return org.springframework.security.core.userdetails.User
                .withUsername(databaseUser.getEmail())
                .password(databaseUser.getPasswordHash())
                .authorities(databaseUser.getRole().name())
                .build();
    }

}
