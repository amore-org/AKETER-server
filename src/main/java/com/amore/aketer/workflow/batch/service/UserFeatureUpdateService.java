package com.amore.aketer.workflow.batch.service;

import com.amore.aketer.workflow.batch.OfflineBatchUsecase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class UserFeatureUpdateService implements OfflineBatchUsecase {
    private static final Logger log = LoggerFactory.getLogger(UserFeatureUpdateService.class);

    @Override
    public void run(String srchDt) {
        // TODO : 유저 리뷰/구매 delta 읽기 -> feature 정규화/임베딩 -> 저장
        log.info("[STUB] UserFeatureUpdateService.run srchDt={}", srchDt);
    }
}
