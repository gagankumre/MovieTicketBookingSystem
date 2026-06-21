package com.example.movieticket.web;

import com.example.movieticket.service.CityService;
import com.example.movieticket.web.dto.CityRequest;
import com.example.movieticket.web.dto.CityResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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

    @PostMapping("/cities")
    @ResponseStatus(HttpStatus.CREATED)
    public CityResponse createCity(@Valid @RequestBody CityRequest request) {
        return cityService.create(request.getName());
    }
}
