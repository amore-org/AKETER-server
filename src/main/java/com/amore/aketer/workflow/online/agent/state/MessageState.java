package com.amore.aketer.workflow.online.agent.state;

import com.amore.aketer.workflow.online.dto.ValidationResult;
import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MessageState extends AgentState {

    // State Keys
    public static final String PERSONA_ID = "personaId";
    public static final String PRODUCT = "product";
    public static final String BRAND = "brand";
    public static final String PURPOSE = "purpose";
    public static final String CHANNEL = "channel";
    public static final String SEND_TIME = "sendTime";
    public static final String TITLE = "title";
    public static final String MESSAGE = "message";
    public static final String BRAND_GUIDELINES = "brandGuidelines";
    public static final String IS_REFRESH = "isRefresh";
    public static final String VALIDATION_RESULT = "validationResult";
    public static final String REGENERATION_ATTEMPT = "regenerationAttempt";
    public static final String FAILURE_REASONS = "failureReasons";
    public static final String TRACE_ID = "traceId";

    // Schema Definition
    public static final Map<String, Channel<?>> SCHEMA = Map.ofEntries(
            Map.entry(PERSONA_ID, Channels.base(() -> 0L)),
            Map.entry(PRODUCT, Channels.base(() -> "")),
            Map.entry(BRAND, Channels.base(() -> "")),
            Map.entry(PURPOSE, Channels.base(() -> "")),
            Map.entry(CHANNEL, Channels.base(() -> "")),
            Map.entry(SEND_TIME, Channels.base(() -> "")),
            Map.entry(TITLE, Channels.base(() -> "")),
            Map.entry(MESSAGE, Channels.base(() -> "")),
            Map.entry(BRAND_GUIDELINES, Channels.base(() -> "")),
            Map.entry(IS_REFRESH, Channels.base(() -> false)),
            Map.entry(VALIDATION_RESULT, Channels.base(() -> "")),
            Map.entry(REGENERATION_ATTEMPT, Channels.base(() -> 0)),
            Map.entry(FAILURE_REASONS, Channels.appender(ArrayList::new))
    );

    public MessageState(Map<String, Object> initData) {
        super(ensureTraceId(initData));
    }

    private static Map<String, Object> ensureTraceId(Map<String, Object> data) {
        if (data.containsKey(TRACE_ID)) {
            return data;
        }

        Map<String, Object> newData = new HashMap<>(data);
        newData.put(TRACE_ID, UUID.randomUUID().toString());
        return newData;
    }

    // Getters
    public Long getPersonaId() {
        return this.<Long>value(PERSONA_ID).orElse(null);
    }

    public String getProduct() {
        return this.<String>value(PRODUCT).orElse(null);
    }

    public String getBrand() {
        return this.<String>value(BRAND).orElse(null);
    }

    public String getPurpose() {
        return this.<String>value(PURPOSE).orElse(null);
    }

    public String getChannel() {
        return this.<String>value(CHANNEL).orElse(null);
    }

    public String getSendTime() {
        return this.<String>value(SEND_TIME).orElse(null);
    }

    public String getTitle() {
        return this.<String>value(TITLE).orElse(null);
    }

    public String getMessage() {
        return this.<String>value(MESSAGE).orElse(null);
    }

    public String getBrandGuidelines() {
        return this.<String>value(BRAND_GUIDELINES).orElse(null);
    }

    public boolean getIsRefresh() {
        return this.<Boolean>value(IS_REFRESH).orElse(false);
    }

    public ValidationResult getValidationResult() {
        return this.<ValidationResult>value(VALIDATION_RESULT).orElse(null);
    }

    public int getRegenerationAttempt() {
        return this.<Integer>value(REGENERATION_ATTEMPT).orElse(0);
    }

    public List<String> getFailureReasons() {
        return this.<List<String>>value(FAILURE_REASONS).orElse(List.of());
    }

    public String getTraceId() {
        return this.<String>value(TRACE_ID)
                .orElseThrow(() -> new IllegalStateException("traceId must be set in constructor"));
    }

    // Helper Methods
    public boolean isValidationPassed() {
        ValidationResult result = getValidationResult();
        return result != null && result.isValid();
    }

    public boolean isMaxRegenerationAttemptsExceeded() {
        return getRegenerationAttempt() >= 3;
    }
}
