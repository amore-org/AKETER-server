package com.amore.aketer.messaging.handler;

import com.amore.aketer.domain.message.MessageReservationRepository;
import com.amore.aketer.external.slack.SlackNotificationService;
import com.amore.aketer.messaging.config.RabbitMqConfig;
import com.amore.aketer.messaging.dto.MessagePayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeadLetterHandler {

    private final MessageReservationRepository reservationRepository;
    private final SlackNotificationService slackService;

    @RabbitListener(queues = RabbitMqConfig.MESSAGE_DLQ)
    public void handleDeadLetter(MessagePayload payload,
                                  @Header(name = "dlqReason", required = false) String reason,
                                  @Header(name = "failedAt", required = false) String failedAt) {

        log.error("Dead letter 수신: reservationId={}, reason={}, failedAt={}", payload.getReservationId(), reason, failedAt);

        slackService.notifyDeadLetter(payload, reason);
    }
}
