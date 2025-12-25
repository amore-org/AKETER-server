package com.amore.aketer.workflow.online.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DraftResult {
    private String title; // <= 40
    private String body;  // <= 350
    private String traceId; // 생성 흐름 추적용
}
