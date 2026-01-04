package com.amore.aketer.domain.association;


import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PersonaItemRepository extends JpaRepository<PersonaItem, Long> {

    @EntityGraph(attributePaths = {"persona", "item", "item.detail", "item.feature"})
    List<PersonaItem> findByPersonaIdOrderByRankAsc(Long personaId);

    // personaId 목록에 대해 rank 오름차순으로 전부 가져오기 (서비스에서 persona별 첫 번째를 top1로 사용)
    @EntityGraph(attributePaths = {"persona", "item"})
    List<PersonaItem> findByPersona_IdInOrderByPersona_IdAscRankAsc(List<Long> personaIds);

    // personaId 목록에 대해 Item + ItemDetail을 Fetch Join으로 가져오기 (브랜드명 조회용)
    @Query("SELECT pi FROM PersonaItem pi " +
            "JOIN FETCH pi.item i " +
            "LEFT JOIN FETCH i.detail " +
            "WHERE pi.persona.id IN :personaIds " +
            "ORDER BY pi.persona.id ASC, pi.rank ASC")
    List<PersonaItem> findByPersonaIdsWithItemDetail(@Param("personaIds") List<Long> personaIds);

    Optional<PersonaItem> findFirstByPersona_IdOrderByRankAsc(Long personaId);
}