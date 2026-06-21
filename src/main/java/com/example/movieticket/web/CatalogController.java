package com.example.movieticket.web;

import com.example.movieticket.service.CityService;
import com.example.movieticket.service.MovieService;
import com.example.movieticket.service.ShowService;
import com.example.movieticket.service.TheaterService;
import com.example.movieticket.web.dto.CityResponse;
import com.example.movieticket.web.dto.MovieResponse;
import com.example.movieticket.web.dto.SeatView;
import com.example.movieticket.web.dto.ShowSummaryResponse;
import com.example.movieticket.web.dto.TheaterResponse;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public catalog browsing under {@code /api/public/**}. GETs are open to anonymous users; grows
 * with each browsable resource (cities, theaters, shows, seat maps).
 */
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class CatalogController {

    private final CityService cityService;
    private final TheaterService theaterService;
    private final MovieService movieService;
    private final ShowService showService;

    @GetMapping("/cities")
    public List<CityResponse> listCities() {
        return cityService.list();
    }

    @GetMapping("/theaters")
    public List<TheaterResponse> listTheaters(@RequestParam(required = false) Long cityId) {
        return theaterService.list(cityId);
    }

    @GetMapping("/movies")
    public List<MovieResponse> listMovies() {
        return movieService.list();
    }

    @GetMapping("/shows")
    public List<ShowSummaryResponse> browseShows(
            @RequestParam(required = false) Long cityId,
            @RequestParam(required = false) Long movieId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return showService.browse(cityId, movieId, date);
    }

    @GetMapping("/shows/{showId}/seats")
    public List<SeatView> seatMap(@PathVariable Long showId) {
        return showService.getSeatMap(showId);
    }
}

