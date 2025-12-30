package com.amore.aketer.api.message.dto;

import java.time.LocalDateTime;

public record TodayReservationRowResponse(
        Long id,
        Long personaId,
        String personaName,
        LocalDateTime scheduledAt,
        String channelType,
        String status,
        Integer targetCount,

        Long itemId,
        String itemKey,
        String itemName,

        Long messageId,
        String messageTitle,
        String messageDescription
) {}