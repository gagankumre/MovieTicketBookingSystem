package com.example.movieticket.repository;

import com.example.movieticket.domain.Booking;
import com.example.movieticket.domain.enums.BookingStatus;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Booking> findByStatusAndShow_StartTimeBetween(BookingStatus status, Instant from, Instant to);
}
