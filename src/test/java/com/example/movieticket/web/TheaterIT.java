package com.example.movieticket.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.movieticket.support.JsonFixtures;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class TheaterIT extends AbstractCatalogIT {

    private String createTheaterBody(long cityId) {
        return JsonFixtures.read("theater/request/create-theater.json", Map.of("cityId", cityId));
    }

    @Test
    void adminCreatesTheater() throws Exception {
        String token = adminToken();
        long cityId = createCity(token);

        String response = mockMvc.perform(post("/api/admin/theaters")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createTheaterBody(cityId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonFixtures.assertMatches("theater/response/theater-created.json", response, "id", "cityId");
    }

    @Test
    void createTheaterForUnknownCityReturnsNotFound() throws Exception {
        mockMvc.perform(post("/api/admin/theaters")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createTheaterBody(999999L)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void duplicateTheaterInCityReturnsConflict() throws Exception {
        String token = adminToken();
        long cityId = createCity(token);
        mockMvc.perform(post("/api/admin/theaters")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createTheaterBody(cityId)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/admin/theaters")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createTheaterBody(cityId)))
                .andExpect(status().isConflict());
    }

    @Test
    void publicBrowsesTheatersByCity() throws Exception {
        String token = adminToken();
        long cityId = createCity(token);
        mockMvc.perform(post("/api/admin/theaters")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createTheaterBody(cityId)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/public/theaters").param("cityId", String.valueOf(cityId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("PVR Forum"))
                .andExpect(jsonPath("$[0].cityName").value("Bengaluru"));
    }
}
