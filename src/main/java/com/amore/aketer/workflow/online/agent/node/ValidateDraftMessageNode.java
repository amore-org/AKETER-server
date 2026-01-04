package com.amore.aketer.workflow.online.agent.node;

import com.amore.aketer.workflow.online.agent.state.ItemState;
import com.amore.aketer.workflow.online.agent.state.MessageState;
import com.amore.aketer.workflow.online.agent.state.PersonaState;
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
public class ValidateDraftMessageNode implements AsyncNodeAction<MessageState> {

    private final ChatClient chatClient;

    /**
     * LLM 구조화 검증 응답
     */
    public record DraftMessageValidationResponse(
            @JsonPropertyDescription("초안이 CRM 메시지로 적합하면 true")
            boolean valid,

            @JsonPropertyDescription("""
                    부적합 사유/개선 포인트 목록. fail일 때만 채워도 됨.
                    최대 5개. 각 항목은 한 문장으로 명확히.
                    예: '제목이 상품과 무관함', '혜택/CTA가 불명확함', '채널 대비 문장이 너무 김'
                    """)
            List<String> violations
    ) {}

    @Override
    public CompletableFuture<Map<String, Object>> apply(MessageState state) {

        PersonaState persona = state.getPersona();
        ItemState item = state.getItem();

        String purpose = nvl(state.getPurpose());
        String channel = state.getChannel() != null ? state.getChannel().name() : "";

        String title = nvl(state.getMessageTitle());
        String body = nvl(state.getMessageBody());

        // ===== 1) 로컬 룰 기반 1차 검증 =====
        List<String> local = new ArrayList<>();

        // 필수값
        if (title.isBlank()) local.add("제목이 비어있음");
        if (body.isBlank()) local.add("본문이 비어있음");

        // 길이 (요구사항: 제목<=40, 본문<=350)
        if (!title.isBlank() && title.length() > 40) local.add("제목 40자 초과");
        if (!body.isBlank() && body.length() > 350) local.add("본문 350자 초과");

        // 최소 의미 체크(너무 짧은 경우)
        if (!body.isBlank() && body.length() < 20) local.add("본문이 너무 짧아 메시지로서 정보가 부족함");

        // 채널별 기본 가이드
        if ("SMS".equalsIgnoreCase(channel) && body.length() > 300) {
            local.add("SMS 채널 대비 본문이 너무 길어 가독성이 떨어짐");
        }

        if (!local.isEmpty()) {
            return CompletableFuture.completedFuture(Map.of(
                    MessageState.VALIDATION, "fail",
                    MessageState.DRAFT_MESSAGE_FAILURE_REASONS, local
            ));
        }

        // ===== 2) LLM 기반 품질/적합성 검증 =====
        BeanOutputConverter<DraftMessageValidationResponse> converter =
                new BeanOutputConverter<>(DraftMessageValidationResponse.class);

        String prompt = """
                너는 아모레퍼시픽의 CRM 메시지 검수자야.
                아래 '초안 메시지'가 목적/채널/상품/페르소나 관점에서 적합한지 검증해.
                (여기서는 '브랜드 톤'은 아직 적용 전이니 톤 평가는 하지 마.)
                
                [검증 항목]
                1) 목적(purpose)에 맞는 메시지인가? (예: promo면 혜택/유도 명확, retention이면 관계/리마인드 중심)
                2) 채널(channel)에 적합한 길이/구성인가? (SMS면 간결, KAKAO면 가독성/줄바꿈 고려)
                3) 상품/상품정보와 내용이 일치하는가? (허위/과장/근거 없는 단정 금지)
                4) 페르소나에 맞는 후킹/설득 요소가 있는가?
                5) CTA(다음 행동 유도)가 최소 1개는 있는가? (예: '지금 확인', '구매하러 가기', '자세히 보기' 등)
                
                [판정 기준]
                - 위 항목 중 치명적 문제가 1개라도 있으면 valid=false
                - violations는 최대 5개, 가장 중요한 것부터
                - 각 violations는 '무엇이 문제인지'가 분명한 한 문장
                
                [입력]
                %s
                
                %s
                
                purpose: %s
                channel: %s
                
                [초안 메시지]
                title: %s
                body: %s
                
                {format}
                """.formatted(
                persona.toString(), item.toString(),
                purpose, channel,
                title, body
        );

        try {
            DraftMessageValidationResponse resp = chatClient.prompt()
                    .user(u -> u.text(prompt).param("format", converter.getFormat()))
                    .call()
                    .entity(converter);

            boolean valid = resp != null && resp.valid();
            List<String> violations = (resp == null || resp.violations() == null)
                    ? List.of()
                    : normalize(resp.violations(), 5);

            if (valid) {
                return CompletableFuture.completedFuture(Map.of(
                        MessageState.VALIDATION, "pass"
                ));
            }

            if (violations.isEmpty()) {
                violations = List.of("초안 메시지가 목적/채널/상품 적합성 기준을 충족하지 못함 (수정 필요)");
            }

            return CompletableFuture.completedFuture(Map.of(
                    MessageState.VALIDATION, "fail",
                    MessageState.DRAFT_MESSAGE_FAILURE_REASONS, violations
            ));
        } catch (Exception e) {
            return CompletableFuture.completedFuture(Map.of(
                    MessageState.VALIDATION, "fail",
                    MessageState.DRAFT_MESSAGE_FAILURE_REASONS,
                    List.of("초안 검증 중 시스템 오류가 발생하여 재생성이 필요함: " + safeMsg(e))
            ));
        }
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }

    private static String safeMsg(Exception e) {
        String m = e.getMessage();
        if (m == null || m.isBlank()) return e.getClass().getSimpleName();
        return m.length() > 200 ? m.substring(0, 200) + "..." : m;
    }

    private static List<String> normalize(List<String> raw, int max) {
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
