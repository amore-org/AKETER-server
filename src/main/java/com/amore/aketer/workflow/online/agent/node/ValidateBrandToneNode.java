package com.amore.aketer.workflow.online.agent.node;

import com.amore.aketer.domain.enums.ChannelType;
import com.amore.aketer.workflow.online.agent.state.ItemState;
import com.amore.aketer.workflow.online.agent.state.MessageState;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class ValidateBrandToneNode implements AsyncNodeAction<MessageState> {

    private final ChatClient chatClient;

    /**
     * LLM이 반환할 구조화 검증 결과
     */
    public record BrandToneValidationResponse(
            @JsonPropertyDescription("브랜드 톤/가이드라인 준수 여부. 준수하면 true")
            boolean valid,

            @JsonPropertyDescription("""
                    위반/개선 포인트 목록. fail일 때만 채워도 됨.
                    각 항목은 '무엇이 문제인지'가 한 문장으로 명확해야 함.
                    예: '존댓말 유지 필요', '과장/단정 표현(무조건/최고/100%) 완화 필요'
                    """)
            List<String> violations
    ) {}

    @Override
    public CompletableFuture<Map<String, Object>> apply(MessageState state) {

        String brand = nvl(state.getBrand());
        String purpose = nvl(state.getPurpose());
        ChannelType channel = state.getChannel();

        String brandGuidelines = nvl(state.getBrandGuidelines());

        ItemState product = state.getItem();

        String title = nvl(state.getMessageTitle());
        String body = nvl(state.getMessageBody());

        // ===== 1) 로컬 룰 기반 1차 검증(빠른 fail) =====
        List<String> localViolations = new ArrayList<>();

        if (title.isBlank()) localViolations.add("제목이 비어있음");
        if (body.isBlank()) localViolations.add("본문이 비어있음");
        if (!title.isBlank() && title.length() > 40) localViolations.add("제목 40자 초과");
        if (!body.isBlank() && body.length() > 350) localViolations.add("본문 350자 초과");

        // 브랜드 톤 검증인데, 메시지 형태가 깨져 있으면 톤 검증이 무의미하므로 바로 fail
        if (!localViolations.isEmpty()) {
            return CompletableFuture.completedFuture(Map.of(
                    MessageState.VALIDATION, "fail",
                    MessageState.BRAND_TONE_FAILURE_REASONS, localViolations
            ));
        }

        // ===== 2) LLM 기반 브랜드 톤 검증 =====
        BeanOutputConverter<BrandToneValidationResponse> converter =
                new BeanOutputConverter<>(BrandToneValidationResponse.class);

        String guidelineSection = brandGuidelines.isBlank()
                ? """
                   [브랜드 톤 가이드라인]
                   - (가이드라인이 제공되지 않았음) 기본적으로 과장/단정 표현을 피하고, 고객 친화적이며 신뢰감 있는 톤을 유지할 것.
                   - 지나친 느낌표/이모지 남발 금지.
                   """
                : """
                   [브랜드 톤 가이드라인]
                   %s
                   """.formatted(brandGuidelines);

        String prompt = """
                너는 CRM 카피 검수자야.
                아래 메시지가 '브랜드 톤 가이드라인'을 제대로 준수하는지 검증해.
                
                [검증 범위]
                - 문체/어조/호칭/존댓말/감탄/이모지/CTA 스타일 등 톤앤매너
                - 과장/단정/오해 소지 표현 여부 (예: 무조건, 100%%, 최고, 완벽, 즉시효과 등)
                - 채널 특성 반영 (SMS면 과도한 장식/줄바꿈/이모지 지양, KAKAO는 가독성 고려)
                
                [판정 기준]
                - 가이드라인 위반이 1개라도 있으면 valid=false
                - violations는 최대 5개까지, 가장 중요한 것부터
                - 각 violations는 '왜 문제인지'가 드러나게 한 문장으로 작성
                
                %s
                
                [메타 정보]
                - brand: %s
                - purpose: %s
                - channel: %s
                
                %s
                
                [검증 대상 메시지]
                - title: %s
                - body: %s
                
                {format}
                """.formatted(
                guidelineSection,
                brand, purpose, channel,
                product.toString(),
                title, body
        );

        try {
            BrandToneValidationResponse resp = chatClient.prompt()
                    .user(u -> u.text(prompt).param("format", converter.getFormat()))
                    .call()
                    .entity(converter);

            boolean valid = resp != null && resp.valid();
            List<String> violations = (resp == null || resp.violations() == null)
                    ? List.of()
                    : normalizeViolations(resp.violations(), 5);

            if (valid) {
                return CompletableFuture.completedFuture(Map.of(
                        MessageState.VALIDATION, "pass"
                ));
            }

            // LLM이 valid=false인데 violations가 비어있을 경우를 대비한 안전장치
            if (violations.isEmpty()) {
                violations = List.of("브랜드 톤 가이드라인 준수 여부가 불명확하여 재적용이 필요함");
            }

            return CompletableFuture.completedFuture(Map.of(
                    MessageState.VALIDATION, "fail",
                    MessageState.BRAND_TONE_FAILURE_REASONS, violations
            ));
        } catch (Exception e) {
            return CompletableFuture.completedFuture(Map.of(
                    MessageState.VALIDATION, "fail",
                    MessageState.BRAND_TONE_FAILURE_REASONS, List.of("브랜드 톤 검증 중 시스템 오류가 발생하여 재시도가 필요함: " + safeMsg(e))
            ));
        }
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }

    private static String safeMsg(Exception e) {
        String m = e.getMessage();
        if (m == null || m.isBlank()) return e.getClass().getSimpleName();
        // 너무 길면 로그/상태 오염 방지
        return m.length() > 200 ? m.substring(0, 200) + "..." : m;
    }

    /**
     * - 공백/중복 제거
     * - 너무 긴 항목 clamp
     * - 최대 n개 제한
     */
    private static List<String> normalizeViolations(List<String> raw, int max) {
        if (raw == null || raw.isEmpty()) return List.of();

        LinkedHashSet<String> set = new LinkedHashSet<>();
        for (String v : raw) {
            if (v == null) continue;
            String t = v.trim();
            if (t.isEmpty()) continue;
            if (t.length() > 120) t = t.substring(0, 119) + "…";
            set.add(t);
            if (set.size() >= max) break;
        }
        return new ArrayList<>(set);
    }
}
