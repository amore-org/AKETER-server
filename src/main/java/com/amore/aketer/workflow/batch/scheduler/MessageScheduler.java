package com.amore.aketer.workflow.batch.scheduler;

import com.amore.aketer.domain.message.MessageReservation;
import com.amore.aketer.domain.message.MessageReservationRepository;
import com.amore.aketer.domain.enums.MessageStatus;
import com.amore.aketer.messaging.dto.MessagePayload;
import com.amore.aketer.messaging.publisher.MessagePublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageScheduler {

    private final MessageReservationRepository reservationRepository;
    private final MessagePublisher publisher;

    @Scheduled(fixedDelay = 30_000)
    @Transactional
    public void scheduleMessages() {
        LocalDateTime now = LocalDateTime.now();

        List<MessageReservation> readyMessages = reservationRepository.findByStatusAndScheduledAtBefore(
            MessageStatus.READY, now);

        if (readyMessages.isEmpty()) {
            log.debug("예정된 메시지가 없습니다 {}", now);
            return;
        }

        log.info("Scheduling {} messages", readyMessages.size());

        for (MessageReservation reservation : readyMessages) {
            try {
                reservation.pending();
                reservationRepository.save(reservation);

                MessagePayload payload = MessagePayload.of(reservation, UUID.randomUUID().toString());

                publisher.publishMessage(payload);

                log.info("메시지 배치 성공: reservationId={}", reservation.getId());

            } catch (ObjectOptimisticLockingFailureException e) {
                log.warn("다른 스케줄러에 의해 이미 실행되고 있습니다 reservationId={}", reservation.getId());
            } catch (Exception e) {
                log.error("메시지 배치 실패: reservationId={}", reservation.getId(), e);
            }
        }
    }
}
