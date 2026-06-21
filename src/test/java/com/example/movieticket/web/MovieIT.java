package com.example.movieticket.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.movieticket.support.JsonFixtures;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class MovieIT extends AbstractCatalogIT {

    private static final String MOVIE_REQUEST = "movie/request/create-movie.json";

    @Test
    void adminCreatesMovie() throws Exception {
        String response = mockMvc.perform(post("/api/admin/movies")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonFixtures.read(MOVIE_REQUEST)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonFixtures.assertMatches("movie/response/movie-created.json", response, "id");
    }

    @Test
    void duplicateMovieReturnsConflict() throws Exception {
        String token = adminToken();
        createMovie(token);

        mockMvc.perform(post("/api/admin/movies")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonFixtures.read(MOVIE_REQUEST)))
                .andExpect(status().isConflict());
    }

    @Test
    void publicListsMovies() throws Exception {
        createMovie(adminToken());

        mockMvc.perform(get("/api/public/movies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Inception"));
    }
}
