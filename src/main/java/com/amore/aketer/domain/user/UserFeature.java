package com.amore.aketer.domain.user;

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
        name = "user_feature",
        indexes = {
                @Index(name = "idx_user_feature_user_key", columnList = "user_key", unique = true)
        }
)
public class UserFeature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 프로토타입용 유저 식별자(아모레몰 userId 등)
    @Column(name = "user_key", nullable = false, length = 64)
    private String userKey;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

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
