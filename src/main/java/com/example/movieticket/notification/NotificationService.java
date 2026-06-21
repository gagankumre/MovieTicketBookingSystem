package com.example.movieticket.notification;

import com.example.movieticket.config.NotificationProperties;
import com.example.movieticket.domain.Booking;
import com.example.movieticket.domain.NotificationOutbox;
import com.example.movieticket.domain.enums.BookingStatus;
import com.example.movieticket.domain.enums.NotificationStatus;
import com.example.movieticket.domain.enums.NotificationType;
import com.example.movieticket.repository.BookingRepository;
import com.example.movieticket.repository.NotificationOutboxRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Outbox-based notifications. Enqueue methods run inside the booking transaction (durable, atomic,
 * non-blocking); {@link #dispatchPending()} and reminder enqueueing run off-thread via scheduled jobs.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationOutboxRepository outboxRepository;
    private final BookingRepository bookingRepository;
    private final NotificationSender notificationSender;
    private final NotificationProperties properties;

    public void enqueueBookingConfirmation(Booking booking) {
        enqueue(NotificationType.BOOKING_CONFIRMATION, booking,
                "Booking " + booking.getId() + " confirmed for show " + booking.getShow().getId());
    }

    public void enqueueBookingCancellation(Booking booking) {
        enqueue(NotificationType.BOOKING_CANCELLATION, booking,
                "Booking " + booking.getId() + " cancelled");
    }

    private void enqueue(NotificationType type, Booking booking, String payload) {
        String reference = type + ":" + booking.getId();
        outboxRepository.save(new NotificationOutbox(type, booking.getUser().getEmail(), payload, reference));
    }

    /** Sends every PENDING notification; failures are retried on later runs until max attempts. */
    @Transactional
    public int dispatchPending() {
        List<NotificationOutbox> pending = outboxRepository.findByStatus(NotificationStatus.PENDING);
        int sent = 0;
        for (NotificationOutbox notification : pending) {
            try {
                notificationSender.send(notification);
                notification.markSent();
                sent++;
            } catch (RuntimeException ex) {
                notification.recordFailedAttempt(properties.maxAttempts());
                log.warn("Notification {} delivery failed (attempt {}): {}",
                        notification.getId(), notification.getAttempts(), ex.getMessage());
            }
        }
        return sent;
    }

    /** Enqueues a one-off reminder for each confirmed booking whose show starts within the lead window. */
    @Transactional
    public int enqueueDueReminders(Instant now) {
        Instant windowEnd = now.plus(properties.reminderLeadMinutes(), ChronoUnit.MINUTES);
        List<Booking> due = bookingRepository.findByStatusAndShow_StartTimeBetween(
                BookingStatus.CONFIRMED, now, windowEnd);
        int enqueued = 0;
        for (Booking booking : due) {
            String reference = NotificationType.SHOW_REMINDER + ":" + booking.getId();
            if (outboxRepository.existsByReferenceAndType(reference, NotificationType.SHOW_REMINDER)) {
                continue;
            }
            outboxRepository.save(new NotificationOutbox(NotificationType.SHOW_REMINDER,
                    booking.getUser().getEmail(),
                    "Reminder: show " + booking.getShow().getId() + " starts soon", reference));
            enqueued++;
        }
        return enqueued;
    }
}
