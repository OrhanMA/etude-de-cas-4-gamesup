package com.gamesUP.gamesUP.service;

import com.gamesUP.gamesUP.dto.AuthResponse;
import com.gamesUP.gamesUP.dto.LoginRequest;
import com.gamesUP.gamesUP.dto.RegisterRequest;

public interface AuthService {
    void register(RegisterRequest registerRequest);
    AuthResponse login(LoginRequest loginRequest);
}
