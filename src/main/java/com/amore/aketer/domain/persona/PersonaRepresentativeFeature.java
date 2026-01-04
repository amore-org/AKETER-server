package com.amore.aketer.domain.persona;

import com.amore.aketer.domain.common.BaseEntity;
import com.amore.aketer.domain.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "persona_representative_feature",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_persona_rank", columnNames = {"persona_id", "sample_rank"}
        ),
        indexes = @Index(name = "idx_prf_persona", columnList = "persona_id")
)
public class PersonaRepresentativeFeature extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 소속 페르소나
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "persona_id", nullable = false)
    private Persona persona;

    // 대표 샘플 순위
    @Column(name = "sample_rank", nullable = false)
    private int rank;

    // UI/로그용 라벨 (ex: "REP_01", "대표#1")
    @Column(name = "sample_label", length = 50)
    private String sampleLabel;

    /**
     * 스냅샷 기준일(이 날짜를 기준으로 최근 N개월/일을 집계)
     * 예: 2025-12-31 기준으로 최근 6개월
     */
    @Column(name = "as_of_date", nullable = false)
    private LocalDate asOfDate;

    /* ===== 대표 샘플 feature 필드들 ===== */

    @Convert(converter = AgeBandConverter.class)
    @Column(name = "age_band", length = 20)
    private AgeBand ageBand;

    @Column(name = "primary_category", length = 30)
    private String primaryCategory;

    @Column(name = "trend_keyword", length = 80)
    private String trendKeyword;

    @Column(name = "core_keyword", length = 80)
    private String coreKeyword;

    @Enumerated(EnumType.STRING)
    @Column(name = "purchase_style", length = 40)
    private PurchaseStyle purchaseStyle;

    @Enumerated(EnumType.STRING)
    @Column(name = "brand_loyalty", length = 40)
    private BrandLoyalty brandLoyalty;

    @Enumerated(EnumType.STRING)
    @Column(name = "price_sensitivity", length = 40)
    private PriceSensitivity priceSensitivity;

    @Enumerated(EnumType.STRING)
    @Column(name = "benefit_sensitivity", length = 40)
    private BenefitSensitivity benefitSensitivity;

    /* ===== 대표 샘플 score 스냅샷 ===== */

    @Column(name = "brand_loyalty_score")
    private Double brandLoyaltyScore; // 최근 6개월 동안 “동일 브랜드 2회 이상 구매한 브랜드들”의 구매 건수가 전체 구매 건수에서 차지하는 비율

    @Column(name = "price_sensitivity_score")
    private Double priceSensitivityScore; // 최근 6개월 구매 중 “할인 적용 상품” 구매 비율

    @Column(name = "benefit_sensitivity_score")
    private Double benefitSensitivityScore; // 최근 6개월 구매 중 “추가 혜택 포함(사은품/세트/쿠폰 등)” 구매 비율
}