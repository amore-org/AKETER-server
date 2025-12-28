package com.amore.aketer.domain.item;

import com.amore.aketer.domain.common.BaseEntity;
import com.amore.aketer.domain.enums.*;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "item_feature",
        indexes = {
                @Index(name = "idx_item_feature_item_name", columnList = "item_name"),
                @Index(name = "idx_item_feature_category", columnList = "primary_category")
        }
)
public class ItemFeature extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 제품명
    @Column(name = "item_name", nullable = false, length = 200)
    private String itemName;

    // 메타데이터 경로(브랜드 톤/문서 묶음 경로 or URL)
    @Column(name = "meta_path", nullable = false, length = 500)
    private String metaPath; // ex) /amore/hera or https://...

    @Convert(converter = AgeBandConverter.class)
    @Column(name = "target_age_segment", length = 20)
    private AgeBand targetAgeSegment;

    @Column(name = "target_age_min")
    private Integer targetAgeMin;

    @Column(name = "target_age_max")
    private Integer targetAgeMax;

    @Enumerated(EnumType.STRING)
    @Column(name = "primary_category", length = 40)
    private ItemCategory primaryCategory;

    // 제품 성분 텍스트 파일 경로
    @Column(name = "ingredients_doc_path", length = 500)
    private String ingredientsDocPath;

    // 제품 USP 텍스트 파일 경로
    @Column(name = "usp_doc_path", length = 500)
    private String uspDocPath;

    // price_position (가성비/프리미엄/할인 적용 상품)
    @Enumerated(EnumType.STRING)
    @Column(name = "price_position", length = 40)
    private PricePosition pricePosition;

    // promotion_type (기획전/시즌 프로모션/증정품)
    @Enumerated(EnumType.STRING)
    @Column(name = "promotion_type", length = 40)
    private PromotionType promotionType;
}
