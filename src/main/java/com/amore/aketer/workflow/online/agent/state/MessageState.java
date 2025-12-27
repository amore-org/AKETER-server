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
    public static final String SRCH_DT = "srchDt";
    public static final String PERSONA_ID = "personaId";
    public static final String BRAND = "brand";
    public static final String PURPOSE = "purpose";
    public static final String CHANNEL = "channel";
    public static final String TITLE = "title";
    public static final String MESSAGE = "message";
    public static final String BRAND_GUIDELINES = "brandGuidelines";
    public static final String VALIDATION_RESULT = "validationResult";
    public static final String REGENERATION_ATTEMPT = "regenerationAttempt";
    public static final String FAILURE_REASONS = "failureReasons";
    public static final String TRACE_ID = "traceId";

    // Schema Definition
    public static final Map<String, Channel<?>> SCHEMA = Map.of(
            FAILURE_REASONS, Channels.appender(ArrayList::new)
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
    public String getSrchDt() {
        return this.<String>value(SRCH_DT).orElse(null);
    }

    public Long getPersonaId() {
        return this.<Long>value(PERSONA_ID).orElse(null);
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

    public String getTitle() {
        return this.<String>value(TITLE).orElse(null);
    }

    public String getMessage() {
        return this.<String>value(MESSAGE).orElse(null);
    }

    public String getBrandGuidelines() {
        return this.<String>value(BRAND_GUIDELINES).orElse(null);
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
