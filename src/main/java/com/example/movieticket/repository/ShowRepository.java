package com.example.movieticket.repository;

import com.example.movieticket.domain.Show;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShowRepository extends JpaRepository<Show, Long> {

    boolean existsByScreenIdAndStartTimeLessThanAndEndTimeGreaterThan(
            Long screenId, Instant end, Instant start);

    /** Browse shows with optional filters (null = ignore); associations fetched to avoid N+1. */
    @Query("""
            select s from Show s
            join fetch s.screen sc
            join fetch sc.theater t
            join fetch t.city c
            join fetch s.movie m
            where (:cityId is null or c.id = :cityId)
              and (:movieId is null or m.id = :movieId)
              and (:from is null or s.startTime >= :from)
              and (:to is null or s.startTime < :to)
            order by s.startTime
            """)
    List<Show> search(@Param("cityId") Long cityId, @Param("movieId") Long movieId,
                      @Param("from") Instant from, @Param("to") Instant to);
}
