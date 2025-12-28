package com.amore.aketer.external.channel;

import com.amore.aketer.domain.enums.ChannelType;
import com.amore.aketer.messaging.dto.MessagePayload;
import com.amore.aketer.messaging.dto.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class SmsMessageSender implements MessageChannelSender {

    @Override
    public MessageResult send(MessagePayload payload) {
        //log.info("[MOCK] Sending SMS: userId={}, body length={}", payload.getUserId(), payload.getBody().length());

        boolean success = Math.random() > 0.05;

        return MessageResult.builder()
                .success(success)
                .messageId(success ? "SMS-" + UUID.randomUUID() : null)
                .retryable(!success)
                .errorCode(success ? null : "SMS_GATEWAY_ERROR")
                .errorMessage(success ? null : "Mock gateway timeout")
                .build();
    }

    @Override
    public ChannelType getChannelType() {
        return ChannelType.SMS;
    }
}
