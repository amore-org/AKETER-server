package com.amore.aketer.domain.message;

import com.amore.aketer.domain.common.BaseEntity;
import com.amore.aketer.domain.enums.ChannelType;
import com.amore.aketer.domain.enums.MessageStatus;
import com.amore.aketer.domain.item.Item;
import com.amore.aketer.domain.persona.Persona;
import com.amore.aketer.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "message_reservation", indexes = {
        @Index(name = "idx_msg_res_persona", columnList = "persona_id"),
        @Index(name = "idx_msg_res_scheduled_at", columnList = "scheduled_at"),
        @Index(name = "idx_msg_res_status", columnList = "status")
})
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MessageReservation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "persona_id", nullable = false)
    private Persona persona;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel_type", nullable = false, length = 20)
    private ChannelType channelType;

    /**
     * 채널별 수신자 주소/식별자
     * - SMS  : 전화번호(010...)
     * - KAKAO: 카카오 식별자(예: kakaoUserKey)
     * - EMAIL: 이메일 주소
     */
    @Column(name = "channel_address", nullable = false, length = 128)
    private String channelAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private MessageStatus status;

    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    @Column(name = "retry_count")
    @Builder.Default
    private int retryCount = 0;

    @Version
    @Column(name = "version")
    private Long version;

    public void pending() {
        this.status = MessageStatus.PENDING;
    }

    public void complete() {
        this.status = MessageStatus.COMPLETED;
    }

    public void fail(boolean retryable) {
        if (retryable && this.retryCount < 3) {
            this.retryCount++;
            this.scheduledAt = LocalDateTime.now().plusMinutes(5); // 5분 뒤 재시도
            this.status = MessageStatus.READY;
        } else {
            this.status = MessageStatus.FAILED;
        }
    }

    public void cancel() {
        if (this.status == MessageStatus.READY) {
            this.status = MessageStatus.CANCELED;
        }
    }
}
