package com.example.movieticket.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.example.movieticket.domain.Booking;
import com.example.movieticket.domain.BookingSeat;
import com.example.movieticket.domain.Payment;
import com.example.movieticket.domain.SeatHold;
import com.example.movieticket.domain.Show;
import com.example.movieticket.domain.ShowSeat;
import com.example.movieticket.domain.User;
import com.example.movieticket.domain.enums.BookingStatus;
import com.example.movieticket.domain.enums.SeatCategory;
import com.example.movieticket.domain.enums.ShowType;
import com.example.movieticket.exception.BusinessRuleException;
import com.example.movieticket.exception.HoldExpiredException;
import com.example.movieticket.exception.PaymentFailedException;
import com.example.movieticket.exception.UnauthorizedActionException;
import com.example.movieticket.mapper.BookingMapperImpl;
import com.example.movieticket.notification.NotificationService;
import com.example.movieticket.payment.PaymentGateway;
import com.example.movieticket.payment.PaymentResult;
import com.example.movieticket.repository.BookingRepository;
import com.example.movieticket.repository.BookingSeatRepository;
import com.example.movieticket.repository.PaymentRepository;
import com.example.movieticket.repository.RefundRepository;
import com.example.movieticket.repository.SeatHoldRepository;
import com.example.movieticket.repository.ShowSeatRepository;
import com.example.movieticket.repository.UserRepository;
import com.example.movieticket.support.factory.SeatFactory;
import com.example.movieticket.support.factory.ShowFactory;
import com.example.movieticket.support.factory.UserFactory;
import com.example.movieticket.web.dto.BookingResponse;
import com.example.movieticket.web.dto.CancellationResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private SeatHoldRepository seatHoldRepository;
    @Mock
    private ShowSeatRepository showSeatRepository;
    @Mock
    private SeatLockManager seatLockManager;
    @Mock
    private DiscountService discountService;
    @Mock
    private PaymentGateway paymentGateway;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private BookingSeatRepository bookingSeatRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RefundService refundService;
    @Mock
    private RefundRepository refundRepository;
    @Mock
    private NotificationService notificationService;

    private BookingService bookingService;

    private final User user = UserFactory.withId(1L, UserFactory.customer("alice@example.com"));
    private final Show show = ShowFactory.withId(9L, ShowFactory.show(null, null,
            Instant.parse("2026-07-01T10:00:00Z"), Instant.parse("2026-07-01T12:00:00Z"),
            ShowType.REGULAR, new BigDecimal("200.00")));

    @BeforeEach
    void setUp() {
        bookingService = new BookingService(seatHoldRepository, showSeatRepository, seatLockManager,
                discountService, paymentGateway, bookingRepository, bookingSeatRepository,
                paymentRepository, userRepository, refundService, refundRepository,
                notificationService, new BookingMapperImpl());
    }

    private SeatHold activeHold() {
        SeatHold hold = new SeatHold(user, show, Instant.parse("2026-07-01T09:55:00Z").plusSeconds(86400));
        hold.setId(55L);
        return hold;
    }

    private ShowSeat heldSeat(long id) {
        ShowSeat seat = new ShowSeat(show, SeatFactory.seat(null, "A", (int) id, SeatCategory.REGULAR),
                new BigDecimal("200.00"));
        seat.setId(id);
        seat.block(55L);
        return seat;
    }

    private void stubHeldSeats(ShowSeat... seats) {
        List<ShowSeat> list = List.of(seats);
        when(showSeatRepository.findByCurrentHoldId(55L)).thenReturn(list);
        when(seatLockManager.lockSeats(any())).thenReturn(list);
    }

    @Test
    void rejectsHoldOwnedByAnotherUser() {
        when(seatHoldRepository.findById(55L)).thenReturn(Optional.of(activeHold()));

        assertThatThrownBy(() -> bookingService.confirmBooking("mallory@example.com", 55L, null, "CARD"))
                .isInstanceOf(UnauthorizedActionException.class);
    }

    @Test
    void rejectsExpiredHold() {
        SeatHold hold = new SeatHold(user, show, Instant.parse("2000-01-01T00:00:00Z"));
        hold.setId(55L);
        when(seatHoldRepository.findById(55L)).thenReturn(Optional.of(hold));

        assertThatThrownBy(() -> bookingService.confirmBooking("alice@example.com", 55L, null, "CARD"))
                .isInstanceOf(HoldExpiredException.class);
    }

    @Test
    void declinedPaymentFailsBooking() {
        when(seatHoldRepository.findById(55L)).thenReturn(Optional.of(activeHold()));
        stubHeldSeats(heldSeat(101L));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setId(7L);
            return b;
        });
        when(paymentGateway.charge(eq(new BigDecimal("200.00")), anyString()))
                .thenReturn(PaymentResult.declined("declined"));

        assertThatThrownBy(() -> bookingService.confirmBooking("alice@example.com", 55L, null, "CARD"))
                .isInstanceOf(PaymentFailedException.class);
    }

    @Test
    void confirmsBookingWithDiscountAndComputesTotals() {
        when(seatHoldRepository.findById(55L)).thenReturn(Optional.of(activeHold()));
        stubHeldSeats(heldSeat(101L), heldSeat(102L));
        when(discountService.apply(eq("SAVE10"), eq(new BigDecimal("400.00")), any()))
                .thenReturn(new AppliedDiscount("SAVE10", new BigDecimal("40.00")));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setId(7L);
            return b;
        });
        when(paymentGateway.charge(eq(new BigDecimal("360.00")), anyString()))
                .thenReturn(PaymentResult.success("PAY-1"));

        BookingResponse response = bookingService.confirmBooking("alice@example.com", 55L, "SAVE10", "CARD");

        assertThat(response.getSubtotal()).isEqualByComparingTo("400.00");
        assertThat(response.getDiscountAmount()).isEqualByComparingTo("40.00");
        assertThat(response.getTotalAmount()).isEqualByComparingTo("360.00");
        assertThat(response.getStatus()).isEqualTo("CONFIRMED");
        assertThat(response.getSeats()).hasSize(2);
        assertThat(response.getPaymentReference()).isEqualTo("PAY-1");
    }

    private Booking confirmedBooking() {
        Booking booking = new Booking(user, show, new BigDecimal("200.00"), BigDecimal.ZERO,
                new BigDecimal("200.00"), null, Instant.parse("2026-06-20T10:00:00Z"));
        booking.setId(7L);
        return booking;
    }

    @Test
    void cancelRejectsNonOwner() {
        when(bookingRepository.findById(7L)).thenReturn(Optional.of(confirmedBooking()));

        assertThatThrownBy(() -> bookingService.cancelBooking("mallory@example.com", 7L))
                .isInstanceOf(UnauthorizedActionException.class);
    }

    @Test
    void cancelRejectsAlreadyCancelledBooking() {
        Booking booking = confirmedBooking();
        booking.casStatus(BookingStatus.CONFIRMED, BookingStatus.CANCELLED);
        when(bookingRepository.findById(7L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.cancelBooking("alice@example.com", 7L))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void cancelReleasesSeatsAndRecordsRefund() {
        Booking booking = confirmedBooking();
        ShowSeat seat = heldSeat(101L);
        seat.confirm(7L);
        BookingSeat bookingSeat = new BookingSeat(booking, seat, new BigDecimal("200.00"));
        Payment payment = new Payment(booking, new BigDecimal("200.00"), "CARD");
        payment.markSuccess("PAY-1");

        when(bookingRepository.findById(7L)).thenReturn(Optional.of(booking));
        when(bookingSeatRepository.findByBookingId(7L)).thenReturn(List.of(bookingSeat));
        when(seatLockManager.lockSeats(any())).thenReturn(List.of(seat));
        when(refundService.computeRefund(eq(new BigDecimal("200.00")), any(), any()))
                .thenReturn(new BigDecimal("200.00"));
        when(paymentRepository.findFirstByBookingIdOrderByIdDesc(7L)).thenReturn(Optional.of(payment));
        when(refundRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CancellationResponse response = bookingService.cancelBooking("alice@example.com", 7L);

        assertThat(response.getStatus()).isEqualTo("CANCELLED");
        assertThat(response.getRefundAmount()).isEqualByComparingTo("200.00");
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(payment.getStatus().name()).isEqualTo("REFUNDED");
    }
}
