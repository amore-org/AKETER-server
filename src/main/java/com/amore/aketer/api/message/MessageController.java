package com.amore.aketer.api.message;

import com.amore.aketer.api.message.dto.CreateDraftRequest;
import com.amore.aketer.workflow.online.MessageGenerateWorkflow;
import com.amore.aketer.workflow.online.dto.DraftResult;
import com.amore.aketer.workflow.online.dto.GenerateDraftCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageGenerateWorkflow generateWorkflow;

    @PostMapping("/drafts")
    public DraftResult createDraft(@RequestBody CreateDraftRequest req) {
        return generateWorkflow.generate(
                GenerateDraftCommand.builder()
                        .srchDt(req.getSrchDt())
                        .personaId(req.getPersonaId())
                        .brand(req.getBrand())
                        .purpose(req.getPurpose())
                        .channel(req.getChannel())
                        .build()
        );
    }

    @PostMapping("/drafts/{draftId}/regenerate")
    public DraftResult regenerate(@PathVariable Long draftId) {
        return generateWorkflow.regenerate(draftId);
    }
}
