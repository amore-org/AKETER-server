package com.amore.aketer.domain.persona;

import com.amore.aketer.domain.common.BaseEntity;
import com.amore.aketer.domain.message.MessageReservation;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "persona")
public class Persona extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="name", nullable = false, length = 100)
    private String name;

    @Lob
    @Column(name="profile_text")
    private String profileText;

    @Column(name="member_count")
    private Integer memberCount;

    // 대표 N명 feature (rank 오름차순)
    @OneToMany(mappedBy = "persona", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("rank asc")
    @Builder.Default
    private List<PersonaRepresentativeFeature> representativeFeatures = new ArrayList<>();

    @OneToMany(mappedBy = "persona", fetch = FetchType.LAZY)
    @Builder.Default
    private List<MessageReservation> reservations = new ArrayList<>();
}
