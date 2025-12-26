package com.amore.aketer.workflow.batch.service;

import com.amore.aketer.workflow.batch.OfflineBatchUsecase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PersonaItemMatchService implements OfflineBatchUsecase {
    private static final Logger log = LoggerFactory.getLogger(PersonaItemMatchService.class);

    @Override
    public void run(String srchDt) {
        // TODO : persona_profile ↔ item_feature 매칭 -> 추천이유 포함 저장
        log.info("[STUB] PersonaItemMatchService.run srchDt={}", srchDt);
    }
}