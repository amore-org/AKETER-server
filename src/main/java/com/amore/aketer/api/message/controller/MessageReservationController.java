package com.amore.aketer.api.message.controller;

import com.amore.aketer.api.message.dto.*;
import com.amore.aketer.service.MessageReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

import static org.springframework.data.domain.Sort.Direction.ASC;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reservations")
public class MessageReservationController {

    private final MessageReservationService reservationService;

    /**
     * 메시지 발송 예약 목록 조회
     * @param scheduledAt null: 오늘(포함) 이후 전체 데이터 / 날짜 지정: 해당 날짜만 조회
     * @param pageable 페이징 정보
     * @return 예약 목록 (페이징)
     */
    @GetMapping
    public Page<ReservationByDateRowResponse> getReservations(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate scheduledAt,

            @PageableDefault(size = 10, sort = "scheduledAt", direction = ASC) Pageable pageable
    ) {
        return reservationService.getListReservationsByDate(scheduledAt, pageable);
    }

    @GetMapping("/{id}")  // 메시지 예약 단건 상세
    public ReservationDetailResponse getReservation(@PathVariable Long id) {
        return reservationService.getReservationDetail(id);
    }

    @PutMapping("/{id}")
    public ReservationResponse updateReservation(@PathVariable Long id, @Valid @RequestBody UpdateReservationRequest request) {
        return reservationService.updateReservation(id, request);
    }

    @PostMapping("/{id}/cancel")
    public void cancelReservation(@PathVariable Long id) {
        reservationService.cancelReservation(id);
    }

    @DeleteMapping("/{id}")
    public void deleteReservation(@PathVariable Long id) {
        reservationService.deleteReservation(id);
    }
}
