package com.amore.aketer.workflow.online.agent.node;

import com.amore.aketer.workflow.online.agent.state.MessageState;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * ValidateEthicsPolicyNode - 메시지 윤리 강령 검증
 *
 * 역할:
 * 1. 메시지의 제목과 본문이 윤리 강령을 준수하는지 검증
 * 2. LLM을 통해 맥락 기반 검증 수행
 * 3. 검증 통과 시 VALIDATION="pass" 반환
 * 4. 검증 실패 시 VALIDATION="fail" 및 ETHICS_FAILURE_REASONS에 위반 사항 목록 반환
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ValidateEthicsPolicyNode implements AsyncNodeAction<MessageState> {

    private final ChatClient chatClient;

    /**
     * LLM 검증 응답 구조
     */
    record ValidationResult(
        @JsonProperty("isCompliant") boolean isCompliant,
        @JsonProperty("riskLevel") String riskLevel,
        @JsonProperty("violations") List<Violation> violations,
        @JsonProperty("overallAssessment") String overallAssessment
    ) {}

    record Violation(
        @JsonProperty("category") String category,
        @JsonProperty("expression") String expression,
        @JsonProperty("reason") String reason,
        @JsonProperty("legalBasis") String legalBasis,
        @JsonProperty("suggestion") String suggestion
    ) {}

    @Override
    public CompletableFuture<Map<String, Object>> apply(MessageState state) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();

            log.info("[ValidateEthicsPolicyNode] 윤리 강령 검증 시작 - title: {}",
                state.getMessageTitle());

            try {
                // 기본 null 체크
                String title = state.getMessageTitle();
                String body = state.getMessageBody();

                if (title == null || body == null) {
                    Map<String, Object> updates = new HashMap<>();
                    updates.put(MessageState.VALIDATION, "fail");
                    updates.put(MessageState.ETHICS_FAILURE_REASONS,
                        List.of("[시스템 오류] 제목 또는 본문이 없음"));
                    log.warn("[ValidateEthicsPolicyNode] 시스템 오류 - null 제목/본문");
                    return updates;
                }

                // LLM 기반 검증
                List<String> violations = validateByLLM(state);

                // 결과 반환
                Map<String, Object> updates = new HashMap<>();

                if (!violations.isEmpty()) {
                    // 검증 실패 - 라우팅 키값 "fail" 설정
                    updates.put(MessageState.VALIDATION, "fail");
                    updates.put(MessageState.ETHICS_FAILURE_REASONS, violations);
                    log.warn("[ValidateEthicsPolicyNode] 검증 실패 - {} 개 위반 사항 발견",
                        violations.size());
                    violations.forEach(v -> log.warn("  - {}", v));
                } else {
                    // 검증 성공 - 라우팅 키값 "pass" 설정
                    updates.put(MessageState.VALIDATION, "pass");
                    log.info("[ValidateEthicsPolicyNode] 검증 성공");
                }

                log.info("[ValidateEthicsPolicyNode] 완료 - elapsed: {}ms",
                    System.currentTimeMillis() - startTime);

                return updates;

            } catch (Exception e) {
                log.error("[ValidateEthicsPolicyNode] 검증 중 오류 발생: {}", e.getMessage(), e);

                // 오류 발생 시 보수적으로 실패 처리
                Map<String, Object> updates = new HashMap<>();
                updates.put(MessageState.VALIDATION, "fail");
                updates.put(MessageState.ETHICS_FAILURE_REASONS,
                    List.of("⚠️ 자동 검증 실패 → 수동 검토 필요: " + e.getMessage()));
                return updates;
            }
        });
    }

    /**
     * LLM 기반 검증
     */
    private List<String> validateByLLM(MessageState state) {
        try {
            // BeanOutputConverter 초기화
            var converter = new BeanOutputConverter<>(ValidationResult.class);

            // 프롬프트 템플릿 구성
            String promptTemplate = buildEthicsValidationPromptTemplate();

            // Spring AI fluent API로 LLM 호출 및 자동 파싱
            ValidationResult result = chatClient.prompt()
                .user(u -> u
                    .text(promptTemplate)
                    .param("messageTitle", state.getMessageTitle())
                    .param("messageBody", state.getMessageBody())
                    .param("ethicsGuidelines", state.getEthicsPolicyGuidelines() != null ?
                        state.getEthicsPolicyGuidelines() : "기본 윤리 강령 적용")
                    .param("format", converter.getFormat())
                )
                .call()
                .entity(converter);

            log.info("LLM 검증 결과 - isCompliant: {}, riskLevel: {}",
                result.isCompliant(), result.riskLevel());

            if (!result.isCompliant() || "HIGH".equals(result.riskLevel())) {
                List<String> llmViolations = new ArrayList<>();

                if (result.violations() != null) {
                    for (Violation v : result.violations()) {
                        llmViolations.add(String.format(
                            "[%s] '%s' → %s (권장: %s)",
                            v.category(), v.expression(), v.reason(), v.suggestion()
                        ));
                    }
                }

                return llmViolations;
            }

            return List.of();

        } catch (Exception e) {
            log.error("LLM 검증 중 오류 발생: {}", e.getMessage(), e);
            return List.of("LLM 검증 실패");
        }
    }

    /**
     * LLM 검증 프롬프트 템플릿 (Spring AI 플레이스홀더 사용)
     */
    private String buildEthicsValidationPromptTemplate() {
        return """
            당신은 광고 심의 및 법무 검토 전문가입니다. 다음 마케팅 메시지가 윤리 강령 및 법적 기준을 준수하는지 평가하세요.

            # 메시지
            **제목**: {messageTitle}
            **본문**:
            {messageBody}

            # 윤리 강령 및 법적 기준 (필수 참조)
            ```
            {ethicsGuidelines}
            ```

            # 검증 항목

            ## 1. 과장 광고 금지 (공정거래법)
            - [ ] "최고", "최초", "100%" 등 검증 불가능한 표현
            - [ ] 근거 없는 비교 우위 주장

            ## 2. 화장품법 준수
            - [ ] 의약품으로 오인될 표현 ("치료", "질병 예방" 등)
            - [ ] 기능성 화장품 아닌 경우 효능 주장 제한

            ## 3. 공정거래법
            - [ ] 소비자 오인 유발 표현
            - [ ] 경쟁사 비방

            ## 4. 개인정보보호법
            - [ ] 과도한 개인정보 요구

            ## 5. 사회적 책임
            - [ ] 차별적 표현
            - [ ] 외모 비하 또는 성적 대상화

            # 리스크 레벨 정의
            - **LOW**: 문제 없음
            - **MEDIUM**: 표현 수정 권장 (발송 가능)
            - **HIGH**: 법적 리스크 있음 (발송 불가, 반드시 수정)

            # 응답 형식
            {format}

            # 판단 기준
            - riskLevel이 HIGH인 경우 반드시 isCompliant: false
            - MEDIUM이더라도 violations가 3개 이상이면 isCompliant: false
            - 애매한 경우 보수적으로 판단하세요

            반드시 위 JSON 형식으로만 응답하세요.
            """;
    }
}