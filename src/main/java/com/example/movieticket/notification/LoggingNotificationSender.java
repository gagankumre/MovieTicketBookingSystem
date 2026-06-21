package com.example.movieticket.notification;

import com.example.movieticket.domain.NotificationOutbox;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Stub sender that logs the notification in lieu of a real email/SMS provider. */
@Slf4j
@Component
public class LoggingNotificationSender implements NotificationSender {

    @Override
    public void send(NotificationOutbox notification) {
        log.info("Sending {} to {}: {}", notification.getType(), notification.getRecipient(),
                notification.getPayload());
    }
}
