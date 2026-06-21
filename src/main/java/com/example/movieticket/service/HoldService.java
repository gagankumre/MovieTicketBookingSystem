package com.example.movieticket.service;

import com.example.movieticket.config.HoldProperties;
import com.example.movieticket.domain.SeatHold;
import com.example.movieticket.domain.Show;
import com.example.movieticket.domain.ShowSeat;
import com.example.movieticket.domain.User;
import com.example.movieticket.domain.enums.HoldStatus;
import com.example.movieticket.exception.ResourceNotFoundException;
import com.example.movieticket.exception.SeatUnavailableException;
import com.example.movieticket.exception.UnauthorizedActionException;
import com.example.movieticket.mapper.HoldMapper;
import com.example.movieticket.repository.SeatHoldRepository;
import com.example.movieticket.repository.ShowRepository;
import com.example.movieticket.repository.ShowSeatRepository;
import com.example.movieticket.repository.UserRepository;
import com.example.movieticket.web.dto.HoldResponse;
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
public class HoldService {

    private final ShowRepository showRepository;
    private final UserRepository userRepository;
    private final SeatHoldRepository seatHoldRepository;
    private final ShowSeatRepository showSeatRepository;
    private final SeatLockManager seatLockManager;
    private final HoldMapper holdMapper;
    private final HoldProperties holdProperties;

    @Transactional
    public HoldResponse holdSeats(String userEmail, Long showId, List<Long> seatIds) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new ResourceNotFoundException("Show", showId));
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));

        List<ShowSeat> seats = seatLockManager.lockSeats(seatIds);
        for (ShowSeat seat : seats) {
            if (!show.getId().equals(seat.getShow().getId())) {
                throw new SeatUnavailableException("Seat " + seat.getId() + " does not belong to show " + showId);
            }
        }

        Instant expiresAt = Instant.now().plus(Duration.ofMinutes(holdProperties.ttlMinutes()));
        SeatHold hold = seatHoldRepository.save(new SeatHold(user, show, expiresAt));
        seatLockManager.hold(seats, hold.getId());

        log.info("User {} holding {} seat(s) on show {} as hold {}", user.getId(), seats.size(), showId, hold.getId());
        return toResponse(hold, seats);
    }

    @Transactional
    public void releaseHold(String userEmail, Long holdId) {
        SeatHold hold = seatHoldRepository.findById(holdId)
                .orElseThrow(() -> new ResourceNotFoundException("Hold", holdId));
        if (!hold.getUser().getEmail().equals(userEmail)) {
            throw new UnauthorizedActionException("Hold " + holdId + " does not belong to the current user");
        }
        if (hold.getStatus() != HoldStatus.ACTIVE) {
            return;
        }
        List<Long> seatIds = showSeatRepository.findByCurrentHoldId(holdId).stream().map(ShowSeat::getId).toList();
        if (!seatIds.isEmpty()) {
            seatLockManager.release(seatLockManager.lockSeats(seatIds));
        }
        hold.casStatus(HoldStatus.ACTIVE, HoldStatus.RELEASED);
        log.info("Released hold {}", holdId);
    }

    private HoldResponse toResponse(SeatHold hold, List<ShowSeat> seats) {
        BigDecimal total = seats.stream().map(ShowSeat::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        return HoldResponse.builder()
                .holdId(hold.getId())
                .showId(hold.getShow().getId())
                .expiresAt(hold.getExpiresAt())
                .seats(holdMapper.toHeldSeats(seats))
                .totalAmount(total)
                .build();
    }
}
