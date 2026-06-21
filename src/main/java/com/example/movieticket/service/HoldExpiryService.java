package com.example.movieticket.service;

import com.example.movieticket.domain.SeatHold;
import com.example.movieticket.domain.ShowSeat;
import com.example.movieticket.domain.enums.HoldStatus;
import com.example.movieticket.repository.SeatHoldRepository;
import com.example.movieticket.repository.ShowSeatRepository;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Releases holds whose TTL has passed: each due hold's seats are locked, freed back to AVAILABLE,
 * and the hold marked EXPIRED. Driven by the scheduled sweeper, and also reusable directly.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HoldExpiryService {

    private final SeatHoldRepository seatHoldRepository;
    private final ShowSeatRepository showSeatRepository;
    private final SeatLockManager seatLockManager;

    @Transactional
    public int expireDueHolds(Instant now) {
        List<SeatHold> due = seatHoldRepository.findByStatusAndExpiresAtBefore(HoldStatus.ACTIVE, now);
        for (SeatHold hold : due) {
            List<Long> seatIds = showSeatRepository.findByCurrentHoldId(hold.getId()).stream()
                    .map(ShowSeat::getId)
                    .toList();
            if (!seatIds.isEmpty()) {
                seatLockManager.release(seatLockManager.lockSeats(seatIds));
            }
            hold.casStatus(HoldStatus.ACTIVE, HoldStatus.EXPIRED);
        }
        if (!due.isEmpty()) {
            log.info("Expired {} hold(s) due before {}", due.size(), now);
        }
        return due.size();
    }
}
