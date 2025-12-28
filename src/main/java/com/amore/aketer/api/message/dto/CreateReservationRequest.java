package com.amore.aketer.api.message.dto;

import com.amore.aketer.domain.enums.ChannelType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateReservationRequest {

    @NotNull
    private Long personaId;

    @NotNull
    private ChannelType channelType;

    @NotNull
    @Future
    private LocalDateTime scheduledAt;
}
