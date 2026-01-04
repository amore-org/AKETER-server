package com.amore.aketer.api.persona.dto;

import com.amore.aketer.domain.enums.*;

import java.time.LocalDateTime;
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
	List<String> coreKeywords,

	// 발송 히스토리
	List<MessageHistoryDto> messageHistory
) {
	public record MessageHistoryDto(
		Long messageReservationId,
		String messageTitle,
		String messageDescription,
		LocalDateTime scheduledAt,
		String brandName,
		String itemName,
		String status
	) {}
}