package com.amore.aketer.workflow.online.agent.node;

import com.amore.aketer.workflow.online.agent.state.MessageState;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
public class ValidateEthicsPolicyNode implements AsyncNodeAction<MessageState> {

    @Override
    public CompletableFuture<Map<String, Object>> apply(MessageState state) {
        return null;
    }
}
