package com.amore.aketer.external.channel;

import com.amore.aketer.domain.enums.ChannelType;
import com.amore.aketer.messaging.dto.MessagePayload;
import com.amore.aketer.messaging.dto.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class KakaoMessageSender implements MessageChannelSender {

    @Override
    public MessageResult send(MessagePayload payload) {
        //log.info("[MOCK] Sending Kakao message: userId={}, title={}", payload.getUserId(), payload.getTitle());

        boolean success = Math.random() > 0.1;

        return MessageResult.builder()
                .success(success)
                .messageId(success ? "KAKAO-" + UUID.randomUUID() : null)
                .retryable(!success)
                .errorCode(success ? null : "KAKAO_TEMP_ERROR")
                .errorMessage(success ? null : "Mock temporary error")
                .build();
    }

    @Override
    public ChannelType getChannelType() {
        return ChannelType.KAKAO;
    }
}
