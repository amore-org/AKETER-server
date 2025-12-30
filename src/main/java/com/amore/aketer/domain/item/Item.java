package com.amore.aketer.domain.item;

import com.amore.aketer.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "item", indexes = {
        @Index(name = "idx_item_item_key", columnList = "item_key", unique = true),
        @Index(name = "idx_item_name", columnList = "name")
})
public class Item extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 외부 상품 식별자(아모레몰 productId 등)
    @Column(name = "item_key", length = 64, unique = true)
    private String itemKey;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    // 브랜드 톤/문서 묶음 경로 or URL
    @Column(name = "meta_path", nullable = false, length = 500)
    private String metaPath;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @OneToOne(mappedBy = "item", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private ItemFeature feature;
}
