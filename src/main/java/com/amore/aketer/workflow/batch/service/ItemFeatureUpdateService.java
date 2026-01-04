package com.amore.aketer.workflow.batch.service;

import com.amore.aketer.workflow.batch.OfflineBatchUsecase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ItemFeatureUpdateService implements OfflineBatchUsecase {
    private static final Logger log = LoggerFactory.getLogger(ItemFeatureUpdateService.class);

    @Override
    public void run(String srchDt) {
        // TODO : 상품 데이터 delta -> feature 정규화/임베딩 -> 저장
        log.info("[STUB] ItemFeatureUpdateService.run srchDt={}", srchDt);
    }
}
