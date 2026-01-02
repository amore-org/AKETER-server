package com.amore.aketer.workflow.online.agent.state;

import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MessageState extends AgentState {

	// 초기 입력 값
	public static final String PERSONA = "persona";
	public static final String PRODUCT = "product";
	public static final String BRAND = "brand";
	public static final String PURPOSE = "purpose";

	// 적절한 채널, 시간 정하는 노드 결과물
	public static final String CHANNEL = "channel";
	public static final String SEND_TIME = "sendTime";
    public static final String STRATEGY_REASON = "strategyReason";

	// 메시지 생성 노드 결과물
	public static final String MESSAGE_TITLE = "messageTitle";
	public static final String MESSAGE_BODY = "messageBody";

	// docs
	public static final String PRODUCT_INFORMATION = "productInformation";
	public static final String BRAND_GUIDELINES = "brandGuidelines";
    public static final String ETHICS_POLICY_KEYWORD = "ethicsPolicyKeyword";
	public static final String ETHICS_POLICY_GUIDELINES = "ethicsPolicyGuidelines";

    // validation
    public static final String VALIDATION = "validation";

	// Validation failure reasons
	public static final String DELIVERY_STRATEGY_FAILURE_REASONS = "deliveryStrategyFailureReasons";
    public static final String DRAFT_MESSAGE_FAILURE_REASONS = "draftMessageFailureReasons";
    public static final String BRAND_TONE_FAILURE_REASONS = "brandToneFailureReasons";
    public static final String ETHICS_FAILURE_REASONS = "ethicsFailureReasons";

	// Schema Definition
    public static final Map<String, Channel<?>> SCHEMA = Map.ofEntries(
        Map.entry(PERSONA, Channels.base(() -> "")),
        Map.entry(PRODUCT, Channels.base(() -> "")),
        Map.entry(BRAND, Channels.base(() -> "")),
        Map.entry(PURPOSE, Channels.base(() -> "")),
        Map.entry(CHANNEL, Channels.base(() -> "")),
        Map.entry(SEND_TIME, Channels.base(() -> LocalDateTime.MIN)),
        Map.entry(STRATEGY_REASON, Channels.base(() -> "")),
        Map.entry(MESSAGE_TITLE, Channels.base(() -> "")),
        Map.entry(MESSAGE_BODY, Channels.base(() -> "")),
        Map.entry(PRODUCT_INFORMATION, Channels.base(() -> "")),
        Map.entry(BRAND_GUIDELINES, Channels.base(() -> "")),
        Map.entry(ETHICS_POLICY_GUIDELINES, Channels.base(() -> "")),
        Map.entry(VALIDATION, Channels.base(() -> "")),
        Map.entry(DELIVERY_STRATEGY_FAILURE_REASONS, Channels.appender(ArrayList::new)),
        Map.entry(DRAFT_MESSAGE_FAILURE_REASONS, Channels.appender(ArrayList::new)),
        Map.entry(BRAND_TONE_FAILURE_REASONS, Channels.appender(ArrayList::new)),
        Map.entry(ETHICS_FAILURE_REASONS, Channels.appender(ArrayList::new))
    );

	public MessageState(Map<String, Object> initData) {
		super(initData);
	}

    // Getters - 초기 입력 값
    public String getPersona() {
        return this.<String>value(PERSONA).orElse(null);
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

    // Getters - 적절한 채널, 시간 정하는 노드 결과물
    public String getChannel() {
        return this.<String>value(CHANNEL).orElse(null);
    }

    public LocalDateTime getSendTime() {
        return this.<LocalDateTime>value(SEND_TIME).orElse(null);
    }

    public String getStrategyReason() {
        return this.<String>value(STRATEGY_REASON).orElse(null);
    }

    // Getters - 메시지 생성 노드 결과물
    public String getMessageTitle() {
        return this.<String>value(MESSAGE_TITLE).orElse(null);
    }

    public String getMessageBody() {
        return this.<String>value(MESSAGE_BODY).orElse(null);
    }

    // Getters - 문서 정보
    public String getProductInformation() {
        return this.<String>value(PRODUCT_INFORMATION).orElse(null);
    }

    public String getBrandGuidelines() {
        return this.<String>value(BRAND_GUIDELINES).orElse(null);
    }

    public String getEthicsPolicyKeyword() {
        return this.<String>value(ETHICS_POLICY_KEYWORD).orElse(null);
    }

    public String getEthicsPolicyGuidelines() {
        return this.<String>value(ETHICS_POLICY_GUIDELINES).orElse(null);
    }

    // Getters - 검증 성공 여부
    public String getValidation() {
        return this.<String>value(VALIDATION).orElse(null);
    }

    // Getters - 검증 실패 사유
    public List<String> getDeliveryStrategyFailureReasons() {
        return this.<List<String>>value(DELIVERY_STRATEGY_FAILURE_REASONS).orElse(new ArrayList<>());
    }

    public List<String> getDraftMessageFailureReasons() {
        return this.<List<String>>value(DRAFT_MESSAGE_FAILURE_REASONS).orElse(new ArrayList<>());
    }

    public List<String> getBrandToneFailureReasons() {
        return this.<List<String>>value(BRAND_TONE_FAILURE_REASONS).orElse(new ArrayList<>());
    }

    public List<String> getEthicsFailureReasons() {
        return this.<List<String>>value(ETHICS_FAILURE_REASONS).orElse(new ArrayList<>());
    }
}
