package com.gamesUP.gamesUP.client;

import com.gamesUP.gamesUP.dto.PythonRecommendationRequest;
import com.gamesUP.gamesUP.dto.PythonRecommendationResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class RecommendationClient {
    private final RestClient restClient;

    public RecommendationClient(RestClient recommendationRestClient) {
        this.restClient = recommendationRestClient;
    }

    public PythonRecommendationResponse recommend(PythonRecommendationRequest request) {
        return restClient.post()
                .uri("/recommendations/")
                .body(request)
                .retrieve()
                .body(PythonRecommendationResponse.class);
    }
}
