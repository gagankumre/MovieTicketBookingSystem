package com.example.movieticket.repository;

import com.example.movieticket.domain.ShowSeat;
import jakarta.persistence.LockModeType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShowSeatRepository extends JpaRepository<ShowSeat, Long> {

    long countByShowId(Long showId);

    /**
     * Loads the given show seats with a pessimistic write lock (SELECT ... FOR UPDATE), ordered by
     * id so concurrent callers acquire locks in the same order — preventing deadlocks. This is the
     * serialization point that makes double-allocation impossible.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select ss from ShowSeat ss where ss.id in :ids order by ss.id")
    List<ShowSeat> findForUpdate(@Param("ids") List<Long> ids);

    List<ShowSeat> findByCurrentHoldId(Long holdId);

    @Query("""
            select ss from ShowSeat ss
            join fetch ss.seat seat
            where ss.show.id = :showId
            order by seat.rowLabel, seat.seatNumber
            """)
    List<ShowSeat> findSeatMap(@Param("showId") Long showId);
}
