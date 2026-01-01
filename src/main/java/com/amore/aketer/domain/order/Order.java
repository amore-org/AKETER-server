package com.amore.aketer.domain.order;

import com.amore.aketer.domain.common.BaseEntity;
import com.amore.aketer.domain.item.Item;
import com.amore.aketer.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "orders",
        indexes = {
                @Index(name = "idx_orders_user", columnList = "user_id"),
                @Index(name = "idx_orders_item", columnList = "item_id"),
                @Index(name = "idx_orders_ordered_at", columnList = "ordered_at")
        }
)
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 주문자
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 구매 아이템
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(name = "qty", nullable = false)
    private Integer qty;

    // 주문 당시 단가 스냅샷 (원 단위)
    @Column(name = "unit_price", nullable = false)
    private Integer unitPrice;

    @Column(name = "ordered_at", nullable = false)
    private LocalDateTime orderedAt;
}
