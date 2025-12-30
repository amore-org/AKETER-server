package com.amore.aketer.service;

import com.amore.aketer.api.message.dto.*;
import com.amore.aketer.domain.association.PersonaItem;
import com.amore.aketer.domain.association.PersonaItemRepository;
import com.amore.aketer.domain.item.Item;
import com.amore.aketer.domain.message.MessageReservation;
import com.amore.aketer.domain.message.MessageReservationRepository;
import com.amore.aketer.domain.enums.MessageStatus;
import com.amore.aketer.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageReservationService {

    private final MessageReservationRepository reservationRepository;
    private final PersonaItemRepository personaItemRepository;
    private final UserRepository userRepository;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

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

    @Transactional(readOnly = true)
    public ReservationDetailResponse getReservationDetail(Long id) {
        MessageReservation r = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 예약이 없습니다: " + id));

        var persona = r.getPersona();
        var msg = persona.getMessage();

        Integer targetCount = persona.getMemberCount();
        if (targetCount == null) {
            targetCount = (int) userRepository.countByPersona_Id(persona.getId());
        }

        var topItemOpt = personaItemRepository.findFirstByPersona_IdOrderByRankAsc(persona.getId());

        ReservationDetailResponse.ItemDto itemDto = topItemOpt
                .map(pi -> new ReservationDetailResponse.ItemDto(
                        pi.getItem().getId(),
                        pi.getItem().getItemKey(),
                        pi.getItem().getName()
                ))
                .orElse(null);

        ReservationDetailResponse.MessageDto messageDto =
                (msg == null) ? null : new ReservationDetailResponse.MessageDto(
                        msg.getId(),
                        msg.getTitle(),
                        msg.getBody()  // description 자리에 body 그대로
                );

        String reason = persona.getProfileText(); // 추천이유 임시 대체

        return new ReservationDetailResponse(
                r.getId(),
                persona.getId(),
                persona.getName(),
                r.getScheduledAt(),
                r.getChannelType().name(),
                r.getStatus().name(),
                targetCount,
                itemDto,
                messageDto,
                reason
        );
    }

    public Page<TodayReservationRowResponse> listTodayReservations( // 오늘 발송 예약 Page
            LocalDate date,
            MessageStatus status,
            String productSearch,
            Pageable pageable
    ) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now(KST);

        LocalDateTime start = targetDate.atStartOfDay();
        LocalDateTime end = targetDate.plusDays(1).atStartOfDay();

        Page<MessageReservation> page = reservationRepository.findTodayReservations(
                start, end, status, normalize(productSearch), pageable
        );

        List<Long> personaIds = page.getContent().stream()
                .map(r -> r.getPersona().getId())
                .distinct()
                .toList();

        Map<Long, PersonaItem> topItemByPersona = new HashMap<>();
        if (!personaIds.isEmpty()) {
            List<PersonaItem> all =
                    personaItemRepository.findByPersona_IdInOrderByPersona_IdAscRankAsc(personaIds);

            for (PersonaItem pi : all) {
                topItemByPersona.putIfAbsent(pi.getPersona().getId(), pi);
            }
        }

        return page.map(r -> {
            var persona = r.getPersona();
            var msg = persona.getMessage();

            Integer targetCount = persona.getMemberCount();
            if (targetCount == null) {
                targetCount = (int) userRepository.countByPersona_Id(persona.getId()); // (권장) 나중에 bulk count로 바꾸기
            }

            PersonaItem top = topItemByPersona.get(persona.getId());
            Item item = (top != null) ? top.getItem() : null;

            String desc = (msg != null) ? msg.getBody() : null;

            return new TodayReservationRowResponse(
                    r.getId(),
                    persona.getId(),
                    persona.getName(),
                    r.getScheduledAt(),
                    r.getChannelType().name(),  // 채널은 "응답 컬럼"이라 그대로 내려줌
                    r.getStatus().name(),
                    targetCount,
                    (item != null) ? item.getId() : null,
                    (item != null) ? item.getItemKey() : null,
                    (item != null) ? item.getName() : null,
                    (msg != null) ? msg.getId() : null,
                    (msg != null) ? msg.getTitle() : null,
                    desc
            );
        });
    }

    private static String normalize(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static String summarize(String body, int maxLen) {
        if (body == null) return null;
        String s = body.replaceAll("\\s+", " ").trim();
        return (s.length() <= maxLen) ? s : s.substring(0, maxLen) + "…";
    }
}
