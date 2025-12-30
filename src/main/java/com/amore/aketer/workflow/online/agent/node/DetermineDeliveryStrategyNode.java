package com.amore.aketer.workflow.online.agent.node;

import com.amore.aketer.domain.enums.ChannelType;
import com.amore.aketer.workflow.online.agent.state.MessageState;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class DetermineDeliveryStrategyNode implements AsyncNodeAction<MessageState> {

    private final ChatClient chatClient;
    private static final ZoneId KST_ZONE = ZoneId.of("Asia/Seoul");

    /**
     * LLM으로부터 응답받은 발송 전략 결과를 구조화하기 위한 레코드입니다.
     *
     * @param channel  추천된 발송 채널 ({@link ChannelType#KAKAO} 또는 {@link ChannelType#SMS})
     * @param sendTime ISO 8601 형식으로 제안된 발송 시간 문자열
     * @param reason   선택된 채널과 시간이 효과적인 이유에 대한 설명
     */
    public record DeliveryStrategyResponse(
            ChannelType channel,
            @JsonPropertyDescription("ISO 8601 형식의 발송 시간 (예: 2024-01-01T10:00:00+09:00)")
            String sendTime,
            String reason
    ) {}

    /**
     * 메시지 발송 전략(채널 및 발송 시간)을 결정하는 비즈니스 로직을 수행합니다.
     * <p>
     * 입력된 페르소나, 상품명, 상품 상세 정보를 바탕으로 LLM을 호출하여
     * 최적의 발송 채널과 발송 시간을 추론하고, 그 결과를 {@link MessageState}에 업데이트합니다.
     * </p>
     *
     * @param state 현재 워크플로우의 상태 (페르소나, 상품 정보 등을 포함)
     * @return 결정된 채널({@link MessageState#CHANNEL})과 발송 시간({@link MessageState#SEND_TIME})을 포함한 맵을 담은 CompletableFuture
     */
    @Override
    public CompletableFuture<Map<String, Object>> apply(MessageState state) {
        //==LLM에 필요한 데이터 준비==//
        String persona = state.getPersona();
        String product = state.getProduct();
        String productInfo = state.getProductInformation();

        String now = LocalDateTime.now(KST_ZONE).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        //==LLM 응답 구조화==/
        BeanOutputConverter<DeliveryStrategyResponse> converter = new BeanOutputConverter<>(DeliveryStrategyResponse.class);

        //==프롬프트==//
        String prompt = String.format("""
                        너는 아모레퍼시픽의 숙련된 CRM 마케터야.
                        주어진 '페르소나', '상품명', 그리고 '상품 상세 정보'를 분석하여, **오픈율(Open Rate)과 구매 전환율(Conversion Rate)이 가장 높을 것으로 예상되는** 메시지 발송 전략을 수립해.
                        
                        [가능한 채널]
                        - KAKAO
                        - SMS
                        (위 두 가지 중에서만 선택해. 다른 채널은 불가능해.)
                        
                        [법적 제약 사항 (필수 준수)]
                        - 대상 국가: 대한민국
                        - **발송 가능 시간: 08:00 ~ 21:00** (정보통신망법 준수)
                        - 위 시간대 이외의 시간(21:00 이후 ~ 익일 08:00 이전)은 절대 추천하지 마.
                        
                        [전략 수립 가이드]
                        1. 페르소나 정보가 부족하다면, 해당 연령대나 직업군의 일반적인 라이프스타일을 추론하여 적용해.
                        2. 상품명뿐만 아니라 **상세 정보(가격대, 카테고리, 특징 등)**를 심층적으로 분석하여 상품의 관여도(저관여/고관여)와 구매 결정 요인을 파악해.
                        3. 발송 시간은 **현재 시간(%s) 이후**여야 해. 만약 현재 시간이 발송 가능 시간이 아니라면, 가장 가까운 발송 가능 시간을 찾아.
                        
                        [출력 요구사항]
                        - sendTime은 반드시 **ISO 8601 형식**을 지켜줘. (예: 2025-12-30T14:00:00+09:00)
                        - reason에는 선택한 채널과 시간이 왜 구매 전환에 효과적인지 논리적으로 설명해. 페르소나와 상품 정보 간의 연관성을 반드시 포함해. (반드시 한국어로 작성할 것)
                        
                        페르소나: %s
                        상품명: %s
                        상품 상세 정보: %s
                        
                        {format}
                        """, now, persona, product, productInfo);

        //==LLM 사용==//
        DeliveryStrategyResponse response = chatClient.prompt()
                .user(u -> u.text(prompt).param("format", converter.getFormat()))
                .call()
                .entity(converter);

        // TODO: 발송 채널, 시간 추천 이유 DB 삽입 로직
        // String reason = response.reason();

        return CompletableFuture.completedFuture(Map.of(
                MessageState.CHANNEL, response.channel().name(),
                MessageState.SEND_TIME, parseTime(response.sendTime())
        ));
    }

    /**
     * ISO 8601 날짜/시간 문자열을 {@link LocalDateTime}으로 파싱합니다.
     * <p>
     * 오프셋 정보(예: +09:00, Z)가 포함된 경우 한국 시간(Asia/Seoul)으로 보정하여 변환하고,
     * 오프셋 정보가 없는 로컬 일시 형식의 경우 그대로 파싱을 시도합니다.
     * </p>
     *
     * @param time 파싱할 ISO 8601 형식의 날짜/시간 문자열 (예: "2024-01-01T10:00:00+09:00" 또는 "2024-01-01T10:00:00")
     * @return 한국 시간 기준으로 파싱된 {@link LocalDateTime} 객체
     * @throws java.time.format.DateTimeParseException 문자열이 유효한 ISO 8601 형식이 아닐 경우 발생
     */
    private LocalDateTime parseTime(String time) {
        try {
            // 오프셋이 있는 경우(예: +09:00, Z) 해당 시점의 '한국 시간'으로 변환 후 LocalDateTime 추출
            return ZonedDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME)
                    .withZoneSameInstant(KST_ZONE)
                    .toLocalDateTime();
        } catch (Exception e) {
            // 오프셋이 없는 경우 (예: 2025-12-30T14:00:00) 문자열 그대로 파싱
            return LocalDateTime.parse(time, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
    }
}
