package com.amore.aketer.workflow.batch.scheduler;

import com.amore.aketer.workflow.batch.OfflineMasterJobRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AketerOfflineScheduler {

    private final OfflineMasterJobRunner runner;

    public AketerOfflineScheduler(OfflineMasterJobRunner runner) {
        this.runner = runner;
    }

//    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul") // 매일 00:00:00
//    public void runAtMidnight() {
//        runner.runNow();
//    }

    // 테스트용
    @Scheduled(initialDelay = 5000, fixedDelay = 60_000)
    public void runEveryMinute() {
        runner.runNow();
    }
}
