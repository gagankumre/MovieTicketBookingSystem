package com.example.movieticket.web;

import com.example.movieticket.service.BookingService;
import com.example.movieticket.service.HoldService;
import com.example.movieticket.web.dto.BookingRequest;
import com.example.movieticket.web.dto.BookingResponse;
import com.example.movieticket.web.dto.HoldRequest;
import com.example.movieticket.web.dto.HoldResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authenticated customer actions under {@code /api/public}. These are non-GET, so the security
 * chain requires a valid token; the caller's email (JWT subject) identifies the owner.
 */
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class BookingController {

    private final HoldService holdService;
    private final BookingService bookingService;

    @PostMapping("/holds")
    @ResponseStatus(HttpStatus.CREATED)
    public HoldResponse hold(@AuthenticationPrincipal String email, @Valid @RequestBody HoldRequest request) {
        return holdService.holdSeats(email, request.getShowId(), request.getSeatIds());
    }

    @PostMapping("/bookings")
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse book(@AuthenticationPrincipal String email, @Valid @RequestBody BookingRequest request) {
        return bookingService.confirmBooking(email, request.getHoldId(),
                request.getDiscountCode(), request.getPaymentMethod());
    }

    @DeleteMapping("/holds/{holdId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void releaseHold(@AuthenticationPrincipal String email, @PathVariable Long holdId) {
        holdService.releaseHold(email, holdId);
    }
}
