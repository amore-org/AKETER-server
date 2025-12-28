package com.amore.aketer.domain.message;

import com.amore.aketer.domain.enums.MessageStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MessageReservationRepository extends JpaRepository<MessageReservation, Long> {

    List<MessageReservation> findByStatusAndScheduledAtBefore(MessageStatus status, LocalDateTime now);

    List<MessageReservation> findByStatusOrderByScheduledAtDesc(MessageStatus status);

    List<MessageReservation> findByScheduledAtBetweenOrderByScheduledAtDesc(LocalDateTime start, LocalDateTime end);

    List<MessageReservation> findByStatus(MessageStatus status);
}
