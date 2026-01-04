package com.amore.aketer.domain.association;

import com.amore.aketer.domain.common.BaseEntity;
import com.amore.aketer.domain.item.Item;
import com.amore.aketer.domain.persona.Persona;
import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "persona_item",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_persona_item",
                columnNames = {"persona_id", "item_id"}
        ),
        indexes = {
                @Index(name = "idx_persona_item_persona", columnList = "persona_id"),
                @Index(name = "idx_persona_item_item", columnList = "item_id")
        }
)
public class PersonaItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "persona_id", nullable = false)
    private Persona persona;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    /** 최종 추천 정렬용 */
    @Column(name = "item_rank", nullable = false)
    private Integer rank;

    /** 임베딩 유사도 (0~1) */
    @Column(name = "similarity_score", nullable = false)
    private Double similarityScore;

    /** 최종 점수 = α*유사도 + (1-α)*판매선호도 */
    @Column(name = "final_score", nullable = false)
    private Double finalScore;
}
