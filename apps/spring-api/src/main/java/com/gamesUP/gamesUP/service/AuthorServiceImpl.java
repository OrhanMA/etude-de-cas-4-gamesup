package com.gamesUP.gamesUP.service;

import com.gamesUP.gamesUP.dto.AuthorDto;
import com.gamesUP.gamesUP.dto.SaveAuthorRequest;
import com.gamesUP.gamesUP.model.Author;
import com.gamesUP.gamesUP.repository.AuthorRepository;
import com.gamesUP.gamesUP.repository.GameRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AuthorServiceImpl implements AuthorService {
    private final AuthorRepository authorRepository;
    private final GameRepository gameRepository;

    public AuthorServiceImpl(AuthorRepository authorRepository, GameRepository gameRepository) {
        this.authorRepository = authorRepository;
        this.gameRepository = gameRepository;
    }

    @Override
    public List<AuthorDto> getAllAuthors() {
        return authorRepository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    public AuthorDto getAuthor(Long id) {
        return authorRepository.findById(id).map(this::toDto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Auteur introuvable"));
    }

    @Override
    public AuthorDto createAuthor(SaveAuthorRequest request) {
        Author author = new Author();
        author.name = request.name().trim();
        return toDto(authorRepository.save(author));
    }

    @Override
    public void updateAuthor(Long id, SaveAuthorRequest request) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Auteur introuvable"));
        author.name = request.name().trim();
        authorRepository.save(author);
    }

    @Override
    public void deleteAuthor(Long id) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Auteur introuvable"));
        if (gameRepository.existsByAuthorId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cet auteur est associé à un ou plusieurs jeux");
        }
        authorRepository.delete(author);
    }

    private AuthorDto toDto(Author author) {
        return new AuthorDto(author.id, author.name);
    }
}
