package com.amore.aketer.api.message.dto;

import com.amore.aketer.domain.message.MessageReservation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationListResponse {

    private List<ReservationResponse> reservations;
    private int total;

    public static ReservationListResponse from(List<MessageReservation> reservations) {
        List<ReservationResponse> responses = reservations.stream()
                .map(ReservationResponse::from)
                .collect(Collectors.toList());

        return ReservationListResponse.builder()
                .reservations(responses)
                .total(responses.size())
                .build();
    }
}
