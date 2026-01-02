package com.amore.aketer.domain.message;

import com.amore.aketer.domain.enums.MessageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MessageReservationRepository extends JpaRepository<MessageReservation, Long> {

    List<MessageReservation> findByStatusAndScheduledAtBefore(MessageStatus status, LocalDateTime now);

    List<MessageReservation> findByStatusOrderByScheduledAtDesc(MessageStatus status);

    List<MessageReservation> findByScheduledAtBetweenOrderByScheduledAtDesc(LocalDateTime start, LocalDateTime end);

    List<MessageReservation> findByStatus(MessageStatus status);

    @EntityGraph(attributePaths = {"persona", "message", "item", "item.detail"})
    @Query("""
    select mr
    from MessageReservation mr
    where mr.scheduledAt >= :start and mr.scheduledAt < :end
      and (:status is null or mr.status = :status)
      and (
          :productSearch is null
          or lower(mr.item.name) like concat('%', lower(:productSearch), '%')
      )
    """)
    Page<MessageReservation> findTodayReservations(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("status") MessageStatus status,
            @Param("productSearch") String productSearch,
            Pageable pageable
    );

}
