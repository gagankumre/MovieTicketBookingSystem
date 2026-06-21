package com.example.movieticket.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.example.movieticket.domain.Movie;
import com.example.movieticket.domain.Screen;
import com.example.movieticket.domain.Show;
import com.example.movieticket.domain.enums.SeatCategory;
import com.example.movieticket.domain.enums.ShowType;
import com.example.movieticket.exception.BusinessRuleException;
import com.example.movieticket.exception.ResourceNotFoundException;
import com.example.movieticket.exception.ShowOverlapException;
import com.example.movieticket.repository.MovieRepository;
import com.example.movieticket.repository.ScreenRepository;
import com.example.movieticket.repository.SeatRepository;
import com.example.movieticket.repository.ShowRepository;
import com.example.movieticket.repository.ShowSeatRepository;
import com.example.movieticket.support.factory.MovieFactory;
import com.example.movieticket.support.factory.ScreenFactory;
import com.example.movieticket.support.factory.SeatFactory;
import com.example.movieticket.support.factory.ShowFactory;
import com.example.movieticket.web.dto.ShowResponse;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ShowServiceTest {

    @Mock
    private ShowRepository showRepository;
    @Mock
    private ShowSeatRepository showSeatRepository;
    @Mock
    private ScreenRepository screenRepository;
    @Mock
    private MovieRepository movieRepository;
    @Mock
    private SeatRepository seatRepository;
    @Mock
    private PricingService pricingService;

    @InjectMocks
    private ShowService showService;

    private final Screen screen = ScreenFactory.withId(5L, ScreenFactory.screen(null, "Audi 1"));
    private final Movie movie = MovieFactory.withId(7L, MovieFactory.movie("Inception", "English", 148));
    private final Instant start = Instant.parse("2026-07-01T10:00:00Z");
    private final Instant end = start.plus(Duration.ofMinutes(148));
    private final BigDecimal basePrice = new BigDecimal("200.00");

    @Test
    void createAndPublishGeneratesShowSeatForEachSeat() {
        when(screenRepository.findById(5L)).thenReturn(Optional.of(screen));
        when(movieRepository.findById(7L)).thenReturn(Optional.of(movie));
        when(showRepository.existsByScreenIdAndStartTimeLessThanAndEndTimeGreaterThan(5L, end, start))
                .thenReturn(false);
        when(seatRepository.findByScreenIdOrderByRowLabelAscSeatNumberAsc(5L)).thenReturn(List.of(
                SeatFactory.seat(screen, "A", 1, SeatCategory.REGULAR),
                SeatFactory.seat(screen, "A", 2, SeatCategory.PREMIUM)));
        when(showRepository.save(any(Show.class))).thenAnswer(inv -> ShowFactory.withId(99L, inv.getArgument(0)));
        when(pricingService.resolvePrice(eq(basePrice), any(SeatCategory.class), eq(ShowType.REGULAR)))
                .thenReturn(new BigDecimal("200.00"));

        ShowResponse response = showService.createAndPublish(5L, 7L, start, ShowType.REGULAR, basePrice);

        assertThat(response.getId()).isEqualTo(99L);
        assertThat(response.getTotalSeats()).isEqualTo(2);
        assertThat(response.getEndTime()).isEqualTo(end);
        assertThat(response.getScreenName()).isEqualTo("Audi 1");
        assertThat(response.getMovieTitle()).isEqualTo("Inception");
        org.mockito.Mockito.verify(showSeatRepository).saveAll(any());
    }

    @Test
    void throwsWhenScreenMissing() {
        when(screenRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> showService.createAndPublish(5L, 7L, start, ShowType.REGULAR, basePrice))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void throwsWhenMovieMissing() {
        when(screenRepository.findById(5L)).thenReturn(Optional.of(screen));
        when(movieRepository.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> showService.createAndPublish(5L, 7L, start, ShowType.REGULAR, basePrice))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void throwsWhenShowOverlapsOnScreen() {
        when(screenRepository.findById(5L)).thenReturn(Optional.of(screen));
        when(movieRepository.findById(7L)).thenReturn(Optional.of(movie));
        when(showRepository.existsByScreenIdAndStartTimeLessThanAndEndTimeGreaterThan(5L, end, start))
                .thenReturn(true);

        assertThatThrownBy(() -> showService.createAndPublish(5L, 7L, start, ShowType.REGULAR, basePrice))
                .isInstanceOf(ShowOverlapException.class);
    }

    @Test
    void throwsWhenScreenHasNoSeatLayout() {
        when(screenRepository.findById(5L)).thenReturn(Optional.of(screen));
        when(movieRepository.findById(7L)).thenReturn(Optional.of(movie));
        when(showRepository.existsByScreenIdAndStartTimeLessThanAndEndTimeGreaterThan(5L, end, start))
                .thenReturn(false);
        when(seatRepository.findByScreenIdOrderByRowLabelAscSeatNumberAsc(5L)).thenReturn(List.of());

        assertThatThrownBy(() -> showService.createAndPublish(5L, 7L, start, ShowType.REGULAR, basePrice))
                .isInstanceOf(BusinessRuleException.class);
    }
}
