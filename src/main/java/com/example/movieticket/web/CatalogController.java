package com.example.movieticket.web;

import com.example.movieticket.service.CityService;
import com.example.movieticket.web.dto.CityResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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

    @GetMapping("/cities")
    public List<CityResponse> listCities() {
        return cityService.list();
    }
}
