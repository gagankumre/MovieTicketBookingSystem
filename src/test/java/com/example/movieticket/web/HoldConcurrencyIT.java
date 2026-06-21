package com.example.movieticket.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.movieticket.domain.enums.SeatStatus;
import com.example.movieticket.exception.SeatUnavailableException;
import com.example.movieticket.service.HoldService;
import com.example.movieticket.support.JsonFixtures;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

/**
 * Proves the core no-double-allocation guarantee: many users racing for the same seat result in
 * exactly one successful hold; every other attempt fails cleanly with {@link SeatUnavailableException}.
 */
class HoldConcurrencyIT extends AbstractCatalogIT {

    private static final String CUSTOMER_EMAIL = "alice@example.com";
    private static final int CONTENDERS = 8;

    @Autowired
    private HoldService holdService;

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
    void onlyOneOfManyConcurrentHoldsSucceeds() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(CONTENDERS);
        CountDownLatch startGate = new CountDownLatch(1);
        List<Future<String>> futures = new ArrayList<>();

        for (int i = 0; i < CONTENDERS; i++) {
            futures.add(pool.submit(() -> {
                startGate.await();
                try {
                    holdService.holdSeats(CUSTOMER_EMAIL, showId, List.of(seatId));
                    return "ok";
                } catch (SeatUnavailableException e) {
                    return "conflict";
                } catch (Exception e) {
                    return "error:" + e.getClass().getSimpleName();
                }
            }));
        }

        startGate.countDown();
        List<String> results = new ArrayList<>();
        for (Future<String> future : futures) {
            results.add(future.get(20, TimeUnit.SECONDS));
        }
        pool.shutdown();
        assertThat(pool.awaitTermination(10, TimeUnit.SECONDS)).isTrue();

        long successes = results.stream().filter("ok"::equals).count();
        long conflicts = results.stream().filter("conflict"::equals).count();
        List<String> unexpected = results.stream().filter(r -> r.startsWith("error:")).toList();

        assertThat(unexpected).as("unexpected errors").isEmpty();
        assertThat(successes).as("exactly one hold succeeds").isEqualTo(1);
        assertThat(conflicts).isEqualTo(CONTENDERS - 1);

        assertThat(showSeatRepository.findById(seatId).orElseThrow().getStatus()).isEqualTo(SeatStatus.HELD);
        assertThat(seatHoldRepository.count()).isEqualTo(1);
    }
}
