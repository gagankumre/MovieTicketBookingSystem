package com.example.movieticket.service;

import com.example.movieticket.domain.Screen;
import com.example.movieticket.domain.Seat;
import com.example.movieticket.exception.DuplicateResourceException;
import com.example.movieticket.exception.ResourceNotFoundException;
import com.example.movieticket.repository.ScreenRepository;
import com.example.movieticket.repository.SeatRepository;
import com.example.movieticket.web.dto.SeatLayoutResponse;
import com.example.movieticket.web.dto.SeatRowSpec;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeatService {

    private final SeatRepository seatRepository;
    private final ScreenRepository screenRepository;

    /** Defines a screen's seat layout once; the layout is immutable thereafter. */
    @Transactional
    public SeatLayoutResponse defineLayout(Long screenId, List<SeatRowSpec> rows) {
        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> new ResourceNotFoundException("Screen", screenId));
        if (seatRepository.existsByScreenId(screenId)) {
            throw new DuplicateResourceException("Seat layout already defined for screen " + screenId);
        }
        List<Seat> seats = new ArrayList<>();
        for (SeatRowSpec row : rows) {
            String rowLabel = row.getRowLabel().trim();
            for (int number = 1; number <= row.getSeatCount(); number++) {
                seats.add(new Seat(screen, rowLabel, number, row.getCategory()));
            }
        }
        seatRepository.saveAll(seats);
        log.info("Defined layout for screen id={} with {} seats", screenId, seats.size());
        return SeatLayoutResponse.builder().screenId(screenId).totalSeats(seats.size()).build();
    }
}
