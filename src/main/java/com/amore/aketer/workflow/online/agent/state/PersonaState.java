package com.amore.aketer.workflow.online.agent.state;

import com.amore.aketer.domain.enums.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonaState {

    private String name;
    private String profileText;

    private List<PersonaRepresentativeState> personaRepresentativeStates;

    @Getter
    @Builder
    public static class PersonaRepresentativeState {
        private int rank;
        private AgeBand ageBand;
        private String primaryCategory;
        private String trendKeyword;
        private String coreKeyword;
        private PurchaseStyle purchaseStyle;
        private BrandLoyalty brandLoyalty;
        private PriceSensitivity priceSensitivity;
        private BenefitSensitivity benefitSensitivity;
        private Double brandLoyaltyScore;
        private Double priceSensitivityScore;
        private Double benefitSensitivityScore;
    }
}
