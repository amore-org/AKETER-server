package com.amore.aketer.api.message.dto;

import java.time.LocalDateTime;

public record ReservationDetailResponse(
        Long id,
        Long personaId,
        String personaName,
        LocalDateTime scheduledAt,
        String channelType,
        String status,
        Integer targetCount,
        ItemDto item,
        MessageDto message,
        String recommendReason
) {
    public record ItemDto(Long id, String itemKey, String name) {}
    public record MessageDto(Long id, String title, String description) {}
}
