package com.amore.aketer.workflow.batch;

import org.springframework.batch.core.JobParameters;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public final class BatchParams {
    private BatchParams() {}
    private static final DateTimeFormatter YMD = DateTimeFormatter.ofPattern("yyyyMMdd");

    public static String srchDt(JobParameters params) {
        String v = params.getString("srchDt");
        if (v != null && v.matches("\\d{8}")) return v;
        return LocalDate.now().format(YMD);
    }
}
