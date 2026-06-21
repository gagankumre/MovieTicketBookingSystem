package com.example.movieticket.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.notification")
public record NotificationProperties(
        long dispatchIntervalMs,
        long reminderIntervalMs,
        long reminderLeadMinutes,
        int maxAttempts) {
}
