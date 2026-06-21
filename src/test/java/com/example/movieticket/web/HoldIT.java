package com.example.movieticket.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.movieticket.support.JsonFixtures;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class HoldIT extends AbstractCatalogIT {

    private long showId;
    private long seatId;
    private String customerToken;

    @BeforeEach
    void publishShowAndRegisterCustomer() throws Exception {
        String admin = adminToken();
        long screenId = createScreen(admin, createTheater(admin, createCity(admin)));
        defineLayout(admin, screenId);
        long movieId = createMovie(admin);
        String showResponse = mockMvc.perform(post("/api/admin/shows")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonFixtures.read("show/request/create-show.json",
                                Map.of("screenId", screenId, "movieId", movieId))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        showId = idOf(showResponse);
        seatId = showSeatRepository.findSeatMap(showId).get(0).getId();
        customerToken = customerToken();
    }

    private String holdBody() {
        return JsonFixtures.read("hold/request/hold-seats.json", Map.of("showId", showId, "seatId", seatId));
    }

    @Test
    void holdRequiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/public/holds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(holdBody()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void customerHoldsSeatAndItBecomesHeld() throws Exception {
        mockMvc.perform(post("/api/public/holds")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(holdBody()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.holdId").isNumber())
                .andExpect(jsonPath("$.seats.length()").value(1))
                .andExpect(jsonPath("$.totalAmount").value(200.00));

        mockMvc.perform(get("/api/public/shows/" + showId + "/seats"))
                .andExpect(jsonPath("$[?(@.showSeatId == " + seatId + ")].status").value("HELD"));
    }

    @Test
    void holdingAnAlreadyHeldSeatReturnsConflict() throws Exception {
        mockMvc.perform(post("/api/public/holds")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(holdBody()))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/public/holds")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(holdBody()))
                .andExpect(status().isConflict());
    }

    @Test
    void releasingHoldFreesTheSeat() throws Exception {
        String response = mockMvc.perform(post("/api/public/holds")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(holdBody()))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long holdId = objectMapper.readTree(response).get("holdId").asLong();

        mockMvc.perform(delete("/api/public/holds/" + holdId)
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/public/shows/" + showId + "/seats"))
                .andExpect(jsonPath("$[?(@.showSeatId == " + seatId + ")].status").value("AVAILABLE"));
    }
}
