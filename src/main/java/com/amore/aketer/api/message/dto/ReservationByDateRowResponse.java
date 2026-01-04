package com.amore.aketer.api.message.dto;

import java.time.LocalDateTime;

public record ReservationByDateRowResponse(
        Long personaId,
        String personaName,
        LocalDateTime scheduledAt,
        String channelType,
        String status,
        Integer targetCount,

        Long itemId,
        String itemKey,
        String itemName,
        String brandName,

        Long messageReservationId,
        String messageTitle,
        String messageDescription
) {}