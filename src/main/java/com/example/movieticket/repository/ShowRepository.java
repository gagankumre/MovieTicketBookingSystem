package com.example.movieticket.repository;

import com.example.movieticket.domain.Show;
import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ShowRepository extends JpaRepository<Show, Long>, JpaSpecificationExecutor<Show> {

    boolean existsByScreenIdAndStartTimeLessThanAndEndTimeGreaterThan(
            Long screenId, Instant end, Instant start);
}
