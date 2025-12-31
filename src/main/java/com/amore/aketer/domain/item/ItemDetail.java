package com.amore.aketer.domain.item;

import com.amore.aketer.domain.common.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "item_detail",
        indexes = {
                @Index(name = "idx_item_detail_brand", columnList = "brand_name"),
                @Index(name = "idx_item_detail_major_category", columnList = "major_category"),
                @Index(name = "idx_item_detail_minor_category", columnList = "minor_category")
        }
)
public class ItemDetail extends BaseEntity {

    @Id
    private Long id;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "item_id", nullable = false, unique = true)
    private Item item;

    /** 브랜드명 */
    @Column(name = "brand_name", length = 80)
    private String brandName;

    /** 대분류 카테고리 (스킨케어/메이크업/바디/헤어) */
    @Column(name = "major_category", length = 40)
    private String majorCategory;

    /** 소분류 카테고리 (크림/에센스/토너...) */
    @Column(name = "minor_category", length = 60)
    private String minorCategory;

    /** 상품이 해결하는 주요 고민 (속건조/민감성/트러블/톤개선...) */
    @Lob
    @Column(name = "main_concerns")
    private String mainConcerns; // 예: "민감성, 트러블, 속건조"

    /** 핵심 효능 및 효과 (USP 효능 포함) */
    @Lob
    @Column(name = "key_benefits")
    private String keyBenefits; // 예: "진정, 장벽개선, 보습강화"

    /** 주요 성분 정보 (USP 성분 포함) */
    @Lob
    @Column(name = "key_ingredients")
    private String keyIngredients; // 예: "레티놀, 세라마이드, 히알루론산"

    /** 성분별 기대 효과 설명 (간단 JSON/텍스트) */
    @Lob
    @Column(name = "ingredient_effects")
    private String ingredientEffects;

    /** 적합 피부 타입 (민감성/건성/복합성...) */
    @Column(name = "fit_skin_types", length = 200)
    private String fitSkinTypes;

    /** 제형 및 사용감 특징 (산뜻/촉촉/끈적임없음...) */
    @Column(name = "texture_notes", length = 300)
    private String textureNotes;

    /** 테스트/신뢰 정보 */
    @Column(name = "is_irritation_tested")
    private Boolean irritationTested;

    @Column(name = "is_dermatologist_tested")
    private Boolean dermatologistTested;

    @Column(name = "test_notes", length = 300)
    private String testNotes;

    /** 가격 및 할인 정보 */
    @Column(name = "list_price")
    private Integer listPrice; // 정상가(원)

    @Column(name = "discount_rate")
    private Double discountRate; // 0~1

    @Column(name = "final_price")
    private Integer finalPrice; // 최종 판매가(원)

    /** 프로모션 및 혜택 정보 */
    @Column(name = "promotion_summary", length = 500)
    private String promotionSummary;

    @Column(name = "promotion_end_date")
    private LocalDate promotionEndDate;

    /** 추천 제외 대상 (주의/비추천 조건) */
    @Lob
    @Column(name = "exclusion_notes")
    private String exclusionNotes;

    /** 회사 판매 선호도 점수 (0~1) */
    @Column(name = "sales_preference_score")
    private Double salesPreferenceScore;
}
