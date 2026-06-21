package com.example.movieticket.service;

import com.example.movieticket.domain.City;
import com.example.movieticket.domain.Theater;
import com.example.movieticket.exception.DuplicateResourceException;
import com.example.movieticket.exception.ResourceNotFoundException;
import com.example.movieticket.repository.CityRepository;
import com.example.movieticket.repository.TheaterRepository;
import com.example.movieticket.web.dto.TheaterResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TheaterService {

    private final TheaterRepository theaterRepository;
    private final CityRepository cityRepository;

    @Transactional
    public TheaterResponse create(Long cityId, String name, String address) {
        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new ResourceNotFoundException("City", cityId));
        String trimmedName = name.trim();
        if (theaterRepository.existsByCityIdAndNameIgnoreCase(cityId, trimmedName)) {
            throw new DuplicateResourceException(
                    "Theater '" + trimmedName + "' already exists in city " + cityId);
        }
        Theater saved = theaterRepository.save(new Theater(city, trimmedName, address.trim()));
        log.info("Created theater id={} cityId={}", saved.getId(), cityId);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<TheaterResponse> list(Long cityId) {
        List<Theater> theaters = cityId == null
                ? theaterRepository.findAll(Sort.by("name"))
                : theaterRepository.findByCityIdOrderByNameAsc(cityId);
        return theaters.stream().map(this::toResponse).toList();
    }

    private TheaterResponse toResponse(Theater theater) {
        return TheaterResponse.builder()
                .id(theater.getId())
                .cityId(theater.getCity().getId())
                .cityName(theater.getCity().getName())
                .name(theater.getName())
                .address(theater.getAddress())
                .build();
    }
}
