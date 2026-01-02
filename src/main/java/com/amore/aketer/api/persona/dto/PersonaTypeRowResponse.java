package com.amore.aketer.api.persona.dto;

import com.amore.aketer.domain.enums.*;

import java.util.List;

public record PersonaTypeRowResponse(
	Long personaId,
	String personaName,
	Integer memberCount,

	AgeBand ageBand,
	String primaryCategory,
	PurchaseStyle purchaseStyle,
	BrandLoyalty brandLoyalty,
	PriceSensitivity priceSensitivity,
	BenefitSensitivity benefitSensitivity,

	List<String> trendKeywords,
	List<String> coreKeywords
) {}