package com.amore.aketer.workflow.online.agent.graph;

import com.amore.aketer.workflow.online.agent.node.*;
import com.amore.aketer.workflow.online.agent.state.MessageState;
import lombok.Getter;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.bsc.langgraph4j.GraphDefinition.END;
import static org.bsc.langgraph4j.GraphDefinition.START;

@Component
@Getter
public class MessageGraph {

    private final CompiledGraph<MessageState> graph;

    public MessageGraph(DetermineDeliveryStrategyNode determineDeliveryStrategyNode,
                        ValidateDeliveryStrategyNode validateDeliveryStrategyNode,
                        DraftMarketingMessageNode draftMarketingMessageNode,
                        ApplyBrandToneNode applyBrandToneNode,
                        ValidateMessageAndToneNode validateMessageAndToneNode,
                        ValidateEthicsPolicyNode validateEthicsPolicyNode,
                        RegenerationNode regenerationNode) throws GraphStateException {
        graph = new StateGraph<>(MessageState.SCHEMA, MessageState::new)
                .addNode("determine_delivery_strategy", determineDeliveryStrategyNode)
                .addNode("validate_delivery_strategy", validateDeliveryStrategyNode)
                .addNode("draft_marketing_message", draftMarketingMessageNode)
                .addNode("apply_brand_tone", applyBrandToneNode)
                .addNode("validate_message_and_tone", validateMessageAndToneNode)
                .addNode("validate_ethics_policy", validateEthicsPolicyNode)
                .addNode("regeneration", regenerationNode)

                .addEdge(START, "determine_delivery_strategy")
                .addEdge("determine_delivery_strategy", "validate_delivery_strategy")
                .addConditionalEdges("validate_delivery_strategy", state -> {
                            String result = state.hasAnyFailures() ? "retry" : "next";

                            return CompletableFuture.completedFuture(result);
                        },
                        Map.of("retry", "determine_delivery_strategy",
                                "next", "draft_marketing_message"))
                .addEdge("draft_marketing_message", "apply_brand_tone")
                .addEdge("apply_brand_tone", "validate_message_and_tone")
                .addConditionalEdges("validate_message_and_tone", state -> {
                            String result = state.hasAnyFailures() ? "retry" : "next";

                            return CompletableFuture.completedFuture(result);
                        },
                        Map.of("retry", "draft_marketing_message",
                                "next", "validate_ethics_policy"))
                .addConditionalEdges("validate_ethics_policy", state -> {
                            String result = state.hasAnyFailures() ? "retry" : "next";

                            return CompletableFuture.completedFuture(result);
                        },
                        Map.of("retry", "regeneration",
                                "next", END))
                .addEdge("regeneration", "validate_ethics_policy")
                .compile();
    }

    public CompletableFuture<MessageState> execute(Map<String, Object> inputs) {
        return graph.invoke(inputs)
                .map(CompletableFuture::completedFuture)
                .orElseGet(() -> CompletableFuture.failedFuture(new GraphStateException("Graph execution returned empty result")));
    }
}
