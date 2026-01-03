package com.amore.aketer.workflow.online.agent.state;

import com.amore.aketer.domain.enums.AgeBand;
import com.amore.aketer.domain.enums.PricePosition;
import com.amore.aketer.domain.enums.PromotionType;
import com.amore.aketer.domain.item.Item;
import com.amore.aketer.domain.item.ItemDetail;
import com.amore.aketer.domain.item.ItemFeature;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemState implements Serializable {

    private static final long serialVersionUID = 1L;

    // Item 엔티티 필드
    private String itemName;
    private String itemMetaPath;

    // ItemDetail 엔티티 필드
    private String brandName;
    private String majorCategory;
    private String minorCategory;
    private String mainConcerns;
    private String keyBenefits;
    private String keyIngredients;
    private String ingredientEffects;
    private String fitSkinTypes;
    private String textureNotes;
    private Boolean irritationTested;
    private Boolean dermatologistTested;
    private String testNotes;
    private Integer listPrice;
    private Double discountRate;
    private Integer finalPrice;
    private String promotionSummary;
    private LocalDate promotionEndDate;
    private String exclusionNotes;

    // ItemFeature 엔티티 필드
    private AgeBand targetAgeSegment;
    private String profileText;
    private PricePosition pricePosition;
    private PromotionType promotionType;

    public static ItemState from(Item item) {
        ItemDetail detail = item.getDetail();
        ItemFeature feature = item.getFeature();

        ItemStateBuilder itemBuilder = ItemState.builder();

        itemBuilder.itemName(item.getName())
                .itemMetaPath(item.getMetaPath());

        if (detail != null) {
            itemBuilder
                    .brandName(detail.getBrandName())
                    .majorCategory(detail.getMajorCategory())
                    .minorCategory(detail.getMinorCategory())
                    .mainConcerns(detail.getMainConcerns())
                    .keyBenefits(detail.getKeyBenefits())
                    .keyIngredients(detail.getKeyIngredients())
                    .ingredientEffects(detail.getIngredientEffects())
                    .fitSkinTypes(detail.getFitSkinTypes())
                    .textureNotes(detail.getTextureNotes())
                    .irritationTested(detail.getIrritationTested())
                    .dermatologistTested(detail.getDermatologistTested())
                    .testNotes(detail.getTestNotes())
                    .listPrice(detail.getListPrice())
                    .discountRate(detail.getDiscountRate())
                    .finalPrice(detail.getFinalPrice())
                    .promotionSummary(detail.getPromotionSummary())
                    .promotionEndDate(detail.getPromotionEndDate())
                    .exclusionNotes(detail.getExclusionNotes());
        }

        if (feature != null) {
            itemBuilder
                    .targetAgeSegment(feature.getTargetAgeSegment())
                    .profileText(feature.getProfileText())
                    .pricePosition(feature.getPricePosition())
                    .promotionType(feature.getPromotionType());
        }

        return itemBuilder.build();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[상품 상세 정보]\n");
        sb.append(String.format("- 브랜드: %s\n", brandName != null ? brandName : "정보 없음"));
        sb.append(String.format("- 상품명: %s\n", itemName != null ? itemName : "정보 없음"));
        sb.append(String.format("- 카테고리: %s > %s\n", 
                majorCategory != null ? majorCategory : "미지정", 
                minorCategory != null ? minorCategory : "미지정"));
        
        sb.append("\n[핵심 특징 및 효능]\n");
        if (keyBenefits != null) sb.append(String.format("- 주요 효능: %s\n", keyBenefits));
        if (mainConcerns != null) sb.append(String.format("- 해결 가능 고민: %s\n", mainConcerns));
        if (keyIngredients != null) sb.append(String.format("- 핵심 성분: %s\n", keyIngredients));
        if (ingredientEffects != null) sb.append(String.format("- 성분 기대 효과: %s\n", ingredientEffects));
        if (textureNotes != null) sb.append(String.format("- 제형 및 사용감: %s\n", textureNotes));
        if (profileText != null) sb.append(String.format("- 상품 소개: %s\n", profileText));

        sb.append("\n[가격 및 프로모션 혜택]\n");
        if (finalPrice != null) {
            sb.append(String.format("- 최종 판매가: %,d원\n", finalPrice));
            if (listPrice != null && !finalPrice.equals(listPrice)) {
                sb.append(String.format("- 정상가: %,d원\n", listPrice));
                if (discountRate != null && discountRate > 0) {
                    sb.append(String.format("- 할인율: %.0f%%\n", discountRate * 100));
                }
            }
        }
        if (pricePosition != null) sb.append(String.format("- 가격 포지션: %s\n", pricePosition));
        if (promotionSummary != null) {
            sb.append(String.format("- 프로모션 내용: %s", promotionSummary));
            if (promotionEndDate != null) sb.append(String.format(" (기간: ~%s)", promotionEndDate));
            sb.append("\n");
        }
        if (promotionType != null) sb.append(String.format("- 프로모션 유형: %s\n", promotionType));

        sb.append("\n[고객 맞춤 정보 및 신뢰도]\n");
        if (targetAgeSegment != null) sb.append(String.format("- 추천 연령대: %s\n", targetAgeSegment));
        if (fitSkinTypes != null) sb.append(String.format("- 추천 피부 타입: %s\n", fitSkinTypes));
        if (irritationTested != null) sb.append(String.format("- 자극 테스트 완료: %s\n", irritationTested ? "예" : "아니오"));
        if (dermatologistTested != null) sb.append(String.format("- 피부과 테스트 완료: %s\n", dermatologistTested ? "예" : "아니오"));
        if (testNotes != null) sb.append(String.format("- 테스트 특이사항: %s\n", testNotes));
        if (exclusionNotes != null) sb.append(String.format("- 주의사항/추천 제외: %s\n", exclusionNotes));

        return sb.toString();
    }
}
