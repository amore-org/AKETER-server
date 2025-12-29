package com.amore.aketer.domain.association;

import com.amore.aketer.domain.common.BaseEntity;
import com.amore.aketer.domain.item.Item;
import com.amore.aketer.domain.persona.Persona;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
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

    // 추천 정렬용
    @Column(name = "item_rank")
    private Integer rank;

    // 매칭 점수
    @Column(name = "score")
    private Double score;
}
