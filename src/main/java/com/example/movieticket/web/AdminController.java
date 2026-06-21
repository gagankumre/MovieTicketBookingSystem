package com.example.movieticket.web;

import com.example.movieticket.service.CityService;
import com.example.movieticket.service.ScreenService;
import com.example.movieticket.service.TheaterService;
import com.example.movieticket.web.dto.CityRequest;
import com.example.movieticket.web.dto.CityResponse;
import com.example.movieticket.web.dto.ScreenRequest;
import com.example.movieticket.web.dto.ScreenResponse;
import com.example.movieticket.web.dto.TheaterRequest;
import com.example.movieticket.web.dto.TheaterResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin-only management endpoints under {@code /api/admin/**} (ROLE_ADMIN enforced by the security
 * filter chain). Grows with each admin-managed resource.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final CityService cityService;
    private final TheaterService theaterService;
    private final ScreenService screenService;

    @PostMapping("/cities")
    @ResponseStatus(HttpStatus.CREATED)
    public CityResponse createCity(@Valid @RequestBody CityRequest request) {
        return cityService.create(request.getName());
    }

    @PostMapping("/theaters")
    @ResponseStatus(HttpStatus.CREATED)
    public TheaterResponse createTheater(@Valid @RequestBody TheaterRequest request) {
        return theaterService.create(request.getCityId(), request.getName(), request.getAddress());
    }

    @PostMapping("/screens")
    @ResponseStatus(HttpStatus.CREATED)
    public ScreenResponse createScreen(@Valid @RequestBody ScreenRequest request) {
        return screenService.create(request.getTheaterId(), request.getName());
    }

    @GetMapping("/screens")
    public List<ScreenResponse> listScreens(@RequestParam Long theaterId) {
        return screenService.listByTheater(theaterId);
    }
}

