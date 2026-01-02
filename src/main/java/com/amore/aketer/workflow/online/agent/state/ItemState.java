package com.amore.aketer.workflow.online.agent.state;

import com.amore.aketer.domain.enums.AgeBand;
import com.amore.aketer.domain.enums.ItemCategory;
import com.amore.aketer.domain.enums.PricePosition;
import com.amore.aketer.domain.enums.PromotionType;
import com.amore.aketer.domain.item.Item;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemState {

    private Item item;
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

    private AgeBand targetAgeSegment;
    private ItemCategory primaryCategory;
    private String profileText;
    private String ingredientsDoc;
    private String uspDoc;
    private PricePosition pricePosition;
    private PromotionType promotionType;
}
