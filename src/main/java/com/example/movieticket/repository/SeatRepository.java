package com.example.movieticket.repository;

import com.example.movieticket.domain.Seat;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    boolean existsByScreenId(Long screenId);

    List<Seat> findByScreenIdOrderByRowLabelAscSeatNumberAsc(Long screenId);
}
