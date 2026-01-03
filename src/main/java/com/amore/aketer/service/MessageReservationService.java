package com.amore.aketer.service;

import com.amore.aketer.api.message.dto.*;
import com.amore.aketer.domain.association.PersonaItemRepository;
import com.amore.aketer.domain.enums.RecommendTargetType;
import com.amore.aketer.domain.message.MessageReservation;
import com.amore.aketer.domain.message.MessageReservationRepository;
import com.amore.aketer.domain.enums.MessageStatus;
import com.amore.aketer.domain.recommend.RecommendRepository;
import com.amore.aketer.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageReservationService {

    private final MessageReservationRepository reservationRepository;
    private final PersonaItemRepository personaItemRepository;
    private final UserRepository userRepository;
    private final RecommendRepository recommendRepository;

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
        var msg = r.getMessage();  // MessageReservation의 메시지 직접 사용
        var item = r.getItem();    // MessageReservation의 아이템 직접 사용

        Integer targetCount = persona.getMemberCount();
        if (targetCount == null) {
            targetCount = (int) userRepository.countByPersona_Id(persona.getId());
        }

        ReservationDetailResponse.ItemDto itemDto = null;
        if (item != null) {
            String brandName = (item.getDetail() != null) ? item.getDetail().getBrandName() : null;
            itemDto = new ReservationDetailResponse.ItemDto(
                    item.getId(),
                    item.getItemKey(),
                    item.getName(),
                    brandName
            );
        }

        ReservationDetailResponse.MessageDto messageDto = null;
        if (msg != null) {
            messageDto = new ReservationDetailResponse.MessageDto(
                    msg.getId(),
                    msg.getTitle(),
                    msg.getBody()
            );
        }

        // Recommend 테이블에서 추천 이유 조회
        String reason = recommendRepository
                .findByTargetTypeAndTargetId(RecommendTargetType.PERSONA, persona.getId())
                .map(recommend -> recommend.getRecommendReason())
                .orElse(null);

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

    public Page<ReservationByDateRowResponse> getListReservationsByDate(
            LocalDate date,
            Pageable pageable
    ) {
        LocalDateTime start;
        LocalDateTime end;

        if (date == null) {
            // null → 오늘(포함) 이후 모든 데이터
            start = LocalDate.now(KST).atStartOfDay();
            end = LocalDateTime.of(2099, 12, 31, 23, 59, 59);
        } else {
            // 날짜 지정 → 해당 날짜만
            start = date.atStartOfDay();
            end = date.plusDays(1).atStartOfDay();
        }

        Page<MessageReservation> page = reservationRepository.findTodayReservations(
                start, end, null, null, pageable
        );

        return page.map(r -> {
            var persona = r.getPersona();
            var msg = r.getMessage();  // MessageReservation의 개별 메시지 사용
            var item = r.getItem();    // MessageReservation의 개별 아이템 사용

            Integer targetCount = persona.getMemberCount();
            if (targetCount == null) {
                targetCount = (int) userRepository.countByPersona_Id(persona.getId()); // (권장) 나중에 bulk count로 바꾸기
            }

            String desc = (msg != null) ? msg.getBody() : null;

            return new ReservationByDateRowResponse(
                    persona.getId(),
                    persona.getName(),
                    r.getScheduledAt(),
                    r.getChannelType().name(),  // 채널은 "응답 컬럼"이라 그대로 내려줌
                    r.getStatus().name(),
                    targetCount,
                    (item != null) ? item.getId() : null,
                    (item != null) ? item.getItemKey() : null,
                    (item != null) ? item.getName() : null,
                    (item != null && item.getDetail() != null) ? item.getDetail().getBrandName() : null,
                    r.getId(),  // messageReservationId
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
