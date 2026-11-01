package com.gamesUP.gamesUP.service;

import com.gamesUP.gamesUP.dto.PublisherDto;
import com.gamesUP.gamesUP.dto.SavePublisherRequest;
import com.gamesUP.gamesUP.model.Publisher;
import com.gamesUP.gamesUP.repository.GameRepository;
import com.gamesUP.gamesUP.repository.PublisherRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class PublisherServiceImpl implements PublisherService {
    private final PublisherRepository publisherRepository;
    private final GameRepository gameRepository;

    public PublisherServiceImpl(PublisherRepository publisherRepository, GameRepository gameRepository) {
        this.publisherRepository = publisherRepository;
        this.gameRepository = gameRepository;
    }

    @Override
    public List<PublisherDto> getAllPublishers() {
        return publisherRepository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    public PublisherDto getPublisher(Long id) {
        return publisherRepository.findById(id).map(this::toDto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Éditeur introuvable"));
    }

    @Override
    public PublisherDto createPublisher(SavePublisherRequest request) {
        Publisher publisher = new Publisher();
        publisher.name = request.name().trim();
        return toDto(publisherRepository.save(publisher));
    }

    @Override
    public void updatePublisher(Long id, SavePublisherRequest request) {
        Publisher publisher = publisherRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Éditeur introuvable"));
        publisher.name = request.name().trim();
        publisherRepository.save(publisher);
    }

    @Override
    public void deletePublisher(Long id) {
        Publisher publisher = publisherRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Éditeur introuvable"));
        if (gameRepository.existsByPublisherId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cet éditeur est associé à un ou plusieurs jeux");
        }
        publisherRepository.delete(publisher);
    }

    private PublisherDto toDto(Publisher publisher) {
        return new PublisherDto(publisher.id, publisher.name);
    }
}
