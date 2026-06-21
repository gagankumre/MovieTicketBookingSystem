package com.example.movieticket.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.movieticket.repository.BookingRepository;
import com.example.movieticket.repository.BookingSeatRepository;
import com.example.movieticket.repository.CityRepository;
import com.example.movieticket.repository.DiscountCodeRepository;
import com.example.movieticket.repository.MovieRepository;
import com.example.movieticket.repository.NotificationOutboxRepository;
import com.example.movieticket.repository.PaymentRepository;
import com.example.movieticket.repository.PricingTierRepository;
import com.example.movieticket.repository.RefundPolicyRepository;
import com.example.movieticket.repository.RefundRepository;
import com.example.movieticket.repository.ScreenRepository;
import com.example.movieticket.repository.SeatHoldRepository;
import com.example.movieticket.repository.SeatRepository;
import com.example.movieticket.repository.ShowRepository;
import com.example.movieticket.repository.ShowSeatRepository;
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
 * the city → theater → screen → seats / movie chain via the admin API.
 */
abstract class AbstractCatalogIT extends AbstractApiIT {

    @Autowired
    protected CityRepository cityRepository;
    @Autowired
    protected TheaterRepository theaterRepository;
    @Autowired
    protected ScreenRepository screenRepository;
    @Autowired
    protected SeatRepository seatRepository;
    @Autowired
    protected MovieRepository movieRepository;
    @Autowired
    protected ShowRepository showRepository;
    @Autowired
    protected ShowSeatRepository showSeatRepository;
    @Autowired
    protected SeatHoldRepository seatHoldRepository;
    @Autowired
    protected BookingSeatRepository bookingSeatRepository;
    @Autowired
    protected PaymentRepository paymentRepository;
    @Autowired
    protected BookingRepository bookingRepository;
    @Autowired
    protected DiscountCodeRepository discountCodeRepository;
    @Autowired
    protected RefundPolicyRepository refundPolicyRepository;
    @Autowired
    protected RefundRepository refundRepository;
    @Autowired
    protected NotificationOutboxRepository notificationOutboxRepository;
    @Autowired
    protected PricingTierRepository pricingTierRepository;

    @BeforeEach
    void resetCatalog() {
        notificationOutboxRepository.deleteAll();
        pricingTierRepository.deleteAll();
        bookingSeatRepository.deleteAll();
        paymentRepository.deleteAll();
        refundRepository.deleteAll();
        bookingRepository.deleteAll();
        showSeatRepository.deleteAll();
        seatHoldRepository.deleteAll();
        showRepository.deleteAll();
        seatRepository.deleteAll();
        screenRepository.deleteAll();
        theaterRepository.deleteAll();
        movieRepository.deleteAll();
        discountCodeRepository.deleteAll();
        refundPolicyRepository.deleteAll();
        cityRepository.deleteAll();
        userRepository.deleteAll();
        seedAdmin();
    }

    protected long createCity(String token) throws Exception {
        return idOf(adminPost("/api/admin/cities", token, JsonFixtures.read("city/request/create-city.json")));
    }

    protected long createTheater(String token, long cityId) throws Exception {
        return idOf(adminPost("/api/admin/theaters", token,
                JsonFixtures.read("theater/request/create-theater.json", Map.of("cityId", cityId))));
    }

    protected long createScreen(String token, long theaterId) throws Exception {
        return idOf(adminPost("/api/admin/screens", token,
                JsonFixtures.read("screen/request/create-screen.json", Map.of("theaterId", theaterId))));
    }

    protected long createMovie(String token) throws Exception {
        return idOf(adminPost("/api/admin/movies", token, JsonFixtures.read("movie/request/create-movie.json")));
    }

    protected void defineLayout(String token, long screenId) throws Exception {
        adminPost("/api/admin/screens/" + screenId + "/seats", token,
                JsonFixtures.read("seatlayout/request/define-layout.json"));
    }

    private String adminPost(String path, String token, String body) throws Exception {
        return mockMvc.perform(post(path)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
    }
}
