package com.example.movieticket.service;

import com.example.movieticket.domain.Booking;
import com.example.movieticket.domain.BookingSeat;
import com.example.movieticket.domain.Payment;
import com.example.movieticket.domain.Refund;
import com.example.movieticket.domain.SeatHold;
import com.example.movieticket.domain.ShowSeat;
import com.example.movieticket.domain.User;
import com.example.movieticket.domain.enums.BookingStatus;
import com.example.movieticket.domain.enums.HoldStatus;
import com.example.movieticket.domain.enums.SeatStatus;
import com.example.movieticket.exception.BusinessRuleException;
import com.example.movieticket.exception.HoldExpiredException;
import com.example.movieticket.exception.PaymentFailedException;
import com.example.movieticket.exception.ResourceNotFoundException;
import com.example.movieticket.exception.SeatUnavailableException;
import com.example.movieticket.exception.UnauthorizedActionException;
import com.example.movieticket.mapper.BookingMapper;
import com.example.movieticket.payment.PaymentGateway;
import com.example.movieticket.payment.PaymentResult;
import com.example.movieticket.repository.BookingRepository;
import com.example.movieticket.repository.BookingSeatRepository;
import com.example.movieticket.repository.PaymentRepository;
import com.example.movieticket.repository.RefundRepository;
import com.example.movieticket.repository.SeatHoldRepository;
import com.example.movieticket.repository.ShowSeatRepository;
import com.example.movieticket.repository.UserRepository;
import com.example.movieticket.web.dto.BookingResponse;
import com.example.movieticket.web.dto.BookingSummaryResponse;
import com.example.movieticket.web.dto.CancellationResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final SeatHoldRepository seatHoldRepository;
    private final ShowSeatRepository showSeatRepository;
    private final SeatLockManager seatLockManager;
    private final DiscountService discountService;
    private final PaymentGateway paymentGateway;
    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final RefundService refundService;
    private final RefundRepository refundRepository;
    private final BookingMapper bookingMapper;

    /**
     * Converts an active hold into a confirmed, paid booking. Runs in one transaction so a payment
     * failure (or any error) rolls back the booking, discount redemption, and seat transitions.
     */
    @Transactional
    public BookingResponse confirmBooking(String userEmail, Long holdId, String discountCode, String paymentMethod) {
        SeatHold hold = seatHoldRepository.findById(holdId)
                .orElseThrow(() -> new ResourceNotFoundException("Hold", holdId));
        if (!hold.getUser().getEmail().equals(userEmail)) {
            throw new UnauthorizedActionException("Hold " + holdId + " does not belong to the current user");
        }
        Instant now = Instant.now();
        if (hold.getStatus() != HoldStatus.ACTIVE || hold.isExpired(now)) {
            throw new HoldExpiredException("Hold " + holdId + " is no longer active");
        }

        List<Long> seatIds = showSeatRepository.findByCurrentHoldId(holdId).stream().map(ShowSeat::getId).toList();
        if (seatIds.isEmpty()) {
            throw new HoldExpiredException("Hold " + holdId + " has no held seats");
        }
        List<ShowSeat> seats = seatLockManager.lockSeats(seatIds);
        for (ShowSeat seat : seats) {
            if (seat.getStatus() != SeatStatus.HELD || !holdId.equals(seat.getCurrentHoldId())) {
                throw new HoldExpiredException("Hold " + holdId + " is no longer valid for seat " + seat.getId());
            }
        }

        BigDecimal subtotal = seats.stream().map(ShowSeat::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal discountAmount = BigDecimal.ZERO;
        String appliedCode = null;
        if (discountCode != null && !discountCode.isBlank()) {
            AppliedDiscount applied = discountService.apply(discountCode, subtotal, now);
            discountAmount = applied.amount();
            appliedCode = applied.code();
        }
        BigDecimal total = subtotal.subtract(discountAmount);

        Booking booking = bookingRepository.save(
                new Booking(hold.getUser(), hold.getShow(), subtotal, discountAmount, total, appliedCode, now));

        PaymentResult result = paymentGateway.charge(total, paymentMethod);
        if (!result.success()) {
            throw new PaymentFailedException(result.failureReason());
        }
        Payment payment = new Payment(booking, total, paymentMethod);
        payment.markSuccess(result.reference());
        paymentRepository.save(payment);

        List<BookingSeat> bookingSeats = new ArrayList<>();
        for (ShowSeat seat : seats) {
            if (!seat.confirm(booking.getId())) {
                throw new SeatUnavailableException("Seat " + seat.getId() + " could not be confirmed");
            }
            bookingSeats.add(new BookingSeat(booking, seat, seat.getPrice()));
        }
        bookingSeatRepository.saveAll(bookingSeats);
        hold.casStatus(HoldStatus.ACTIVE, HoldStatus.CONVERTED);

        log.info("Confirmed booking id={} from hold {} total={}", booking.getId(), holdId, total);
        return toResponse(booking, bookingSeats, payment);
    }

    /**
     * Cancels a confirmed booking (owner only): releases its seats, computes the policy refund,
     * issues a mock-gateway refund, and records it. {@code @Version} on the booking guards against
     * a concurrent double-cancel (the loser fails with a 409).
     */
    @Transactional
    public CancellationResponse cancelBooking(String userEmail, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));
        if (!booking.getUser().getEmail().equals(userEmail)) {
            throw new UnauthorizedActionException("Booking " + bookingId + " does not belong to the current user");
        }
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BusinessRuleException("Booking " + bookingId + " is not confirmed");
        }

        Instant now = Instant.now();
        List<Long> seatIds = bookingSeatRepository.findByBookingId(bookingId).stream()
                .map(bookingSeat -> bookingSeat.getShowSeat().getId())
                .toList();
        if (!seatIds.isEmpty()) {
            seatLockManager.release(seatLockManager.lockSeats(seatIds));
        }

        BigDecimal refundAmount = refundService.computeRefund(
                booking.getTotalAmount(), booking.getShow().getStartTime(), now);
        Payment payment = paymentRepository.findFirstByBookingIdOrderByIdDesc(bookingId).orElse(null);
        if (payment != null && refundAmount.signum() > 0) {
            paymentGateway.refund(payment.getGatewayRef(), refundAmount);
            payment.markRefunded();
        }
        Refund refund = refundRepository.save(new Refund(booking, refundAmount, now));
        booking.casStatus(BookingStatus.CONFIRMED, BookingStatus.CANCELLED);

        log.info("Cancelled booking {} refund={}", bookingId, refundAmount);
        return CancellationResponse.builder()
                .bookingId(bookingId)
                .status(booking.getStatus().name())
                .refundAmount(refundAmount)
                .refundStatus(refund.getStatus().name())
                .build();
    }

    @Transactional(readOnly = true)
    public List<BookingSummaryResponse> getMyBookings(String userEmail) {
        User customer = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));
        return bookingMapper.toSummaryList(bookingRepository.findByUserIdOrderByCreatedAtDesc(customer.getId()));
    }

    @Transactional(readOnly = true)
    public BookingResponse getBooking(String userEmail, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));
        if (!booking.getUser().getEmail().equals(userEmail)) {
            throw new UnauthorizedActionException("Booking " + bookingId + " does not belong to the current user");
        }
        List<BookingSeat> bookingSeats = bookingSeatRepository.findByBookingId(bookingId);
        Payment payment = paymentRepository.findFirstByBookingIdOrderByIdDesc(bookingId).orElse(null);
        return toResponse(booking, bookingSeats, payment);
    }

    private BookingResponse toResponse(Booking booking, List<BookingSeat> bookingSeats, Payment payment) {
        return BookingResponse.builder()
                .bookingId(booking.getId())
                .showId(booking.getShow().getId())
                .status(booking.getStatus().name())
                .subtotal(booking.getSubtotal())
                .discountAmount(booking.getDiscountAmount())
                .totalAmount(booking.getTotalAmount())
                .discountCode(booking.getDiscountCode())
                .createdAt(booking.getCreatedAt())
                .seats(bookingMapper.toBookedSeats(bookingSeats))
                .paymentStatus(payment == null ? null : payment.getStatus().name())
                .paymentReference(payment == null ? null : payment.getGatewayRef())
                .build();
    }
}
