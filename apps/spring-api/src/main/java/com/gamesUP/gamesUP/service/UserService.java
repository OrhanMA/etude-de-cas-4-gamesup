package com.gamesUP.gamesUP.service;

import com.gamesUP.gamesUP.dto.UserDto;
import com.gamesUP.gamesUP.dto.UpdateClientRequest;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

public interface UserService {
    List<UserDto> getAllUsers();
    UserDto getUser(Long id) throws ResponseStatusException;
    void updateUser(Long id, UpdateClientRequest request) throws ResponseStatusException;
    void deleteUser(Long id) throws ResponseStatusException;
}
