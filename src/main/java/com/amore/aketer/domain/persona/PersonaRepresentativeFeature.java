package com.amore.aketer.domain.persona;

import com.amore.aketer.domain.common.BaseEntity;
import com.amore.aketer.domain.enums.*;
import jakarta.persistence.*;
import lombok.*;

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

    /* ===== 대표 샘플 feature 필드들 ===== */

    @Convert(converter = AgeBandConverter.class)
    @Column(name = "age_band", length = 20)
    private AgeBand ageBand;

    @Column(name = "primary_category", length = 30)
    private String primaryCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "purchase_style", length = 40)
    private PurchaseStyle purchaseStyle;

    @Enumerated(EnumType.STRING)
    @Column(name = "brand_loyalty", length = 40)
    private BrandLoyalty brandLoyalty;

    @Column(name = "trend_keyword", length = 80)
    private String trendKeyword;

    @Column(name = "core_keyword", length = 80)
    private String coreKeyword;

    @Enumerated(EnumType.STRING)
    @Column(name = "price_sensitivity", length = 40)
    private PriceSensitivity priceSensitivity;

    @Enumerated(EnumType.STRING)
    @Column(name = "benefit_sensitivity", length = 40)
    private BenefitSensitivity benefitSensitivity;
}
