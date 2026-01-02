package com.amore.aketer.workflow.online.agent.node;

import com.amore.aketer.workflow.online.agent.state.MessageState;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * RegenerationNode - 윤리 강령 검증 실패 시 메시지 재생성
 *
 * 역할:
 * 1. ValidateEthicsPolicyNode에서 "fail" 판정 시 호출됨
 * 2. 실패 사유(ETHICS_FAILURE_REASONS)를 분석
 * 3. LLM을 통해 윤리 강령을 준수하도록 메시지 재작성
 * 4. 새로운 MESSAGE_TITLE, MESSAGE_BODY 반환
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegenerationNode implements AsyncNodeAction<MessageState> {

    private final ChatClient chatClient;

    /**
     * 재생성 응답 구조
     */
    record RegenerationResponse(
        @JsonProperty("title") String title,
        @JsonProperty("body") String body,
        @JsonProperty("changes") List<String> changes,
        @JsonProperty("rationale") String rationale
    ) {}

    @Override
    public CompletableFuture<Map<String, Object>> apply(MessageState state) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();

            log.info("[RegenerationNode] 메시지 재생성 시작 - title: {}",
                state.getMessageTitle());

            try {
                // 1. 실패 사유 추출
                List<String> failureReasons = state.getEthicsFailureReasons();
                if (failureReasons == null || failureReasons.isEmpty()) {
                    log.warn("[RegenerationNode] 실패 사유가 없음 - 재생성 불가");
                    return Map.of(); // 변경 없이 반환
                }

                // 2. 기존 메시지 정보 추출
                String oldTitle = state.getMessageTitle();
                String oldBody = state.getMessageBody();
                String brand = state.getBrand();
                String brandGuidelines = state.getBrandGuidelines();
                String ethicsGuidelines = state.getEthicsPolicyGuidelines();
                String persona = state.getPersona();
                String product = state.getItem();

                // 3. BeanOutputConverter 초기화
                var converter = new BeanOutputConverter<>(RegenerationResponse.class);

                // 4. 재생성 프롬프트 템플릿 구성
                String promptTemplate = buildRegenerationPromptTemplate();

                log.info("[RegenerationNode] LLM 호출 - 메시지 재생성 중...");

                // 5. Spring AI fluent API로 LLM 호출 및 자동 파싱
                RegenerationResponse result = chatClient.prompt()
                    .user(u -> u
                        .text(promptTemplate)
                        .param("oldTitle", oldTitle != null ? oldTitle : "(제목 없음)")
                        .param("oldBody", oldBody != null ? oldBody : "(본문 없음)")
                        .param("brand", brand != null ? brand : "(브랜드 정보 없음)")
                        .param("brandGuidelines", brandGuidelines != null ? brandGuidelines : "(브랜드 가이드라인 없음)")
                        .param("persona", persona != null ? persona : "일반 고객")
                        .param("product", product != null ? product : "(제품 정보 없음)")
                        .param("failureReasons", formatFailureReasons(failureReasons))
                        .param("ethicsGuidelines", ethicsGuidelines != null ? ethicsGuidelines : "기본 윤리 강령 적용")
                        .param("format", converter.getFormat())
                    )
                    .call()
                    .entity(converter);

                // 6. 응답 검증 (빈 값 체크)
                if (result.title() == null || result.title().isBlank() ||
                    result.body() == null || result.body().isBlank()) {
                    log.error("[RegenerationNode] LLM 응답에 빈 제목/본문 포함 - 재생성 실패");
                    throw new IllegalStateException("LLM이 빈 제목 또는 본문을 반환했습니다");
                }

                log.info("[RegenerationNode] 재생성 완료:");
                log.info("  기존 제목: {}", oldTitle);
                log.info("  새 제목: {}", result.title());
                log.info("  기존 본문: {}", oldBody);
                log.info("  새 본문: {}", result.body());

                // 변경 사항 로깅
                if (result.changes() != null && !result.changes().isEmpty()) {
                    log.info("  변경 사항:");
                    result.changes().forEach(change -> log.info("    - {}", change));
                }
                if (result.rationale() != null) {
                    log.info("  재작성 근거: {}", result.rationale());
                }

                // 8. 업데이트 반환
                Map<String, Object> updates = new HashMap<>();
                updates.put(MessageState.MESSAGE_TITLE, result.title());
                updates.put(MessageState.MESSAGE_BODY, result.body());

                log.info("[RegenerationNode] 완료 - elapsed: {}ms",
                    System.currentTimeMillis() - startTime);

                return updates;

            } catch (Exception e) {
                log.error("[RegenerationNode] 재생성 중 오류 발생: {}", e.getMessage(), e);

                Map<String, Object> updates = new HashMap<>();
                updates.put(MessageState.VALIDATION, "fail");
                updates.put(MessageState.ETHICS_FAILURE_REASONS, List.of(
                    "⚠️ 자동 재생성 실패: " + e.getMessage()
                ));

                return updates;
            }
        });
    }

    /**
     * 실패 사유 포맷팅 (리스트 → 문자열)
     */
    private String formatFailureReasons(List<String> failureReasons) {
        return failureReasons.stream()
            .map(reason -> "- " + reason)
            .collect(Collectors.joining("\n"));
    }

    /**
     * 재생성 프롬프트 템플릿 (Spring AI 플레이스홀더 사용)
     */
    private String buildRegenerationPromptTemplate() {
        return """
            당신은 마케팅 메시지 전문 작성자입니다. 다음 메시지가 윤리 강령 검증에서 실패했습니다.
            실패 사유를 모두 해결하여 메시지를 재작성하세요.

            # 기존 메시지
            **제목**: {oldTitle}
            **본문**:
            {oldBody}

            # 브랜드
            {brand}

            # 브랜드 가이드라인 (반드시 준수)
            ```
            {brandGuidelines}
            ```

            위 브랜드 가이드라인에 명시된 톤앤매너, 커뮤니케이션 스타일, 용어 사용 규칙을 반드시 준수하세요.

            # 타겟 페르소나
            {persona}

            # 제품 정보
            {product}

            # 실패 사유 (반드시 모두 해결할 것)
            {failureReasons}

            # 윤리 강령
            ```
            {ethicsGuidelines}
            ```

            # 재작성 요구사항

            ## 필수 사항
            1. **실패 사유 모두 해결**: 위에 나열된 모든 실패 사유를 해결할 것
            2. **원래 의도 유지**: 메시지의 핵심 목적과 의도는 유지할 것
            3. **브랜드 가이드라인 준수**: 브랜드 가이드라인에 명시된 모든 규칙을 반드시 따를 것
            4. **윤리 강령 준수**: 모든 윤리 강령을 완벽히 준수할 것
            5. **브랜드 톤앤매너 유지**: 브랜드의 고유한 커뮤니케이션 스타일과 어조를 반드시 유지할 것
            6. **타겟 페르소나 고려**: 페르소나에 맞는 톤앤매너 유지

            ## 금지 사항
            - 과장 광고 표현 (최고, 최초, 100% 등)
            - 의학적 효능 주장 (치료, 질병, 질환 등)
            - 검증 불가능한 비교 표현
            - 타사 비방 또는 비교

            ## 권장 사항
            - 구체적이고 검증 가능한 표현 사용
            - 브랜드의 고유한 커뮤니케이션 스타일 유지
            - 부드럽고 긍정적인 톤 유지
            - 명확하고 간결한 문장 구조
            - 고객 혜택 중심으로 작성

            # 응답 형식
            {format}

            이제 위 기존 메시지를 재작성하세요. 반드시 위 JSON 형식으로만 응답하세요.
            """;
    }
}
