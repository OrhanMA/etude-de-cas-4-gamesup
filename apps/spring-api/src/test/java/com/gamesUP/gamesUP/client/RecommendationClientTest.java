package com.gamesUP.gamesUP.client;

import com.gamesUP.gamesUP.dto.PythonCatalogGame;
import com.gamesUP.gamesUP.dto.PythonRecommendationRequest;
import com.gamesUP.gamesUP.dto.PythonRecommendationResponse;
import com.gamesUP.gamesUP.dto.PythonUserInteraction;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class RecommendationClientTest {

    @Test
    void envoieLeContratPythonEtLitLesRecommandations() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://python.test");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RecommendationClient client = new RecommendationClient(builder.build());

        server.expect(requestTo("http://python.test/recommendations/"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {
                          "user_id": 12,
                          "interactions": [{"user_id": 12, "game_id": 4, "interaction_count": 2}],
                          "catalog": [{
                            "game_id": 4, "name": "Jeu acheté", "genre": "Stratégie",
                            "edition_number": 1, "price": 20.00,
                            "author": null, "publisher": null
                          }],
                          "limit": 3
                        }
                        """))
                .andRespond(withSuccess("""
                        {
                          "user_id": 12,
                          "strategy": "knn-hybrid",
                          "recommendations": [{
                            "game_id": 9, "game_name": "Jeu voisin",
                            "genre": "Stratégie", "score": 0.875
                          }]
                        }
                        """, MediaType.APPLICATION_JSON));

        PythonRecommendationRequest request = new PythonRecommendationRequest(
                12L,
                List.of(new PythonUserInteraction(12L, 4L, 2)),
                List.of(new PythonCatalogGame(
                        4L, "Jeu acheté", "Stratégie", 1,
                        new BigDecimal("20.00"), null, null)),
                3);

        PythonRecommendationResponse response = client.recommend(request);

        assertThat(response.userId()).isEqualTo(12L);
        assertThat(response.strategy()).isEqualTo("knn-hybrid");
        assertThat(response.recommendations()).hasSize(1);
        assertThat(response.recommendations().getFirst().gameId()).isEqualTo(9L);
        assertThat(response.recommendations().getFirst().score()).isEqualTo(0.875);
        server.verify();
    }
}
