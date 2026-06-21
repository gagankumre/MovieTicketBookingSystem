package com.example.movieticket.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.movieticket.domain.SeatHold;
import com.example.movieticket.domain.enums.HoldStatus;
import com.example.movieticket.domain.enums.SeatStatus;
import com.example.movieticket.service.HoldExpiryService;
import com.example.movieticket.service.HoldService;
import com.example.movieticket.support.JsonFixtures;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class HoldExpiryIT extends AbstractCatalogIT {

    private static final String CUSTOMER_EMAIL = "alice@example.com";

    @Autowired
    private HoldService holdService;
    @Autowired
    private HoldExpiryService holdExpiryService;

    private long showId;
    private long seatId;

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
        customerToken(); // registers CUSTOMER_EMAIL
    }

    @Test
    void sweeperReleasesExpiredHoldAndSeatBecomesReHoldable() {
        long holdId = holdService.holdSeats(CUSTOMER_EMAIL, showId, List.of(seatId)).getHoldId();
        assertThat(showSeatRepository.findById(seatId).orElseThrow().getStatus()).isEqualTo(SeatStatus.HELD);

        int expired = holdExpiryService.expireDueHolds(Instant.now().plus(10, ChronoUnit.MINUTES));

        assertThat(expired).isEqualTo(1);
        assertThat(showSeatRepository.findById(seatId).orElseThrow().getStatus()).isEqualTo(SeatStatus.AVAILABLE);
        assertThat(seatHoldRepository.findById(holdId).orElseThrow().getStatus()).isEqualTo(HoldStatus.EXPIRED);

        // seat is free again, so a fresh hold succeeds
        assertThat(holdService.holdSeats(CUSTOMER_EMAIL, showId, List.of(seatId)).getHoldId()).isNotNull();
    }

    @Test
    void expiredHoldIsTreatedAsFreeLazilyOnNextHold() {
        long staleHoldId = holdService.holdSeats(CUSTOMER_EMAIL, showId, List.of(seatId)).getHoldId();

        // force the hold to look expired without running the sweeper
        SeatHold stale = seatHoldRepository.findById(staleHoldId).orElseThrow();
        stale.setExpiresAt(Instant.now().minus(1, ChronoUnit.MINUTES));
        seatHoldRepository.save(stale);

        long freshHoldId = holdService.holdSeats(CUSTOMER_EMAIL, showId, List.of(seatId)).getHoldId();

        assertThat(freshHoldId).isNotEqualTo(staleHoldId);
        assertThat(seatHoldRepository.findById(staleHoldId).orElseThrow().getStatus()).isEqualTo(HoldStatus.EXPIRED);
        assertThat(showSeatRepository.findById(seatId).orElseThrow().getCurrentHoldId()).isEqualTo(freshHoldId);
    }
}
