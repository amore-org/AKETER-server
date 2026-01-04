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

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class ValidateDeliveryStrategyNode implements AsyncNodeAction<MessageState> {

    private final ChatClient chatClient;

    /**
     * LLM으로부터 응답받은 발송 전략 검증 결과를 구조화하기 위한 레코드입니다.
     *
     * @param validation    검증 결과 (pass: 적합, fail: 부적합)
     * @param failureReason 부적합할 경우(fail) 그 구체적인 이유
     */
    public record ValidationResponse(
            @JsonPropertyDescription("전략이 적합하면 'pass', 부적합하면 'fail'")
            String validation,
            @JsonPropertyDescription("부적합할 경우(fail) 그 구체적인 이유 (pass일 경우 null)")
            String failureReason
    ) {}

    /**
     * 이전 노드에서 수립된 메시지 발송 전략(채널, 시간)의 유효성을 검증합니다.
     * <p>
     * 페르소나, 상품 정보, 그리고 제안된 전략의 근거(Reason)를 종합적으로 분석하여
     * 해당 전략이 실제로 구매 전환에 효과적일지 판단합니다.
     * 검증 결과에 따라 상태(pass/fail)를 업데이트하고, 실패 시 사유를 기록합니다.
     * </p>
     *
     * @param state 현재 워크플로우의 상태 (페르소나, 상품 정보, 제안된 채널/시간/이유 포함)
     * @return 검증 결과({@link MessageState#VALIDATION})와 실패 사유({@link MessageState#DELIVERY_STRATEGY_FAILURE_REASONS})가 업데이트된 맵
     */
    @Override
    public CompletableFuture<Map<String, Object>> apply(MessageState state) {
        //==LLM에 필요한 데이터 준비==//
        PersonaState persona = state.getPersona();
        ItemState product = state.getItem();
        ChannelType channel = state.getChannel();
        String sendTime = state.getSendTime().toString();
        String strategyReason = state.getStrategyReason();

        //==LLM 응답 구조화==/
        BeanOutputConverter<ValidationResponse> converter = new BeanOutputConverter<>(ValidationResponse.class);

        //==프롬프트==//
        String prompt = String.format("""
                너는 아모레퍼시픽의 꼼꼼한 마케팅 전략 검수자야.
                앞서 수립된 '메시지 발송 전략'이 타겟 페르소나와 상품 특성에 비추어 정말 효과적인지 비판적으로 검증해.
                
                [검증 대상 정보]
                %s
                
                %s
                
                - 제안된 채널: %s
                - 제안된 발송 시간: %s
                - 전략 수립 근거: %s
                
                [검증 기준]
                1. **논리적 일관성:** 제안된 채널과 시간이 페르소나의 라이프스타일과 상품 특성에 부합하는가?
                2. **구매 전환 가능성:** 이 전략이 실제로 구매 전환에 효과적인지 판단해.
                3. **법적 준수 여부:** 발송 시간이 대한민국 정보통신망법상 허용된 시간(08:00 ~ 21:00) 내에 있는가?
                
                [출력 요구사항]
                - 전략이 타당하다면 validation을 'pass'로 설정해.
                - 전략에 문제가 있거나 개선이 필요하다면 validation을 'fail'로 설정하고, failureReason에 그 이유를 구체적으로 명시해. (반드시 한국어로 작성)
                
                {format}
                """, persona.toString(), product.toString(), channel, sendTime, strategyReason);

        //==LLM 사용==//
        ValidationResponse response = chatClient.prompt()
                .user(u -> u.text(prompt).param("format", converter.getFormat()))
                .call()
                .entity(converter);

        return CompletableFuture.completedFuture(Map.of(
                MessageState.VALIDATION, response.validation().toLowerCase(),
                MessageState.DELIVERY_STRATEGY_FAILURE_REASONS, Stream.ofNullable(response.failureReason()).toList()
        ));
    }
}
