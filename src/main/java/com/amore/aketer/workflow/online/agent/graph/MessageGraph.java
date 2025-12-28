package com.amore.aketer.workflow.online.agent.graph;

import com.amore.aketer.workflow.online.agent.node.*;
import com.amore.aketer.workflow.online.agent.state.MessageState;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.action.AsyncEdgeAction;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.bsc.langgraph4j.GraphDefinition.END;
import static org.bsc.langgraph4j.GraphDefinition.START;

@Component
@Getter
@RequiredArgsConstructor
public class MessageGraph {

    private CompiledGraph<MessageState> graph;

    private final DetermineDeliveryStrategyNode determineDeliveryStrategyNode;
    private final ValidateDeliveryStrategyNode validateDeliveryStrategyNode;
    private final DraftMarketingMessageNode draftMarketingMessageNode;
    private final ApplyBrandToneNode applyBrandToneNode;
    private final ValidateMessageAndToneNode validateMessageAndToneNode;
    private final ValidateEthicsPolicyNode validateEthicsPolicyNode;
    private final RegenerationNode regenerationNode;
    private final GenerateEthicsPolicyKeywordNode generateEthicsPolicyKeywordNode;
    private final RetrieveEthicsPolicyNode retrieveEthicsPolicyNode;

    @PostConstruct
    private void init() throws GraphStateException {
        graph = new StateGraph<>(MessageState.SCHEMA, MessageState::new)
                /*
                 * Node 설정
                 */
                // 최적 발송 채널 선정 노드
                .addNode("determine_delivery_strategy", determineDeliveryStrategyNode)

                // 발송 채널 적합성 검증 노드
                .addNode("validate_delivery_strategy", validateDeliveryStrategyNode)

                // 마케팅 메시지 생성 노드
                .addNode("draft_marketing_message", draftMarketingMessageNode)

                // 브랜드 톤 적용 노드
                .addNode("apply_brand_tone", applyBrandToneNode)

                // 메시지, 브랜드 톤 적합성 검증 노드
                .addNode("validate_message_and_tone", validateMessageAndToneNode)

                // 윤리 강령 검색 키워드 추천 노드
                .addNode("generate_ethics_policy_keyword", generateEthicsPolicyKeywordNode)

                // 윤리 강령 검색 노드
                .addNode("retrieve_ethics_policy", retrieveEthicsPolicyNode)

                // 메시지 윤리 강령 위반 검증 노드
                .addNode("validate_ethics_policy", validateEthicsPolicyNode)

                // 윤리 강령 위반 시 메시지 수정 노드
                .addNode("regeneration", regenerationNode)

                /*
                 * Edge 설정
                 */
                // START -> 최적 발송 채널 선정 노드
                .addEdge(START, "determine_delivery_strategy")

                // 최적 발송 채널 선정 노드 -> 발송 채널 적합성 검증 노드
                .addEdge("determine_delivery_strategy", "validate_delivery_strategy")

                // 발송 채널 적합성 검증 노드 (검증 결과: 실패) -> 최적 발송 채널 선정 노드
                //                      (검증 결과: 성공) -> 마케팅 메시지 생성 노드
                .addConditionalEdges("validate_delivery_strategy",
                        strategyRoute(),
                        Map.of("retry", "determine_delivery_strategy",
                                "next", "draft_marketing_message"))

                // 마케팅 메시지 생성 노드 -> 브랜드 톤 적용 노드
                .addEdge("draft_marketing_message", "apply_brand_tone")

                // 브랜드 톤 적용 노드 -> 메시지, 브랜드 톤 적합성 검증 노드
                .addEdge("apply_brand_tone", "validate_message_and_tone")

                // 메시지, 브랜드 톤 적합성 검증 노드 (검증 결과: 실패) -> 마케팅 메시지 생성 노드
                //                            (검증 결과: 성공) -> 윤리 강령 검색 키워드 추천 노드
                .addConditionalEdges("validate_message_and_tone",
                        messageRoute(),
                        Map.of("retry", "draft_marketing_message",
                                "next", "generate_ethics_policy_keyword"))

                // 윤리 강령 검색 키워드 추천 노드 -> 윤리 강령 검색 노드
                .addEdge("generate_ethics_policy_keyword", "retrieve_ethics_policy")

                // 윤리 강령 검색 노드 -> 메시지 윤리 강령 위반 검증 노드
                .addEdge("retrieve_ethics_policy", "validate_ethics_policy")

                // 메시지 윤리 강령 위반 검증 노드 (검증 결과: 실패) -> 메시지 수정 노드
                //                          (검증 결과: 성공) -> END
                .addConditionalEdges("validate_ethics_policy",
                        ethicsRoute(),
                        Map.of("retry", "regeneration",
                                "next", END))

                // 메시지 수정 노드 -> 윤리 강령 검색 키워드 추천 노드
                .addEdge("regeneration", "generate_ethics_policy_keyword")
                .compile();
    }

    // 발송 채널 적합성 검증 노드 분기 조건
    private AsyncEdgeAction<MessageState> strategyRoute() {
        return state -> {
            String result = state.hasAnyFailures() ? "retry" : "next";

            return CompletableFuture.completedFuture(result);
        };
    }

    // 메시지, 브랜드 톤 적합성 검증 노드 분기 조건
    private AsyncEdgeAction<MessageState> messageRoute() {
        return state -> {
            String result = state.hasAnyFailures() ? "retry" : "next";

            return CompletableFuture.completedFuture(result);
        };
    }

    // 메시지 윤리 강령 위반 검증 노드 분기 조건
    private AsyncEdgeAction<MessageState> ethicsRoute() {
        return state -> {
            String result = state.hasAnyFailures() ? "retry" : "next";

            return CompletableFuture.completedFuture(result);
        };
    }

    public CompletableFuture<MessageState> execute(Map<String, Object> inputs) {
        return graph.invoke(inputs)
                .map(CompletableFuture::completedFuture)
                .orElseGet(() -> CompletableFuture.failedFuture(new GraphStateException("Graph execution returned empty result")));
    }
}
