package com.amore.aketer.domain.enums;

public enum MessageStatus {
    READY,      // 발송 대기
    PENDING,    // 큐 적재 완료 / 전송 중
    COMPLETED,  // 전송 성공
    FAILED,     // 최종 전송 실패
    CANCELED    // 마케터에 의한 취소
}
