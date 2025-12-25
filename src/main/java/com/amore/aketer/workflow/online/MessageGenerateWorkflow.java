package com.amore.aketer.workflow.online;

import com.amore.aketer.workflow.online.dto.DraftResult;
import com.amore.aketer.workflow.online.dto.GenerateDraftCommand;

public interface MessageGenerateWorkflow {
    DraftResult generate(GenerateDraftCommand cmd);
    DraftResult regenerate(Long draftId);
}
