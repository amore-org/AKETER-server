package com.amore.aketer.domain.interaction;

import com.amore.aketer.domain.common.BaseEntity;
import com.amore.aketer.domain.item.Item;
import com.amore.aketer.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "user_item_interaction",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_uii_user_item",
                columnNames = {"user_id", "item_id"}
        ),
        indexes = {
                @Index(name = "idx_uii_user", columnList = "user_id"),
                @Index(name = "idx_uii_item", columnList = "item_id"),
                @Index(name = "idx_uii_asof", columnList = "as_of_date")
        }
)
public class UserItemInteraction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 어떤 스냅샷 기준으로 집계한 값인지 */
    @Column(name = "as_of_date", nullable = false)
    private LocalDate asOfDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    /** 클릭 */
    @Column(name = "click_cnt", nullable = false)
    @Builder.Default
    private long clickCnt = 0;

    /** 구매 */
    @Column(name = "purchase_cnt", nullable = false)
    @Builder.Default
    private long purchaseCnt = 0;

    /** 찜 */
    @Column(name = "is_wishlisted", nullable = false)
    @Builder.Default
    private boolean wishlisted = false;

    /** 장바구니 */
    @Column(name = "is_in_cart", nullable = false)
    @Builder.Default
    private boolean inCart = false;

    /** 아이템과의 마지막 상호작용 시각 */
    @Column(name = "last_interaction_at")
    private LocalDateTime lastInteractionAt;
}
