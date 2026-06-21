package com.example.movieticket.repository;

import com.example.movieticket.domain.ShowSeat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShowSeatRepository extends JpaRepository<ShowSeat, Long> {

    long countByShowId(Long showId);
}
