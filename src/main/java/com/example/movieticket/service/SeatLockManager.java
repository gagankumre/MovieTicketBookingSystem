package com.example.movieticket.service;

import com.example.movieticket.domain.ShowSeat;
import com.example.movieticket.exception.ResourceNotFoundException;
import com.example.movieticket.exception.SeatUnavailableException;
import com.example.movieticket.repository.ShowSeatRepository;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Single choke point for seat-occupancy transitions. Every hold/book/release acquires a pessimistic
 * write lock on the target {@link ShowSeat} rows (in id order) before changing status, which
 * serializes concurrent attempts on the same seat. Callers must run inside a transaction.
 */
@Component
@RequiredArgsConstructor
public class SeatLockManager {

    private final ShowSeatRepository showSeatRepository;

    /** Locks the requested seats FOR UPDATE; throws if any id does not exist. */
    public List<ShowSeat> lockSeats(Collection<Long> seatIds) {
        List<Long> ordered = seatIds.stream().distinct().sorted().toList();
        List<ShowSeat> seats = showSeatRepository.findForUpdate(ordered);
        if (seats.size() != ordered.size()) {
            throw new ResourceNotFoundException("ShowSeat", ordered);
        }
        return seats;
    }

    /** Transitions already-locked seats AVAILABLE → HELD; fails fast if any is not available. */
    public void hold(List<ShowSeat> lockedSeats, Long holdId) {
        for (ShowSeat seat : lockedSeats) {
            if (!seat.block(holdId)) {
                throw new SeatUnavailableException("Seat " + seat.getId() + " is not available");
            }
        }
    }

    /** Releases already-locked seats back to AVAILABLE. */
    public void release(List<ShowSeat> lockedSeats) {
        lockedSeats.forEach(ShowSeat::release);
    }
}
