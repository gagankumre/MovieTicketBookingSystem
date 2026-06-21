package com.example.movieticket.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.hold")
public record HoldProperties(long ttlMinutes, long sweepIntervalMs) {
}
