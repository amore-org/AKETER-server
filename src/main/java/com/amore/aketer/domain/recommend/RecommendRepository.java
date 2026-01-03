package com.amore.aketer.domain.recommend;

import com.amore.aketer.domain.enums.RecommendTargetType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RecommendRepository extends JpaRepository<Recommend, Long> {

    Optional<Recommend> findByTargetTypeAndTargetId(RecommendTargetType targetType, Long targetId);
}