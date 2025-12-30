package com.amore.aketer.messaging.publisher;

import com.amore.aketer.messaging.config.RabbitMqConfig;
import com.amore.aketer.messaging.dto.MessagePayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessagePublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishMessage(MessagePayload payload) {
        log.info("큐에 메시지 전송: reservationId={}, retryCount={}", payload.getReservationId(), payload.getRetryCount());

        rabbitTemplate.convertAndSend(
                RabbitMqConfig.MESSAGE_EXCHANGE,
                RabbitMqConfig.ROUTING_KEY,
                payload,
                message -> {
                    message.getMessageProperties().setHeader("reservationId", payload.getReservationId());
                    message.getMessageProperties().setHeader("retryCount", payload.getRetryCount());
                    message.getMessageProperties().setHeader("traceId", payload.getTraceId());
                    return message;
                }
        );
    }

    public void publishToRetryQueue(MessagePayload payload, long delayMs) {
        log.info("큐에 메시지 재전송: reservationId={}, retryCount={}, delayMs={}", payload.getReservationId(), payload.getRetryCount(), delayMs);

        rabbitTemplate.convertAndSend(
                RabbitMqConfig.MESSAGE_EXCHANGE,
                RabbitMqConfig.RETRY_ROUTING_KEY,
                payload,
                message -> {
                    message.getMessageProperties().setExpiration(String.valueOf(delayMs));
                    message.getMessageProperties().setHeader("retryCount", payload.getRetryCount());
                    message.getMessageProperties().setHeader("traceId", payload.getTraceId());
                    return message;
                }
        );
    }

    public void publishToDlq(MessagePayload payload, String reason) {
        log.warn("DLQ에 메시지 전송: reservationId={}, reason={}", payload.getReservationId(), reason);

        rabbitTemplate.convertAndSend(
                RabbitMqConfig.DLQ_EXCHANGE,
                RabbitMqConfig.DLQ_ROUTING_KEY,
                payload,
                message -> {
                    message.getMessageProperties().setHeader("dlqReason", reason);
                    message.getMessageProperties().setHeader("failedAt", java.time.LocalDateTime.now().toString());
                    return message;
                }
        );
    }
}
