package com.example.movieticket.repository;

import com.example.movieticket.domain.ShowSeat;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShowSeatRepository extends JpaRepository<ShowSeat, Long> {

    long countByShowId(Long showId);

    @Query("""
            select ss from ShowSeat ss
            join fetch ss.seat seat
            where ss.show.id = :showId
            order by seat.rowLabel, seat.seatNumber
            """)
    List<ShowSeat> findSeatMap(@Param("showId") Long showId);
}
