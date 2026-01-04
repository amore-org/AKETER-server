package com.amore.aketer.workflow.online.agent.node;

import com.amore.aketer.workflow.online.agent.state.MessageState;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 생성된 마케팅 메시지를 분석하여 윤리강령 검증에 필요한 키워드를 추출하는 노드
 */
@Component
@RequiredArgsConstructor
public class GenerateEthicsPolicyKeywordNode implements AsyncNodeAction<MessageState> {

    private final ChatClient chatClient;

    /**
     * LLM으로부터 응답받은 윤리강령 키워드 결과를 구조화하기 위한 레코드
     */
    public record EthicsPolicyKeywordResponse(
            @JsonPropertyDescription("윤리강령 문서 검색에 사용할 핵심 키워드 (예: '개인정보 보호', '과대광고 금지', '허위표시')")
            String keyword
    ) {}

    @Override
    public CompletableFuture<Map<String, Object>> apply(MessageState state) {
        String messageTitle = state.getMessageTitle();
        String messageBody = state.getMessageBody();

        // LLM 응답 구조화
        BeanOutputConverter<EthicsPolicyKeywordResponse> converter =
                new BeanOutputConverter<>(EthicsPolicyKeywordResponse.class);

        // 프롬프트
        String prompt = String.format("""
                        너는 아모레퍼시픽의 컴플라이언스 전문가야.
                        주어진 마케팅 메시지를 분석하여, **윤리강령 문서에서 검색할 때 사용할 핵심 키워드**를 추출해.

                        [메시지 정보]
                        - 제목: %s
                        - 본문: %s

                        [키워드 추출 가이드]
                        1. 메시지에서 윤리적 검증이 필요한 요소를 파악해 (예: 할인/혜택, 성분/효능, 개인정보, 표현 방식, 의학표현 등)
                        2. 해당 요소와 관련된 윤리강령 항목을 검색할 수 있는 명확한 키워드를 생성해
                        3. 키워드는 구체적이면서도 관련 문서를 폭넓게 찾을 수 있어야 해
                        4. 일반적인 윤리강령 주제어를 사용해 (예: '개인정보 보호', '과대광고', '허위표시', '할인 표시', '효능 표현')

                        [출력 요구사항]
                        - keyword에는 윤리강령 검색에 가장 적합한 키워드를 한국어로 작성해
                        - 여러 키워드가 필요한 경우 ,(반점) 으로 구분해 (예: "할인 표시 과대광고")

                        {format}
                        """, messageTitle, messageBody);

        // LLM 사용
        EthicsPolicyKeywordResponse response = chatClient.prompt()
                .user(u -> u.text(prompt).param("format", converter.getFormat()))
                .call()
                .entity(converter);

        return CompletableFuture.completedFuture(Map.of(
                MessageState.ETHICS_POLICY_KEYWORD, response.keyword()
        ));
    }
}
