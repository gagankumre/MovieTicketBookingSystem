package com.example.movieticket;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test: the full Spring context (including the JPA / datasource configuration) boots
 * against the H2 test profile.
 */
@SpringBootTest
@ActiveProfiles("test")
class MovieTicketBookingApplicationIT {

    @Test
    void contextLoads() {
        // Fails if the application context — and thus the DB configuration — cannot start.
    }
}
