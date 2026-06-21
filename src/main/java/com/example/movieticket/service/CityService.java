package com.example.movieticket.service;

import com.example.movieticket.domain.City;
import com.example.movieticket.exception.DuplicateResourceException;
import com.example.movieticket.repository.CityRepository;
import com.example.movieticket.web.dto.CityResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CityService {

    private final CityRepository cityRepository;

    @Transactional
    public CityResponse create(String name) {
        String trimmed = name.trim();
        if (cityRepository.existsByNameIgnoreCase(trimmed)) {
            throw new DuplicateResourceException("City '" + trimmed + "' already exists");
        }
        City saved = cityRepository.save(new City(trimmed));
        log.info("Created city id={} name={}", saved.getId(), saved.getName());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<CityResponse> list() {
        return cityRepository.findAll(Sort.by("name")).stream().map(this::toResponse).toList();
    }

    private CityResponse toResponse(City city) {
        return CityResponse.builder().id(city.getId()).name(city.getName()).build();
    }
}
