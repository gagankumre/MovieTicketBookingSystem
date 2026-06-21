package com.example.movieticket.notification;

import com.example.movieticket.domain.NotificationOutbox;

/** Delivers a notification (email/SMS). The project ships a logging stub. */
public interface NotificationSender {

    void send(NotificationOutbox notification);
}
