package com.amore.aketer.domain.association;


import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PersonaItemRepository extends JpaRepository<PersonaItem, Long> {

    // personaId 목록에 대해 rank 오름차순으로 전부 가져오기 (서비스에서 persona별 첫 번째를 top1로 사용)
    @EntityGraph(attributePaths = {"persona", "item"})
    List<PersonaItem> findByPersona_IdInOrderByPersona_IdAscRankAsc(List<Long> personaIds);

    Optional<PersonaItem> findFirstByPersona_IdOrderByRankAsc(Long personaId);
}