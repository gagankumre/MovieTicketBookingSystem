package com.example.movieticket.service;

import com.example.movieticket.domain.Screen;
import com.example.movieticket.domain.Theater;
import com.example.movieticket.exception.DuplicateResourceException;
import com.example.movieticket.exception.ResourceNotFoundException;
import com.example.movieticket.repository.ScreenRepository;
import com.example.movieticket.repository.TheaterRepository;
import com.example.movieticket.web.dto.ScreenResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScreenService {

    private final ScreenRepository screenRepository;
    private final TheaterRepository theaterRepository;

    @Transactional
    public ScreenResponse create(Long theaterId, String name) {
        Theater theater = theaterRepository.findById(theaterId)
                .orElseThrow(() -> new ResourceNotFoundException("Theater", theaterId));
        String trimmedName = name.trim();
        if (screenRepository.existsByTheaterIdAndNameIgnoreCase(theaterId, trimmedName)) {
            throw new DuplicateResourceException(
                    "Screen '" + trimmedName + "' already exists in theater " + theaterId);
        }
        Screen saved = screenRepository.save(new Screen(theater, trimmedName));
        log.info("Created screen id={} theaterId={}", saved.getId(), theaterId);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ScreenResponse> listByTheater(Long theaterId) {
        return screenRepository.findByTheaterIdOrderByNameAsc(theaterId).stream()
                .map(this::toResponse)
                .toList();
    }

    private ScreenResponse toResponse(Screen screen) {
        return ScreenResponse.builder()
                .id(screen.getId())
                .theaterId(screen.getTheater().getId())
                .theaterName(screen.getTheater().getName())
                .name(screen.getName())
                .build();
    }
}
