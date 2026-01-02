package com.amore.aketer.domain.persona;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PersonaRepresentativeFeatureRepository
	extends JpaRepository<PersonaRepresentativeFeature, Long> {

	List<PersonaRepresentativeFeature>
	findByPersona_IdInOrderByPersona_IdAscRankAsc(List<Long> personaIds);
}