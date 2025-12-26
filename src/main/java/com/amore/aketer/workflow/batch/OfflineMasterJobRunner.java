package com.amore.aketer.workflow.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class OfflineMasterJobRunner {
    private static final Logger log = LoggerFactory.getLogger(OfflineMasterJobRunner.class);
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter YMD = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final JobLauncher jobLauncher;
    private final Job offlineMasterJob;

    private final AtomicBoolean running = new AtomicBoolean(false);

    public OfflineMasterJobRunner(
            JobLauncher jobLauncher,
            @Qualifier("offlineMasterJob") Job offlineMasterJob
    ) {
        this.jobLauncher = jobLauncher;
        this.offlineMasterJob = offlineMasterJob;
    }

    public void runNow() {
        if (!running.compareAndSet(false, true)) {
            log.warn("OfflineMasterJob is already running. Skip this trigger.");
            return;
        }

        try {
            String srchDt = LocalDate.now(KST).format(YMD);

            JobParameters params = new JobParametersBuilder()
                    .addString("srchDt", srchDt)
                    // 같은 srchDt로 재실행해도 JobInstance 충돌 안 나게 유니크 파라미터 추가
                    .addLong("triggerTime", System.currentTimeMillis())
                    .toJobParameters();

            log.info("Launching OfflineMasterJob srchDt={}", srchDt);
            JobExecution exec = jobLauncher.run(offlineMasterJob, params);
            log.info("OfflineMasterJob executionId={} status={}", exec.getId(), exec.getStatus());
        } catch (Exception e) {
            log.error("OfflineMasterJob failed", e);
        } finally {
            running.set(false);
        }
    }
}
