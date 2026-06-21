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

class CancellationIT extends AbstractCatalogIT {

    private long showId;
    private long seatId;
    private String customerToken;

    @BeforeEach
    void setUpShowAndPolicies() throws Exception {
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
        createRefundPolicy(admin, 48, 100);
        createRefundPolicy(admin, 6, 50);
        createRefundPolicy(admin, 0, 0);
        customerToken = customerToken();
    }

    private void createRefundPolicy(String token, int hours, int percent) throws Exception {
        mockMvc.perform(post("/api/admin/refund-policies")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonFixtures.read("refundpolicy/request/create-policy.json",
                                Map.of("hoursBeforeShow", hours, "refundPercent", percent))))
                .andExpect(status().isCreated());
    }

    private long book() throws Exception {
        String holdResponse = mockMvc.perform(post("/api/public/holds")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonFixtures.read("hold/request/hold-seats.json",
                                Map.of("showId", showId, "seatId", seatId))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long holdId = objectMapper.readTree(holdResponse).get("holdId").asLong();
        String bookingResponse = mockMvc.perform(post("/api/public/bookings")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonFixtures.read("booking/request/book.json", Map.of("holdId", holdId))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(bookingResponse).get("bookingId").asLong();
    }

    @Test
    void cancelReleasesSeatAndRefundsPerPolicy() throws Exception {
        long bookingId = book();

        mockMvc.perform(post("/api/public/bookings/" + bookingId + "/cancel")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.refundAmount").value(200.00))
                .andExpect(jsonPath("$.refundStatus").value("REFUNDED"));

        mockMvc.perform(get("/api/public/shows/" + showId + "/seats"))
                .andExpect(jsonPath("$[?(@.showSeatId == " + seatId + ")].status").value("AVAILABLE"));
    }

    @Test
    void cancellingTwiceReturnsConflict() throws Exception {
        long bookingId = book();

        mockMvc.perform(post("/api/public/bookings/" + bookingId + "/cancel")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/public/bookings/" + bookingId + "/cancel")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isConflict());
    }

    @Test
    void cancelRequiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/public/bookings/1/cancel"))
                .andExpect(status().isUnauthorized());
    }
}
