package com.amore.aketer.workflow.online.agent.state;

import com.amore.aketer.domain.enums.*;
import com.amore.aketer.domain.persona.Persona;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

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
    @NoArgsConstructor
    @AllArgsConstructor
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

    public static PersonaState from(Persona persona) {
        List<PersonaRepresentativeState> repStates = persona.getRepresentativeFeatures().stream()
                .map(f -> PersonaRepresentativeState.builder()
                        .rank(f.getRank())
                        .ageBand(f.getAgeBand())
                        .primaryCategory(f.getPrimaryCategory())
                        .trendKeyword(f.getTrendKeyword())
                        .coreKeyword(f.getCoreKeyword())
                        .purchaseStyle(f.getPurchaseStyle())
                        .brandLoyalty(f.getBrandLoyalty())
                        .priceSensitivity(f.getPriceSensitivity())
                        .benefitSensitivity(f.getBenefitSensitivity())
                        .brandLoyaltyScore(f.getBrandLoyaltyScore())
                        .priceSensitivityScore(f.getPriceSensitivityScore())
                        .benefitSensitivityScore(f.getBenefitSensitivityScore())
                        .build())
                .collect(Collectors.toList());

        return PersonaState.builder()
                .name(persona.getName())
                .profileText(persona.getProfileText())
                .personaRepresentativeStates(repStates)
                .build();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[페르소나 정보]\n");
        sb.append(String.format("- 이름: %s\n", name != null ? name : "정보 없음"));
        sb.append(String.format("- 특징: %s\n", profileText != null ? profileText : "정보 없음"));

        if (personaRepresentativeStates != null && !personaRepresentativeStates.isEmpty()) {
            sb.append("- 상세 분석 (대표 특징 목록):\n");
            for (PersonaRepresentativeState rep : personaRepresentativeStates) {
                sb.append(String.format("  [순위: %d]\n", rep.getRank()));
                if (rep.getAgeBand() != null) sb.append(String.format("    * 연령대: %s\n", rep.getAgeBand()));
                if (rep.getPrimaryCategory() != null) sb.append(String.format("    * 선호 카테고리: %s\n", rep.getPrimaryCategory()));
                if (rep.getTrendKeyword() != null) sb.append(String.format("    * 트렌드 키워드: %s\n", rep.getTrendKeyword()));
                if (rep.getCoreKeyword() != null) sb.append(String.format("    * 핵심 키워드: %s\n", rep.getCoreKeyword()));
                if (rep.getPurchaseStyle() != null) sb.append(String.format("    * 구매 스타일: %s\n", rep.getPurchaseStyle()));
                if (rep.getBrandLoyalty() != null) sb.append(String.format("    * 브랜드 충성도: %s (점수: %.2f)\n", rep.getBrandLoyalty(), rep.getBrandLoyaltyScore()));
                if (rep.getPriceSensitivity() != null) sb.append(String.format("    * 가격 민감도: %s (점수: %.2f)\n", rep.getPriceSensitivity(), rep.getPriceSensitivityScore()));
                if (rep.getBenefitSensitivity() != null) sb.append(String.format("    * 혜택 민감도: %s (점수: %.2f)\n", rep.getBenefitSensitivity(), rep.getBenefitSensitivityScore()));
            }
        }

        return sb.toString();
    }
}
