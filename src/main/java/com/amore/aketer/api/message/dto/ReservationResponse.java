package com.amore.aketer.api.message.dto;

import com.amore.aketer.domain.enums.ChannelType;
import com.amore.aketer.domain.enums.MessageStatus;
import com.amore.aketer.domain.message.MessageReservation;
import com.amore.aketer.domain.persona.Persona;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationResponse {

    private Long id;
    private Persona persona;
    private ChannelType channelType;
    private MessageStatus status;
    private LocalDateTime scheduledAt;
    private int retryCount;
    private Instant createdAt;
    private Instant updatedAt;

    public static ReservationResponse from(MessageReservation reservation) {
        return ReservationResponse.builder()
                .id(reservation.getId())
                .persona(reservation.getPersona())
                .channelType(reservation.getChannelType())
                .status(reservation.getStatus())
                .scheduledAt(reservation.getScheduledAt())
                .retryCount(reservation.getRetryCount())
                .createdAt(reservation.getCreatedAt())
                .updatedAt(reservation.getUpdatedAt())
                .build();
    }
}
