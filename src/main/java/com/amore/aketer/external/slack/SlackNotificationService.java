package com.amore.aketer.external.slack;

import com.amore.aketer.external.slack.dto.SlackMessage;
import com.amore.aketer.messaging.dto.MessagePayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SlackNotificationService {

    private final WebClient webClient;

    @Value("${aketer.slack.webhook-url}")
    private String webhookUrl;

    @Value("${aketer.slack.enabled}")
    private boolean enabled;

    @Async
    public void sendReport(long successCount, long failureCount) {
        if (!enabled) {
            log.debug("슬랙 알림 비활성화됨");
            return;
        }

        String text = String.format(
                "AKETER 메시지 발송 리포트\n성공: %d건\n실패: %d건",
                successCount, failureCount
        );

        SlackMessage message = SlackMessage.builder()
                .text(text)
                .build();

        sendToSlack(message);
    }

    @Async
    public void notifyDeadLetter(MessagePayload payload, String reason) {
        if (!enabled) {
            log.debug("슬랙 알림 비활성화됨");
            return;
        }

        String text = String.format(
                "DLQ 알림: 메시지 발송 최종 실패\nReservation ID: %d\nUser: %s\nChannel: %s\nReason: %s",
                payload.getReservationId(),
                payload.getPersonaId(),
                payload.getChannelType(),
                reason
        );

        SlackMessage message = SlackMessage.builder()
                .text(text)
                .attachments(List.of(
                        SlackMessage.Attachment.builder()
                                .color("danger")
                                .title("메시지 내용")
//                                .text(payload.getTitle() + "\n" + payload.getBody())
                                .build()
                ))
                .build();

        sendToSlack(message);
    }

    private void sendToSlack(SlackMessage message) {
        try {
            webClient.post()
                    .uri(webhookUrl)
                    .bodyValue(message)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("슬랙 알림 전송 성공");
        } catch (Exception e) {
            log.error("슬랙 알림 전송 실패", e);
        }
    }
}
