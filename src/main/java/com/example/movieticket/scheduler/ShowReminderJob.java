package com.example.movieticket.scheduler;

import com.example.movieticket.notification.NotificationService;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Periodically enqueues pre-show reminders for confirmed bookings within the lead window. */
@Component
@RequiredArgsConstructor
public class ShowReminderJob {

    private final NotificationService notificationService;

    @Scheduled(fixedDelayString = "${app.notification.reminder-interval-ms}")
    public void enqueueReminders() {
        notificationService.enqueueDueReminders(Instant.now());
    }
}
