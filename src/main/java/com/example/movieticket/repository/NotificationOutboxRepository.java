package com.example.movieticket.repository;

import com.example.movieticket.domain.NotificationOutbox;
import com.example.movieticket.domain.enums.NotificationStatus;
import com.example.movieticket.domain.enums.NotificationType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationOutboxRepository extends JpaRepository<NotificationOutbox, Long> {

    List<NotificationOutbox> findByStatus(NotificationStatus status);

    boolean existsByReferenceAndType(String reference, NotificationType type);
}
