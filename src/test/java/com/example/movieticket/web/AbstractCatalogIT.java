package com.example.movieticket.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.movieticket.repository.CityRepository;
import com.example.movieticket.repository.ScreenRepository;
import com.example.movieticket.repository.TheaterRepository;
import com.example.movieticket.support.JsonFixtures;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

/**
 * Catalog integration-test base. Resets the whole catalog hierarchy (child → parent) and re-seeds
 * the admin before each test — the shared H2 instance is reused across test classes, so cleanup
 * must cover the full hierarchy to avoid foreign-key violations. Also exposes helpers that build
 * the city → theater → screen chain via the admin API.
 */
abstract class AbstractCatalogIT extends AbstractApiIT {

    @Autowired
    protected CityRepository cityRepository;
    @Autowired
    protected TheaterRepository theaterRepository;
    @Autowired
    protected ScreenRepository screenRepository;

    @BeforeEach
    void resetCatalog() {
        screenRepository.deleteAll();
        theaterRepository.deleteAll();
        cityRepository.deleteAll();
        userRepository.deleteAll();
        seedAdmin();
    }

    protected long createCity(String token) throws Exception {
        String response = mockMvc.perform(post("/api/admin/cities")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonFixtures.read("city/request/create-city.json")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return idOf(response);
    }

    protected long createTheater(String token, long cityId) throws Exception {
        String response = mockMvc.perform(post("/api/admin/theaters")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonFixtures.read("theater/request/create-theater.json", Map.of("cityId", cityId))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return idOf(response);
    }

    protected long createScreen(String token, long theaterId) throws Exception {
        String response = mockMvc.perform(post("/api/admin/screens")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonFixtures.read("screen/request/create-screen.json", Map.of("theaterId", theaterId))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return idOf(response);
    }
}
