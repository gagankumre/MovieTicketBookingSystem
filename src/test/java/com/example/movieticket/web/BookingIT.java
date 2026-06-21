package com.example.movieticket.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.movieticket.support.JsonFixtures;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class BookingIT extends AbstractCatalogIT {

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

    private long holdSeat() throws Exception {
        String response = mockMvc.perform(post("/api/public/holds")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonFixtures.read("hold/request/hold-seats.json",
                                Map.of("showId", showId, "seatId", seatId))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("holdId").asLong();
    }

    private void createDiscountAsAdmin() throws Exception {
        mockMvc.perform(post("/api/admin/discount-codes")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonFixtures.read("discount/request/create-discount.json")))
                .andExpect(status().isCreated());
    }

    @Test
    void confirmsBookingAndSeatBecomesBooked() throws Exception {
        long holdId = holdSeat();

        mockMvc.perform(post("/api/public/bookings")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonFixtures.read("booking/request/book.json", Map.of("holdId", holdId))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.totalAmount").value(200.00))
                .andExpect(jsonPath("$.seats.length()").value(1))
                .andExpect(jsonPath("$.paymentStatus").value("SUCCESS"));

        mockMvc.perform(get("/api/public/shows/" + showId + "/seats"))
                .andExpect(jsonPath("$[?(@.showSeatId == " + seatId + ")].status").value("BOOKED"));
    }

    @Test
    void bookingWithDiscountReducesTotal() throws Exception {
        createDiscountAsAdmin();
        long holdId = holdSeat();

        mockMvc.perform(post("/api/public/bookings")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonFixtures.read("booking/request/book-with-discount.json", Map.of("holdId", holdId))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.subtotal").value(200.00))
                .andExpect(jsonPath("$.discountAmount").value(20.00))
                .andExpect(jsonPath("$.totalAmount").value(180.00))
                .andExpect(jsonPath("$.discountCode").value("SAVE10"));
    }

    @Test
    void declinedPaymentReturns402AndSeatStaysHeld() throws Exception {
        long holdId = holdSeat();

        mockMvc.perform(post("/api/public/bookings")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonFixtures.read("booking/request/book-declined.json", Map.of("holdId", holdId))))
                .andExpect(status().isPaymentRequired());

        assertThat(bookingRepository.count()).isZero();
        mockMvc.perform(get("/api/public/shows/" + showId + "/seats"))
                .andExpect(jsonPath("$[?(@.showSeatId == " + seatId + ")].status").value("HELD"));
    }
}
