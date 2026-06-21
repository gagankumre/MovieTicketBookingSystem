package com.example.movieticket.repository;

import com.example.movieticket.domain.BookingSeat;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingSeatRepository extends JpaRepository<BookingSeat, Long> {

    @Query("select bs from BookingSeat bs join fetch bs.showSeat ss join fetch ss.seat "
            + "where bs.booking.id = :bookingId order by ss.seat.rowLabel, ss.seat.seatNumber")
    List<BookingSeat> findByBookingId(@Param("bookingId") Long bookingId);
}
