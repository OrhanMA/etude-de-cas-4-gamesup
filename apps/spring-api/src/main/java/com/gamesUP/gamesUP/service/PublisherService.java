package com.gamesUP.gamesUP.service;

import com.gamesUP.gamesUP.dto.PublisherDto;
import com.gamesUP.gamesUP.dto.SavePublisherRequest;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

public interface PublisherService {
    List<PublisherDto> getAllPublishers();
    PublisherDto getPublisher(Long id) throws ResponseStatusException;
    PublisherDto createPublisher(SavePublisherRequest request);
    void updatePublisher(Long id, SavePublisherRequest request) throws ResponseStatusException;
    void deletePublisher(Long id) throws ResponseStatusException;
}
