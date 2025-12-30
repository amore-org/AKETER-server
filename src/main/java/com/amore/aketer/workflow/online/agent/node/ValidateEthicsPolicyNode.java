package com.amore.aketer.workflow.online.agent.node;

import com.amore.aketer.workflow.online.agent.state.MessageState;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class ValidateEthicsPolicyNode implements AsyncNodeAction<MessageState> {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 금지 표현 사전
    private static final List<String> PROHIBITED_EXPRESSIONS = Arrays.asList(
        "치료", "질병", "100%"
    );

    @Override
    public CompletableFuture<Map<String, Object>> apply(MessageState state) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();

            log.info("[ValidateEthicsPolicyNode] 윤리 강령 검증 시작 - title: {}",
                state.getMessageTitle());

            try {
                List<String> violations = new ArrayList<>();

                // Phase 1: 룰 기반 검증
                violations.addAll(validateByRules(state));

                // Phase 2: LLM 기반 맥락 검증
                if (violations.isEmpty()) {
                    violations.addAll(validateByLLM(state));
                }

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
     * Phase 1: 룰 기반 검증
     */
    private List<String> validateByRules(MessageState state) {
        List<String> violations = new ArrayList<>();

        String title = state.getMessageTitle();
        String body = state.getMessageBody();

        if (title == null || body == null) {
            violations.add("[시스템 오류] 제목 또는 본문이 없음");
            return violations;
        }

        String fullMessage = title + " " + body;

        // 1. 금지 표현 사전 검증
        violations.addAll(checkProhibitedExpressions(fullMessage));

        // 2. 정규표현식 패턴 검증
        violations.addAll(detectSuspiciousPatterns(fullMessage));

        // 3. 산업별 규제 검증
        violations.addAll(validateCosmeticRegulations(fullMessage, state.getBrand()));

        return violations;
    }

    /**
     * 금지 표현 사전 검증
     */
    private List<String> checkProhibitedExpressions(String message) {
        List<String> violations = new ArrayList<>();

        for (String prohibited : PROHIBITED_EXPRESSIONS) {
            if (message.contains(prohibited)) {
                violations.add(String.format(
                    "[금지 표현] '%s' 사용 금지 → 제거 또는 완화 필요 (과장 광고 및 법적 리스크)",
                    prohibited
                ));
            }
        }

        return violations;
    }

    /**
     * 정규표현식 패턴 검증
     */
    private List<String> detectSuspiciousPatterns(String message) {
        List<String> violations = new ArrayList<>();

        // 패턴: 의학 용어
        Pattern medicalPattern = Pattern.compile("(치료|질병|질환)");
        Matcher matcher = medicalPattern.matcher(message);
        if (matcher.find()) {
            violations.add(String.format(
                "[의학적 효능 주장] '%s' 사용 금지 → 화장품법 위반 가능성",
                matcher.group()
            ));
        }

        return violations;
    }

    /**
     * 화장품법 규제 검증 (간소화 - 기획 구체화 전)
     */
    private List<String> validateCosmeticRegulations(String message, String brand) {
        List<String> violations = new ArrayList<>();
        // TODO: 기획 구체화 후 세부 규칙 추가

        return violations;
    }

    /**
     * Phase 2: LLM 기반 맥락 검증
     */
    private List<String> validateByLLM(MessageState state) {
        try {
            String prompt = buildEthicsValidationPrompt(state);

            // Spring AI ChatClient를 사용하여 LLM 호출
            String content = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

            log.debug("LLM 응답: {}", content);

			JsonNode result = objectMapper.readTree(content);

            boolean isCompliant = result.get("isCompliant").asBoolean();
            String riskLevel = result.get("riskLevel").asText();

            log.info("LLM 검증 결과 - isCompliant: {}, riskLevel: {}", isCompliant, riskLevel);

            if (!isCompliant || "HIGH".equals(riskLevel)) {
                List<String> llmViolations = new ArrayList<>();

                JsonNode violationsNode = result.get("violations");
                violationsNode.forEach(v -> {
                    String category = v.get("category").asText();
                    String expression = v.get("expression").asText();
                    String reason = v.get("reason").asText();
                    String suggestion = v.get("suggestion").asText();

                    llmViolations.add(String.format(
                        "[%s] '%s' → %s (권장: %s)",
                        category, expression, reason, suggestion
                    ));
                });

                return llmViolations;
            }

            return List.of();

        } catch (Exception e) {
            log.error("LLM 검증 중 오류 발생: {}", e.getMessage(), e);
            return List.of("LLM 검증 실패");
        }
    }

    /**
     * LLM 검증 프롬프트 구성
     */
    private String buildEthicsValidationPrompt(MessageState state) {
        return String.format("""
            당신은 광고 심의 및 법무 검토 전문가입니다. 다음 마케팅 메시지가 윤리 강령 및 법적 기준을 준수하는지 평가하세요.

            # 메시지
            **제목**: %s
            **본문**:
            %s

            # 윤리 강령 및 법적 기준 (필수 참조)
            ```
            %s
            ```

            # 검증 항목

            ## 1. 과장 광고 금지 (공정거래법)
            - [ ] "최고", "최초", "100%%" 등 검증 불가능한 표현
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

            # 응답 형식 (JSON)
            ```json
            {
              "isCompliant": true/false,
              "riskLevel": "LOW/MEDIUM/HIGH",
              "violations": [
                {
                  "category": "과장 광고/화장품법/공정거래법/개인정보/사회적 책임",
                  "expression": "문제가 되는 표현",
                  "reason": "위반 이유",
                  "legalBasis": "관련 법령",
                  "suggestion": "대체 표현"
                }
              ],
              "overallAssessment": "종합 평가"
            }
            ```

            # 판단 기준
            - riskLevel이 HIGH인 경우 반드시 isCompliant: false
            - MEDIUM이더라도 violations가 3개 이상이면 isCompliant: false
            - 애매한 경우 보수적으로 판단하세요
            """,
            state.getMessageTitle(),
            state.getMessageBody(),
            state.getEthicsPolicyGuidelines() != null ?
                state.getEthicsPolicyGuidelines() : "기본 윤리 강령 적용"
        );
    }
}