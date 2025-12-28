package com.amore.aketer.service;

import com.amore.aketer.domain.message.MessageReservation;
import com.amore.aketer.domain.message.MessageReservationRepository;
import com.amore.aketer.domain.enums.ChannelType;
import com.amore.aketer.external.channel.MessageChannelSender;
import com.amore.aketer.messaging.dto.MessagePayload;
import com.amore.aketer.messaging.dto.MessageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageSendService {

    private final MessageReservationRepository reservationRepository;
    private final Map<ChannelType, MessageChannelSender> channelSenders;
    private final MessageReportService reportService;

    public MessageResult sendMessage(MessagePayload payload) {
        log.info("메시지 전송: reservationId={}, channelType={}, retryCount={}", payload.getReservationId(), payload.getChannelType(), payload.getRetryCount());

        try {
            MessageChannelSender sender = channelSenders.get(payload.getChannelType());
            if (sender == null) {
                throw new IllegalStateException("채널 sender를 찾을 수 없습니다: " + payload.getChannelType());
            }

            MessageResult result = sender.send(payload);

            updateReservationStatus(payload.getReservationId(), result);

            reportService.recordSendResult(payload, result);

            return result;

        } catch (Exception e) {
            log.error("Error sending message: reservationId={}", payload.getReservationId(), e);
            MessageResult failResult = MessageResult.builder()
                    .success(false)
                    .retryable(true)
                    .errorMessage(e.getMessage())
                    .build();

            updateReservationStatus(payload.getReservationId(), failResult);
            reportService.recordSendResult(payload, failResult);

            return failResult;
        }
    }

    @Transactional
    protected void updateReservationStatus(Long reservationId, MessageResult result) {
        MessageReservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalStateException("해당 메시지 예약이 없습니다: " + reservationId));

        if (result.isSuccess()) {
            reservation.complete();
        } else {
            reservation.fail(result.isRetryable());
        }

        reservationRepository.save(reservation);
    }
}
