package com.amore.aketer.domain.item;

import com.amore.aketer.domain.common.BaseEntity;
import com.amore.aketer.domain.enums.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "item_feature", indexes = {
        @Index(name = "idx_item_feature_category", columnList = "primary_category")
})
public class ItemFeature extends BaseEntity {

    @Id
    private Long id;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "item_id", nullable = false, unique = true)
    private Item item;

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

    @Lob
    @Column(name="profile_text")
    private String profileText;

    // 제품 성분 텍스트
    @Column(name = "ingredients_doc", length = 500)
    private String ingredientsDoc;

    // 제품 USP 텍스트
    @Column(name = "usp_doc", length = 500)
    private String uspDoc;

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

    /** 회사 판매 선호도 점수 (0~1) */
    @Column(name = "sales_preference_score")
    private Double salesPreferenceScore;
}
