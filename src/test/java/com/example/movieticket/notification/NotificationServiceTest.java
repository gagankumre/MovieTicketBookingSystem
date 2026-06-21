package com.example.movieticket.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.movieticket.config.NotificationProperties;
import com.example.movieticket.domain.Booking;
import com.example.movieticket.domain.NotificationOutbox;
import com.example.movieticket.domain.Show;
import com.example.movieticket.domain.enums.BookingStatus;
import com.example.movieticket.domain.enums.NotificationStatus;
import com.example.movieticket.domain.enums.NotificationType;
import com.example.movieticket.domain.enums.ShowType;
import com.example.movieticket.repository.BookingRepository;
import com.example.movieticket.repository.NotificationOutboxRepository;
import com.example.movieticket.support.factory.ShowFactory;
import com.example.movieticket.support.factory.UserFactory;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationOutboxRepository outboxRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private NotificationSender notificationSender;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(outboxRepository, bookingRepository,
                notificationSender, new NotificationProperties(0, 0, 60, 5));
    }

    private NotificationOutbox pendingNotification() {
        return new NotificationOutbox(NotificationType.BOOKING_CONFIRMATION, "a@b.com", "msg", "ref");
    }

    @Test
    void dispatchSendsPendingAndMarksSent() {
        NotificationOutbox notification = pendingNotification();
        when(outboxRepository.findByStatus(NotificationStatus.PENDING)).thenReturn(List.of(notification));

        int sent = notificationService.dispatchPending();

        assertThat(sent).isEqualTo(1);
        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.SENT);
        verify(notificationSender).send(notification);
    }

    @Test
    void dispatchRecordsFailedAttemptWhenSenderThrows() {
        NotificationOutbox notification = pendingNotification();
        when(outboxRepository.findByStatus(NotificationStatus.PENDING)).thenReturn(List.of(notification));
        doThrow(new RuntimeException("smtp down")).when(notificationSender).send(any());

        int sent = notificationService.dispatchPending();

        assertThat(sent).isZero();
        assertThat(notification.getAttempts()).isEqualTo(1);
        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.PENDING);
    }

    @Test
    void enqueueDueRemindersSkipsAlreadyRemindedBooking() {
        Booking booking = bookingForReminder();
        when(bookingRepository.findByStatusAndShow_StartTimeBetween(eq(BookingStatus.CONFIRMED), any(), any()))
                .thenReturn(List.of(booking));
        when(outboxRepository.existsByReferenceAndType("SHOW_REMINDER:7", NotificationType.SHOW_REMINDER))
                .thenReturn(true);

        int enqueued = notificationService.enqueueDueReminders(Instant.parse("2026-07-01T09:30:00Z"));

        assertThat(enqueued).isZero();
        verify(outboxRepository, never()).save(any());
    }

    @Test
    void enqueueDueRemindersCreatesReminderForNewBooking() {
        Booking booking = bookingForReminder();
        when(bookingRepository.findByStatusAndShow_StartTimeBetween(eq(BookingStatus.CONFIRMED), any(), any()))
                .thenReturn(List.of(booking));
        when(outboxRepository.existsByReferenceAndType("SHOW_REMINDER:7", NotificationType.SHOW_REMINDER))
                .thenReturn(false);

        int enqueued = notificationService.enqueueDueReminders(Instant.parse("2026-07-01T09:30:00Z"));

        assertThat(enqueued).isEqualTo(1);
        verify(outboxRepository).save(any(NotificationOutbox.class));
    }

    private Booking bookingForReminder() {
        Show show = ShowFactory.withId(9L, ShowFactory.show(null, null,
                Instant.parse("2026-07-01T10:00:00Z"), Instant.parse("2026-07-01T12:00:00Z"),
                ShowType.REGULAR, new BigDecimal("200.00")));
        Booking booking = new Booking(UserFactory.withId(1L, UserFactory.customer("a@b.com")), show,
                new BigDecimal("200.00"), BigDecimal.ZERO, new BigDecimal("200.00"), null,
                Instant.parse("2026-06-20T10:00:00Z"));
        booking.setId(7L);
        return booking;
    }
}
