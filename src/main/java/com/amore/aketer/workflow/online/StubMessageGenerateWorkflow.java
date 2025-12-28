package com.amore.aketer.workflow.online;

import com.amore.aketer.workflow.online.dto.DraftResult;
import com.amore.aketer.workflow.online.dto.GenerateDraftCommand;
import org.springframework.stereotype.Service;


@Service // TODO 예시용으로, 구현체 생길 시 현재 클래스 지우기
public class StubMessageGenerateWorkflow implements MessageGenerateWorkflow {

    @Override
    public DraftResult generate(GenerateDraftCommand cmd) {
        return null;
    }

    @Override
    public DraftResult regenerate(Long draftId) {
        return null;
    }
}
