package com.example.movieticket.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.movieticket.support.JsonFixtures;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class ScreenIT extends AbstractCatalogIT {

    private String screenBody(long theaterId) {
        return JsonFixtures.read("screen/request/create-screen.json", Map.of("theaterId", theaterId));
    }

    @Test
    void adminCreatesScreen() throws Exception {
        String token = adminToken();
        long theaterId = createTheater(token, createCity(token));

        String response = mockMvc.perform(post("/api/admin/screens")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(screenBody(theaterId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonFixtures.assertMatches("screen/response/screen-created.json", response, "id", "theaterId");
    }

    @Test
    void createScreenForUnknownTheaterReturnsNotFound() throws Exception {
        mockMvc.perform(post("/api/admin/screens")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(screenBody(999999L)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void duplicateScreenInTheaterReturnsConflict() throws Exception {
        String token = adminToken();
        long theaterId = createTheater(token, createCity(token));
        createScreen(token, theaterId);

        mockMvc.perform(post("/api/admin/screens")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(screenBody(theaterId)))
                .andExpect(status().isConflict());
    }

    @Test
    void adminListsScreensByTheater() throws Exception {
        String token = adminToken();
        long theaterId = createTheater(token, createCity(token));
        createScreen(token, theaterId);

        mockMvc.perform(get("/api/admin/screens")
                        .header("Authorization", "Bearer " + token)
                        .param("theaterId", String.valueOf(theaterId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Audi 1"));
    }
}
