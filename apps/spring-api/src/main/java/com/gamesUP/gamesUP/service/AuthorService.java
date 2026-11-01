package com.gamesUP.gamesUP.service;

import com.gamesUP.gamesUP.dto.AuthorDto;
import com.gamesUP.gamesUP.dto.SaveAuthorRequest;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

public interface AuthorService {
    List<AuthorDto> getAllAuthors();
    AuthorDto getAuthor(Long id) throws ResponseStatusException;
    AuthorDto createAuthor(SaveAuthorRequest request);
    void updateAuthor(Long id, SaveAuthorRequest request) throws ResponseStatusException;
    void deleteAuthor(Long id) throws ResponseStatusException;
}
