
package com.amore.aketer.domain.user;

import com.amore.aketer.domain.common.BaseEntity;
import com.amore.aketer.domain.enums.*;
import com.amore.aketer.domain.persona.Persona;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "user_feature",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_feature_user", columnNames = {"user_id"})
        },
        indexes = {
                @Index(name = "idx_uf_persona", columnList = "persona_id"),
                @Index(name = "idx_uf_asof", columnList = "as_of_date"),
                @Index(name = "idx_uf_primary_category", columnList = "primary_category"),
                @Index(name = "idx_uf_price_sensitivity", columnList = "price_sensitivity"),
                @Index(name = "idx_uf_benefit_sensitivity", columnList = "benefit_sensitivity"),
                @Index(name = "idx_uf_brand_loyalty", columnList = "brand_loyalty")
        }
)
public class UserFeature extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 유저당 1개 row */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 현재 소속 페르소나(군집 결과) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "persona_id")
    private Persona persona;

    /**
     * 스냅샷 기준일(이 날짜를 기준으로 최근 N개월/일을 집계)
     * 예: 2025-12-31 기준으로 최근 6개월
     */
    @Column(name = "as_of_date", nullable = false)
    private LocalDate asOfDate;

    /* ===== user feature 필드들 (bucket) ===== */

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

    /* ===== 점수(score) ===== */

    @Column(name = "brand_loyalty_score")
    private Double brandLoyaltyScore; // “동일 브랜드 2회 이상 구매한 브랜드들” 구매 비율(0~1)

    @Column(name = "price_sensitivity_score")
    private Double priceSensitivityScore; // 할인 적용 구매 비율(0~1)

    @Column(name = "benefit_sensitivity_score")
    private Double benefitSensitivityScore; // 혜택 포함 구매 비율(0~1)
}
