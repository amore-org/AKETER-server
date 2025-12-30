package com.amore.aketer.messaging.dto;

import com.amore.aketer.domain.enums.ChannelType;
import com.amore.aketer.domain.message.MessageReservation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessagePayload implements Serializable {

    private Long reservationId;
    private Long personaId;
    private ChannelType channelType;
    private int retryCount;
    private Long version;
    private String traceId;

    public static MessagePayload of(MessageReservation messageReservation, String traceId) {
        return MessagePayload.builder()
                .reservationId(messageReservation.getId())
                .personaId(messageReservation.getPersona().getId())
                .channelType(messageReservation.getChannelType())
                .retryCount(messageReservation.getRetryCount())
                .traceId(traceId)
                .build();
    }

    public static MessagePayload from(MessagePayload original, int newRetryCount) {
        return MessagePayload.builder()
                .reservationId(original.reservationId)
                .personaId(original.personaId)
                .channelType(original.channelType)
                .retryCount(newRetryCount)
                .version(original.version)
                .traceId(original.traceId)
                .build();
    }
}
