package com.example.movieticket.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.movieticket.domain.NotificationOutbox;
import com.example.movieticket.domain.enums.NotificationStatus;
import com.example.movieticket.domain.enums.NotificationType;
import com.example.movieticket.notification.NotificationService;
import com.example.movieticket.support.JsonFixtures;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class NotificationIT extends AbstractCatalogIT {

    @Autowired
    private NotificationService notificationService;

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

    private void book() throws Exception {
        String holdResponse = mockMvc.perform(post("/api/public/holds")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonFixtures.read("hold/request/hold-seats.json",
                                Map.of("showId", showId, "seatId", seatId))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long holdId = objectMapper.readTree(holdResponse).get("holdId").asLong();
        mockMvc.perform(post("/api/public/bookings")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonFixtures.read("booking/request/book.json", Map.of("holdId", holdId))))
                .andExpect(status().isCreated());
    }

    @Test
    void confirmationIsDeliveredAsynchronouslyAfterCommit() throws Exception {
        book();

        // The booking returned without blocking on delivery; the after-commit @Async listener
        // dispatches the outbox row shortly afterwards on a separate thread.
        await(() -> !notificationOutboxRepository.findByStatus(NotificationStatus.SENT).isEmpty());

        assertThat(notificationOutboxRepository.findByStatus(NotificationStatus.SENT))
                .extracting(NotificationOutbox::getType)
                .containsExactly(NotificationType.BOOKING_CONFIRMATION);
    }

    @Test
    void scheduledDispatchActsAsRetrySafetyNet() throws Exception {
        book();
        // Whatever the async listener didn't finish, the scheduled dispatcher would pick up;
        // invoking it directly is idempotent and leaves the confirmation SENT.
        notificationService.dispatchPending();

        assertThat(notificationOutboxRepository.findByStatus(NotificationStatus.PENDING)).isEmpty();
    }

    private void await(java.util.function.BooleanSupplier condition) throws InterruptedException {
        for (int i = 0; i < 50 && !condition.getAsBoolean(); i++) {
            Thread.sleep(100);
        }
    }
}
