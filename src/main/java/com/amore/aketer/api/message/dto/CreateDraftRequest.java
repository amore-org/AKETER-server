package com.amore.aketer.api.message.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateDraftRequest {
    private String srchDt;
    private Long personaId;
    private String brand;
    private String purpose;
    private String channel;
}