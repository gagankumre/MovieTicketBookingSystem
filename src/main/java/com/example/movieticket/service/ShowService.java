package com.example.movieticket.service;

import com.example.movieticket.domain.Movie;
import com.example.movieticket.domain.Screen;
import com.example.movieticket.domain.Seat;
import com.example.movieticket.domain.Show;
import com.example.movieticket.domain.ShowSeat;
import com.example.movieticket.domain.enums.ShowType;
import com.example.movieticket.exception.BusinessRuleException;
import com.example.movieticket.exception.ResourceNotFoundException;
import com.example.movieticket.exception.ShowOverlapException;
import com.example.movieticket.repository.MovieRepository;
import com.example.movieticket.repository.ScreenRepository;
import com.example.movieticket.repository.SeatRepository;
import com.example.movieticket.repository.ShowRepository;
import com.example.movieticket.repository.ShowSeatRepository;
import com.example.movieticket.web.dto.ShowResponse;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShowService {

    private final ShowRepository showRepository;
    private final ShowSeatRepository showSeatRepository;
    private final ScreenRepository screenRepository;
    private final MovieRepository movieRepository;
    private final SeatRepository seatRepository;
    private final PricingService pricingService;

    /**
     * Schedules a show and publishes it: generates one {@link ShowSeat} per seat in the screen with
     * its resolved price. Rejects overlapping shows on the same screen and screens without a layout.
     */
    @Transactional
    public ShowResponse createAndPublish(Long screenId, Long movieId, Instant startTime,
                                         ShowType showType, BigDecimal basePrice) {
        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> new ResourceNotFoundException("Screen", screenId));
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", movieId));

        Instant endTime = startTime.plus(Duration.ofMinutes(movie.getDurationMinutes()));
        if (showRepository.existsByScreenIdAndStartTimeLessThanAndEndTimeGreaterThan(screenId, endTime, startTime)) {
            throw new ShowOverlapException(
                    "Screen " + screenId + " already has a show overlapping " + startTime + "–" + endTime);
        }

        List<Seat> seats = seatRepository.findByScreenIdOrderByRowLabelAscSeatNumberAsc(screenId);
        if (seats.isEmpty()) {
            throw new BusinessRuleException("Screen " + screenId + " has no seat layout");
        }

        Show show = showRepository.save(new Show(screen, movie, startTime, endTime, showType, basePrice));
        List<ShowSeat> showSeats = seats.stream()
                .map(seat -> new ShowSeat(show, seat,
                        pricingService.resolvePrice(basePrice, seat.getCategory(), showType)))
                .toList();
        showSeatRepository.saveAll(showSeats);

        log.info("Published show id={} on screen={} with {} seats", show.getId(), screenId, showSeats.size());
        return toResponse(show, showSeats.size());
    }

    private ShowResponse toResponse(Show show, int totalSeats) {
        return ShowResponse.builder()
                .id(show.getId())
                .screenId(show.getScreen().getId())
                .screenName(show.getScreen().getName())
                .movieId(show.getMovie().getId())
                .movieTitle(show.getMovie().getTitle())
                .startTime(show.getStartTime())
                .endTime(show.getEndTime())
                .showType(show.getShowType().name())
                .basePrice(show.getBasePrice())
                .totalSeats(totalSeats)
                .build();
    }
}
