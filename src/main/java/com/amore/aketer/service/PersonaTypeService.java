package com.amore.aketer.service;

import com.amore.aketer.api.persona.dto.PersonaTypeRowResponse;
import com.amore.aketer.domain.message.MessageReservation;
import com.amore.aketer.domain.message.MessageReservationRepository;
import com.amore.aketer.domain.persona.Persona;
import com.amore.aketer.domain.persona.PersonaRepository;
import com.amore.aketer.domain.persona.PersonaRepresentativeFeature;
import com.amore.aketer.domain.persona.PersonaRepresentativeFeatureRepository;
import com.amore.aketer.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PersonaTypeService {

	private final PersonaRepository personaRepository;
	private final PersonaRepresentativeFeatureRepository prfRepository;
	private final UserRepository userRepository;
	private final MessageReservationRepository messageReservationRepository;

	@Transactional(readOnly = true)
	public Page<PersonaTypeRowResponse> listPersonaTypes(Pageable pageable) {

		Page<Persona> page = personaRepository.findAll(pageable);

		List<Long> personaIds = page.getContent().stream()
			.map(Persona::getId)
			.toList();

		// PersonaRepresentativeFeature
		Map<Long, List<PersonaRepresentativeFeature>> featuresByPersonaId = new HashMap<>();
		if (!personaIds.isEmpty()) {
			List<PersonaRepresentativeFeature> all =
				prfRepository.findByPersona_IdInOrderByPersona_IdAscRankAsc(personaIds);

			featuresByPersonaId = all.stream()
				.collect(Collectors.groupingBy(
					f -> f.getPersona().getId(),
					LinkedHashMap::new,
					Collectors.toList()
				));
		}

		// MessageReservation
		Map<Long, List<MessageReservation>> messageHistoryByPersonaId = new HashMap<>();
		if (!personaIds.isEmpty()) {
			List<MessageReservation> allReservations =
				messageReservationRepository.findByPersona_IdInOrderByPersona_IdAscScheduledAtDesc(personaIds);

			messageHistoryByPersonaId = allReservations.stream()
				.collect(Collectors.groupingBy(
					r -> r.getPersona().getId(),
					LinkedHashMap::new,
					Collectors.toList()
				));
		}

		Map<Long, List<PersonaRepresentativeFeature>> finalFeaturesByPersonaId = featuresByPersonaId;
		Map<Long, List<MessageReservation>> finalMessageHistoryByPersonaId = messageHistoryByPersonaId;
		return page.map(persona -> {
			Long personaId = persona.getId();

			Integer memberCount = persona.getMemberCount();
			if (memberCount == null) {
				memberCount = (int) userRepository.countByPersona_Id(personaId);
			}

			// persona별 전체 대표 feature
			List<PersonaRepresentativeFeature> feats =
				finalFeaturesByPersonaId.getOrDefault(personaId, List.of());

			// rank=1 (없으면 null)
			PersonaRepresentativeFeature top1 = feats.isEmpty() ? null : feats.get(0);

			List<PersonaRepresentativeFeature> top3 = feats.size() <= 3 ? feats : feats.subList(0, 3);

			// rank 1~3 trendKeywords
			List<String> trendKeywords = top3.stream()
				.map(PersonaRepresentativeFeature::getTrendKeyword)
				.map(PersonaTypeService::normalize)
				.filter(Objects::nonNull)
				.distinct()
				.toList();

			// rank 1~3 coreKeywords
			List<String> coreKeywords = top3.stream()
				.map(PersonaRepresentativeFeature::getCoreKeyword)
				.map(PersonaTypeService::normalize)
				.filter(Objects::nonNull)
				.distinct()
				.toList();

			// primaryCategory rank=1
			String primaryCategory = (top1 != null) ? top1.getPrimaryCategory() : null;

			// 발송 히스토리 변환
			List<PersonaTypeRowResponse.MessageHistoryDto> messageHistory =
				finalMessageHistoryByPersonaId.getOrDefault(personaId, List.of())
					.stream()
					.map(r -> {
						var msg = r.getMessage();
						var item = r.getItem();
						return new PersonaTypeRowResponse.MessageHistoryDto(
							r.getId(),
							(msg != null) ? msg.getTitle() : null,
							(msg != null) ? msg.getBody() : null,
							r.getScheduledAt(),
							(item != null && item.getDetail() != null) ? item.getDetail().getBrandName() : null,
							(item != null) ? item.getName() : null,
							r.getStatus().name()
						);
					})
					.toList();

			return new PersonaTypeRowResponse(
				personaId,
				persona.getName(),
				memberCount,
				top1 != null ? top1.getAgeBand() : null,
				primaryCategory,
				top1 != null ? top1.getPurchaseStyle() : null,
				top1 != null ? top1.getBrandLoyalty() : null,
				top1 != null ? top1.getPriceSensitivity() : null,
				top1 != null ? top1.getBenefitSensitivity() : null,
				trendKeywords,
				coreKeywords,
				messageHistory
			);
		});
	}

	private static String normalize(String s) {
		if (s == null) return null;
		String t = s.trim();
		return t.isEmpty() ? null : t;
	}
}