package com.amore.aketer.workflow.online.agent.node;

import com.amore.aketer.domain.enums.ChannelType;
import com.amore.aketer.workflow.online.agent.state.ItemState;
import com.amore.aketer.workflow.online.agent.state.MessageState;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class ApplyBrandToneNode implements AsyncNodeAction<MessageState> {

    private final ChatClient chatClient;

    /**
     * LLM 응답을 구조화하기 위한 레코드
     * - title: 40자 이내
     * - body:  350자 이내
     */
    public record ToneAppliedMessageResponse(
            String title,
            String body,
            String toneSummary
    ) {}

    @Override
    public CompletableFuture<Map<String, Object>> apply(MessageState state) {
        // ===== 입력 읽기 =====
        String purpose = nvl(state.getPurpose());
        ChannelType channel = state.getChannel();
        ItemState item = state.getItem();
        String brand = state.getBrand(); // 브랜드명 추가

        // TODO: "/amore/brand/브랜드명.txt" 파일 읽어와서 가이드라인에 추가
        String brandGuidelines = nvl(state.getBrandGuidelines());

        String draftTitle = nvl(state.getMessageTitle());
        String draftBody = nvl(state.getMessageBody());

        // (권장) 브랜드 톤 검증 실패 피드백: 최근 것만 몇 개 사용
        List<String> toneFailureReasons = safeLast(state.getBrandToneFailureReasons(), 5);

        // ===== LLM 응답 구조화 =====
        BeanOutputConverter<ToneAppliedMessageResponse> converter =
                new BeanOutputConverter<>(ToneAppliedMessageResponse.class);

        // ===== 피드백 프롬프트 =====
        String feedbackSection = "";
        if (!toneFailureReasons.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("\n[이전 브랜드 톤 검증 실패 사유 (반드시 반영)]\n");
            for (String r : toneFailureReasons) sb.append("- ").append(r).append("\n");
            sb.append("위 사유를 해결하도록 문장/어조/표현을 수정해.\n");
            feedbackSection = sb.toString();
        }

        // ===== 브랜드 가이드라인 fallback =====
        // (RAG가 비어있을 수 있으니 최소 안전장치)
        String guidelineSection = brandGuidelines.isBlank()
                ? """
                   [브랜드 톤 가이드라인]
                   - (가이드라인이 제공되지 않았음) 과장/단정 표현을 피하고, 고객 친화적인 톤으로 자연스럽게 작성해.
                   - 지나친 느낌표/이모지 남발 금지. 문장 길이는 간결하게.
                   """
                : """
                   [브랜드 톤 가이드라인]
                   %s
                   """.formatted(brandGuidelines);

        // ===== 프롬프트 =====
        String prompt = """
                너는 아모레퍼시픽의 브랜드 카피라이터야.
                아래의 '초안 메시지'를 주어진 '브랜드 톤 가이드라인'과 '페르소나', '상품 정보'에 맞게 다듬어 최종 문구를 만들어.
                
                [목표]
                - 초안의 핵심 정보(상품/혜택/CTA)는 유지하되, 문체/어조/표현을 브랜드 톤에 맞게 정교화
                - 불필요한 과장, 단정, 허위/오해 소지가 있는 표현은 제거
                - 고객에게 친근하지만 신뢰감 있게
                - 채널 특성 반영(SMS면 더 간결하게, KAKAO면 가독성/줄바꿈 고려)
                
                [출력 형식]
                - 반드시 아래 JSON 스키마로만 출력해.
                - title은 40자 이내(공백 포함), body는 350자 이내(공백 포함).
                - 한국어로 작성.
                
                %s
                %s
                
                [메타 정보]
                - brand: %s
                - purpose: %s
                - channel: %s
                
                [상품 정보(참고)]
                %s
                
                [초안 메시지]
                - draftTitle: %s
                - draftBody: %s
                
                {format}
                """.formatted(
                guidelineSection,
                feedbackSection,
                brand, purpose, channel,
                item.toString(),
                draftTitle, draftBody
        );

        ToneAppliedMessageResponse response = chatClient.prompt()
                .user(u -> u.text(prompt).param("format", converter.getFormat()))
                .call()
                .entity(converter);

        // ===== 후처리(안전장치) =====
        // - 모델이 간혹 길이를 넘길 수 있으니 최소한으로 클램핑
        String finalTitle = clamp(nvl(response.title()).trim(), 40);
        String finalBody = clamp(nvl(response.body()).trim(), 350);

        return CompletableFuture.completedFuture(Map.of(
                MessageState.MESSAGE_TITLE, finalTitle,
                MessageState.MESSAGE_BODY, finalBody
        ));
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }

    private static String clamp(String s, int maxLen) {
        if (s == null) return "";
        if (s.length() <= maxLen) return s;
        // 끝에 말줄임표를 붙일 공간 확보
        if (maxLen <= 1) return s.substring(0, maxLen);
        return s.substring(0, maxLen - 1) + "…";
    }

    private static List<String> safeLast(List<String> list, int n) {
        if (list == null || list.isEmpty()) return List.of();
        int size = list.size();
        int from = Math.max(0, size - n);
        return new ArrayList<>(list.subList(from, size));
    }
}
