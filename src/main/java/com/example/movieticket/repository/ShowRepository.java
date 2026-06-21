package com.example.movieticket.repository;

import com.example.movieticket.domain.Show;
import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShowRepository extends JpaRepository<Show, Long> {

    boolean existsByScreenIdAndStartTimeLessThanAndEndTimeGreaterThan(
            Long screenId, Instant end, Instant start);
}
