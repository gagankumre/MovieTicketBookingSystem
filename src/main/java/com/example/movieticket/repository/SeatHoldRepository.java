package com.example.movieticket.repository;

import com.example.movieticket.domain.SeatHold;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatHoldRepository extends JpaRepository<SeatHold, Long> {
}
