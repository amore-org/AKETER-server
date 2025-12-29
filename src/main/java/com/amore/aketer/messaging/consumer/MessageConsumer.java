package com.amore.aketer.messaging.consumer;

import com.amore.aketer.messaging.config.RabbitMqConfig;
import com.amore.aketer.messaging.dto.MessagePayload;
import com.amore.aketer.messaging.dto.MessageResult;
import com.amore.aketer.messaging.publisher.MessagePublisher;
import com.amore.aketer.service.MessageSendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageConsumer {

    private final MessageSendService sendService;
    private final MessagePublisher publisher;

    @RabbitListener(queues = RabbitMqConfig.MESSAGE_QUEUE)
    public void consumeMessage(MessagePayload payload, @Header(name = "retryCount", required = false) Integer headerRetryCount) {

        log.info("메시지큐 처리: reservationId={}, retryCount={}", payload.getReservationId(), payload.getRetryCount());

        try {
            MessageResult result = sendService.sendMessage(payload);

            if (!result.isSuccess() && result.isRetryable()) {
                handleRetry(payload);
            } else if (!result.isSuccess()) {
                publisher.publishToDlq(payload, "Non-retryable error: " + result.getErrorMessage());
            }

        } catch (Exception e) {
            log.error("메시지 처리 중 예상치 못한 오류 발생: reservationId={}", payload.getReservationId(), e);
            handleRetry(payload);
        }
    }

    private void handleRetry(MessagePayload payload) {
        int currentRetry = payload.getRetryCount();

        // 최대 3번까지 재시도
        if (currentRetry >= 3) {
            log.warn("최대 재시도 횟수 초과: reservationId={}", payload.getReservationId());
            publisher.publishToDlq(payload, "최대 재시도 횟수 초과");
            return;
        }

        MessagePayload retryPayload = MessagePayload.from(payload, currentRetry + 1);

        long delay = calculateRetryDelay(currentRetry);
        publisher.publishToRetryQueue(retryPayload, delay);
    }

    private long calculateRetryDelay(int currentRetry) {
        return switch (currentRetry) {
            case 0 -> RabbitMqConfig.RETRY_DELAY_1;
            case 1 -> RabbitMqConfig.RETRY_DELAY_2;
            default -> RabbitMqConfig.RETRY_DELAY_3;
        };
    }
}
