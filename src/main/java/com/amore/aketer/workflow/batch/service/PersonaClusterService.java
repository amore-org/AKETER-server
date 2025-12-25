package com.amore.aketer.workflow.batch.service;

import com.amore.aketer.workflow.batch.OfflineBatchUsecase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PersonaClusterService implements OfflineBatchUsecase {
    private static final Logger log = LoggerFactory.getLogger(PersonaClusterService.class);

    @Override
    public void run(String srchDt) {
        // TODO : user_feature 기반 클러스터링 -> persona_profile 생성(LLM)
        log.info("[STUB] PersonaClusterService.run srchDt={}", srchDt);
    }
}
