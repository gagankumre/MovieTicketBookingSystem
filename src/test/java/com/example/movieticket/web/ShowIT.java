package com.example.movieticket.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.movieticket.support.JsonFixtures;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

class ShowIT extends AbstractCatalogIT {

    private ResultActions postShow(String token, long screenId, long movieId) throws Exception {
        String body = JsonFixtures.read("show/request/create-show.json",
                Map.of("screenId", screenId, "movieId", movieId));
        return mockMvc.perform(post("/api/admin/shows")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    @Test
    void adminPublishesShowAndGeneratesShowSeats() throws Exception {
        String token = adminToken();
        long screenId = createScreen(token, createTheater(token, createCity(token)));
        defineLayout(token, screenId);
        long movieId = createMovie(token);

        String response = postShow(token, screenId, movieId)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonFixtures.assertMatches("show/response/show-created.json", response, "id", "screenId", "movieId");
        long showId = idOf(response);
        assertThat(showSeatRepository.countByShowId(showId)).isEqualTo(5);
    }

    @Test
    void publishingWithoutSeatLayoutReturnsConflict() throws Exception {
        String token = adminToken();
        long screenId = createScreen(token, createTheater(token, createCity(token)));
        long movieId = createMovie(token);

        postShow(token, screenId, movieId)
                .andExpect(status().isConflict());
    }

    @Test
    void overlappingShowOnScreenReturnsConflict() throws Exception {
        String token = adminToken();
        long screenId = createScreen(token, createTheater(token, createCity(token)));
        defineLayout(token, screenId);
        long movieId = createMovie(token);
        postShow(token, screenId, movieId).andExpect(status().isCreated());

        postShow(token, screenId, movieId)
                .andExpect(status().isConflict());
    }

    @Test
    void publishingForUnknownScreenReturnsNotFound() throws Exception {
        String token = adminToken();
        long movieId = createMovie(token);

        postShow(token, 999999L, movieId)
                .andExpect(status().isNotFound());
    }
}
