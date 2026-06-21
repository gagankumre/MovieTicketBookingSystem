package com.example.movieticket.repository;

import com.example.movieticket.domain.Payment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findFirstByBookingIdOrderByIdDesc(Long bookingId);
}
