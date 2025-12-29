package com.amore.aketer.service;

import com.amore.aketer.api.message.dto.CreateReservationRequest;
import com.amore.aketer.api.message.dto.ReservationListResponse;
import com.amore.aketer.api.message.dto.ReservationResponse;
import com.amore.aketer.api.message.dto.UpdateReservationRequest;
import com.amore.aketer.domain.message.MessageReservation;
import com.amore.aketer.domain.message.MessageReservationRepository;
import com.amore.aketer.domain.enums.MessageStatus;
import com.amore.aketer.domain.persona.Persona;
import com.amore.aketer.domain.persona.PersonaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageReservationService {

    private final MessageReservationRepository reservationRepository;
    private final PersonaRepository personaRepository;

    @Transactional
    public ReservationResponse createReservation(CreateReservationRequest request) {
        Persona persona = personaRepository.findById(request.getPersonaId())
                .orElseThrow(() -> new IllegalArgumentException("해당 페르소나가 없습니다: " + request.getPersonaId()));

        MessageReservation reservation = MessageReservation.builder()
                .persona(persona)
                .channelType(request.getChannelType())
                .scheduledAt(request.getScheduledAt())
                .build();

        MessageReservation savedReservation = reservationRepository.save(reservation);
        return ReservationResponse.from(savedReservation);
    }

    @Transactional(readOnly = true)
    public ReservationResponse getReservation(Long id) {
        MessageReservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 예약이 없습니다" + id));
        return ReservationResponse.from(reservation);
    }

    @Transactional(readOnly = true)
    public ReservationListResponse listReservations(String userId, MessageStatus status, LocalDateTime from, LocalDateTime to) {
        List<MessageReservation> reservations;

        if (status != null) {
            reservations = reservationRepository.findByStatusOrderByScheduledAtDesc(status);
        } else if (from != null && to != null) {
            reservations = reservationRepository.findByScheduledAtBetweenOrderByScheduledAtDesc(from, to);
        } else {
            reservations = reservationRepository.findAll();
        }

        List<ReservationResponse> responses = reservations.stream()
                .map(ReservationResponse::from)
                .collect(Collectors.toList());

        return ReservationListResponse.builder()
                .reservations(responses)
                .total(responses.size())
                .build();
    }

    @Transactional
    public ReservationResponse updateReservation(Long id, UpdateReservationRequest request) {
        MessageReservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 예약이 없습니다: " + id));

        if (reservation.getStatus() != MessageStatus.READY) {
            throw new IllegalStateException("대기 상태인 예약만 수정할 수 있습니다");
        }

        return ReservationResponse.from(reservation);
    }

    @Transactional
    public void cancelReservation(Long id) {
        MessageReservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 예약이 없습니다: " + id));

        reservation.cancel();
        reservationRepository.save(reservation);
    }

    @Transactional
    public void deleteReservation(Long id) {
        MessageReservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 예약이 없습니다: " + id));

        if (reservation.getStatus() != MessageStatus.CANCELED && reservation.getStatus() != MessageStatus.FAILED) {
            throw new IllegalStateException("취소되거나 실패한 예약만 삭제할 수 있습니다");
        }

        reservationRepository.delete(reservation);
    }
}
