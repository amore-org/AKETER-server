package com.amore.aketer.workflow.online.agent.node;

import com.amore.aketer.domain.enums.ChannelType;
import com.amore.aketer.workflow.online.agent.state.ItemState;
import com.amore.aketer.workflow.online.agent.state.MessageState;
import com.amore.aketer.workflow.online.agent.state.PersonaState;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class DraftMarketingMessageNode implements AsyncNodeAction<MessageState> {

    private final ChatClient chatClient;

    /**
     * LLM으로부터 응답받은 마케팅 메시지 초안을 구조화하기 위한 레코드입니다.
     *
     * @param title     생성된 메시지 제목
     * @param body      생성된 메시지 본문
     * @param rationale 메시지 작성 의도 및 전략 설명
     */
    public record DraftMessageResponse(
            @JsonPropertyDescription("마케팅 메시지의 제목 (고객의 이목을 끌 수 있는 문구)")
            String title,
            @JsonPropertyDescription("마케팅 메시지의 본문 (상품의 장점과 혜택을 강조)")
            String body,
            @JsonPropertyDescription("해당 메시지와 제목을 작성하게 된 의도 및 전략")
            String rationale
    ) {}

    /**
     * 결정된 발송 전략(채널, 시간)과 페르소나, 상품 정보를 바탕으로 마케팅 메시지 초안을 생성합니다.
     * <p>
     * 입력된 정보를 종합하여 고객에게 매력적으로 다가갈 수 있는 제목과 본문을 작성하고,
     * 그 결과를 {@link MessageState}에 업데이트합니다.
     * </p>
     *
     * @param state 현재 워크플로우의 상태 (페르소나, 상품 정보, 채널, 발송 시간 등 포함)
     * @return 생성된 메시지 제목({@link MessageState#MESSAGE_TITLE})과 본문({@link MessageState#MESSAGE_BODY})을 포함한 맵을 담은 CompletableFuture
     */
    @Override
    public CompletableFuture<Map<String, Object>> apply(MessageState state) {
        //==LLM에 필요한 데이터 준비==//
        PersonaState persona = state.getPersona();
        ItemState product = state.getItem();
        ChannelType channel = state.getChannel();
        String sendTime = state.getSendTime() != null ? state.getSendTime().toString() : "미정";
        List<String> failureReasons = state.getMessageFailureReasons();

        String personaInfo = (persona != null) ? persona.getProfileText() : "N/A";
        String productName = (product != null) ? product.getBrandName() : "N/A";
        String productDetails = (product != null) ? String.format("카테고리: %s, 특징: %s", product.getMajorCategory(), product.getKeyBenefits()) : "N/A";

        //==LLM 응답 구조화==/
        BeanOutputConverter<DraftMessageResponse> converter = new BeanOutputConverter<>(DraftMessageResponse.class);

        //==실패 사유가 있을 경우 프롬프트에 추가==//
        StringBuilder feedbackPrompt = new StringBuilder();
        if (!failureReasons.isEmpty()) {
            feedbackPrompt.append("\n[이전 시도 실패 사유 (반드시 반영하여 개선할 것)]\n");
            for (String reason : failureReasons) {
                feedbackPrompt.append("- ").append(reason).append("\n");
            }
            feedbackPrompt.append("위 실패 사유를 분석하여, 동일한 문제가 발생하지 않도록 메시지를 수정해.\n");
        }

        //==프롬프트==//
        String prompt = String.format("""
                        너는 아모레퍼시픽의 전문 카피라이터야.
                        주어진 '페르소나', '상품명', '상품 상세 정보', 그리고 '발송 채널'과 '발송 시간'을 고려하여,
                        고객의 마음을 사로잡을 수 있는 매력적인 **마케팅 메시지(제목/본문)**를 작성해.
                        
                        [입력 정보]
                        - 페르소나: %s
                        - 상품명: %s
                        - 상품 상세 정보: %s
                        - 발송 채널: %s
                        - 발송 시간: %s
                        
                        %s
                        
                        [작성 가이드]
                        1. **채널 특성 반영:**
                           - **KAKAO:** 친근하고 자연스러운 어조, 이모지 적절히 사용, 가독성 높은 줄바꿈.
                           - **SMS:** 짧고 간결한 문구, 핵심 혜택 위주, 글자 수 제한 고려(하지만 여기서는 내용은 충실히 작성).
                        2. **페르소나 맞춤:** 해당 페르소나의 관심사, 어투, 니즈를 반영하여 공감대를 형성해.
                        3. **상품 매력 발산:** 상품의 USP(Unique Selling Point)와 혜택을 자연스럽게 녹여내어 구매 욕구를 자극해.
                        4. **발송 시간 고려:** 발송되는 시간대(아침, 점심, 저녁 등)에 어울리는 인사말이나 문구를 포함하면 좋아.
                        
                        [출력 요구사항]
                        - title: 메시지의 핵심을 관통하는 훅(Hook)이 있는 제목.
                        - body: 구체적인 혜택과 행동 유도(Call to Action)가 포함된 본문.
                        - rationale: 왜 이렇게 작성했는지에 대한 논리적 근거 (한국어로 작성).
                        
                        {format}
                        """, personaInfo, productName, productDetails, channel, sendTime, feedbackPrompt);

        //==LLM 사용==//
        DraftMessageResponse response = chatClient.prompt()
                .user(u -> u.text(prompt).param("format", converter.getFormat()))
                .call()
                .entity(converter);

        // TODO: 상품 추천 이유를 recommend reason으로 recommend에 저장

        return CompletableFuture.completedFuture(Map.of(
                MessageState.MESSAGE_TITLE, response.title(),
                MessageState.MESSAGE_BODY, response.body()
        ));
    }
}
