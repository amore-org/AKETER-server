package com.amore.aketer.domain.recommend;

import com.amore.aketer.domain.enums.RecommendTargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RecommendRepository extends JpaRepository<Recommend, Long> {

    Optional<Recommend> findByTargetTypeAndTargetId(RecommendTargetType targetType, Long targetId);

    @Query("""
        SELECT r
        FROM Recommend r
        WHERE r.id IN :recommendIds
          AND NOT EXISTS (
              SELECT 1
              FROM Order o
              WHERE o.user.id = :userId
                AND o.item = r.item
          )
        ORDER BY r.id ASC
        LIMIT 1
    """)
    Optional<Recommend> findFirstValidRecommend(
            @Param("userId") Long userId,
            @Param("recommendIds") List<Long> recommendIds
    );
}
