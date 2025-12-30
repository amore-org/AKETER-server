package com.amore.aketer.workflow.online.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 윤리 강령 검증 결과
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationResult {

    private boolean valid;

    private List<String> violations;

    private String failureReason;  // 재생성 시 사용

    private int checkedGuidelinesCount;  // 추적용

    public static ValidationResult success(int checkedGuidelinesCount) {
        return ValidationResult.builder()
                .valid(true)
                .violations(new ArrayList<>())
                .checkedGuidelinesCount(checkedGuidelinesCount)
                .build();
    }

    public static ValidationResult failure(List<String> violations, int checkedGuidelinesCount) {
        String failureReason = String.join("\n", violations);

        return ValidationResult.builder()
                .valid(false)
                .violations(violations)
                .failureReason(failureReason)
                .checkedGuidelinesCount(checkedGuidelinesCount)
                .build();
    }
}