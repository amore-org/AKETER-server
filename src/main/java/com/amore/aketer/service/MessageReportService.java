package com.amore.aketer.service;

import com.amore.aketer.external.slack.SlackNotificationService;
import com.amore.aketer.messaging.dto.MessagePayload;
import com.amore.aketer.messaging.dto.MessageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageReportService {

    private final SlackNotificationService slackService;

    private final AtomicLong successCount = new AtomicLong(0);
    private final AtomicLong failureCount = new AtomicLong(0);

    @Async
    public void recordSendResult(MessagePayload payload, MessageResult result) {
        if (result.isSuccess()) {
            successCount.incrementAndGet();
        } else {
            failureCount.incrementAndGet();
        }

        log.info("메시지 전송 결과: reservationId={}, 성공여부={}, 총 성공={}, 총 실패={}", payload.getReservationId(), result.isSuccess(), successCount.get(), failureCount.get());
    }

    @Scheduled(fixedRate = 300_000)
    public void sendPeriodicReport() {
        long success = successCount.getAndSet(0);
        long failure = failureCount.getAndSet(0);

        if (success > 0 || failure > 0) {
            slackService.sendReport(success, failure);
        }
    }

    public Map<String, Long> getCurrentStats() {
        return Map.of(
                "success", successCount.get(),
                "failure", failureCount.get()
        );
    }
}
