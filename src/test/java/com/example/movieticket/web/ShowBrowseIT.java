package com.example.movieticket.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.movieticket.support.JsonFixtures;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class ShowBrowseIT extends AbstractCatalogIT {

    private long cityId;
    private long movieId;

    @BeforeEach
    void publishShow() throws Exception {
        String token = adminToken();
        cityId = createCity(token);
        long screenId = createScreen(token, createTheater(token, cityId));
        defineLayout(token, screenId);
        movieId = createMovie(token);
        mockMvc.perform(post("/api/admin/shows")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonFixtures.read("show/request/create-show.json",
                                Map.of("screenId", screenId, "movieId", movieId))))
                .andExpect(status().isCreated());
    }

    @Test
    void browseShowsIsOpenAndReturnsPublishedShow() throws Exception {
        mockMvc.perform(get("/api/public/shows"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].movieTitle").value("Inception"))
                .andExpect(jsonPath("$[0].cityName").value("Bengaluru"))
                .andExpect(jsonPath("$[0].theaterName").value("PVR Forum"));
    }

    @Test
    void browseFiltersByCityAndMovie() throws Exception {
        mockMvc.perform(get("/api/public/shows")
                        .param("cityId", String.valueOf(cityId))
                        .param("movieId", String.valueOf(movieId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void browseWithNonMatchingFilterReturnsEmpty() throws Exception {
        mockMvc.perform(get("/api/public/shows").param("cityId", "999999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void seatMapReturnsAllSeatsForShow() throws Exception {
        long showId = showRepository.findAll().get(0).getId();

        mockMvc.perform(get("/api/public/shows/" + showId + "/seats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[0].status").value("AVAILABLE"))
                .andExpect(jsonPath("$[0].showSeatId").isNumber());
    }

    @Test
    void seatMapForUnknownShowReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/public/shows/999999/seats"))
                .andExpect(status().isNotFound());
    }
}
