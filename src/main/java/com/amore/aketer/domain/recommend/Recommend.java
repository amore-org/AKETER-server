package com.amore.aketer.domain.recommend;

import com.amore.aketer.domain.common.BaseEntity;
import com.amore.aketer.domain.enums.ChannelType;
import com.amore.aketer.domain.enums.RecommendTargetType;
import com.amore.aketer.domain.item.Item;
import com.amore.aketer.domain.message.Message;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "recommend")
public class Recommend extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private RecommendTargetType targetType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id")
    private Message message;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel_type", length = 20)
    private ChannelType channelType;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Lob
    @Column(name = "recommend_reason")
    private String recommendReason;
}
