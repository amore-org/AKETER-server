package com.amore.aketer.workflow.online;

import com.amore.aketer.workflow.online.agent.graph.MessageGraph;
import com.amore.aketer.workflow.online.dto.DraftResult;
import com.amore.aketer.workflow.online.dto.GenerateDraftCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service // TODO 예시용으로, 구현체 생길 시 현재 클래스 지우기
@RequiredArgsConstructor
public class StubMessageGenerateWorkflow implements MessageGenerateWorkflow {

    private final MessageGraph messageGraph;

    @Override
    public DraftResult generate(GenerateDraftCommand cmd) {
        messageGraph.execute(Map.of("하이루", "hihi"));
        return DraftResult.builder()
                .title("[" + cmd.getBrand() + "] 추천 메시지")
                .body("[STUB] personaId=" + cmd.getPersonaId() + ", purpose=" + cmd.getPurpose() + ", channel=" + cmd.getChannel())
                .traceId(UUID.randomUUID().toString())
                .build();
    }

    @Override
    public DraftResult regenerate(Long draftId) {
        return null;
    }
}
