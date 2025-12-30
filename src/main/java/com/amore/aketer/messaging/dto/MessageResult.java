package com.amore.aketer.messaging.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MessageResult {

    private boolean success;
    private String messageId;
    private String errorCode;
    private String errorMessage;
    private boolean retryable;
}
