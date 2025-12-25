package com.amore.aketer.workflow.online.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerateDraftCommand {
    private String srchDt;     // yyyymmdd
    private Long personaId;    // persona PK
    private String brand;      // 브랜드 코드/명
    private String purpose;    // 목적(ex: promo/retention)
    private String channel;    // 채널(ex: SMS/PUSH/EMAIL)
}
