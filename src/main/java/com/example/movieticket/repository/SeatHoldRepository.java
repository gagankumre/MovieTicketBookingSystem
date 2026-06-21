package com.example.movieticket.repository;

import com.example.movieticket.domain.SeatHold;
import com.example.movieticket.domain.enums.HoldStatus;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatHoldRepository extends JpaRepository<SeatHold, Long> {

    List<SeatHold> findByStatusAndExpiresAtBefore(HoldStatus status, Instant cutoff);
}
