package com.amore.aketer.domain.user;

import com.amore.aketer.domain.common.BaseEntity;
import com.amore.aketer.domain.persona.Persona;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_persona_id", columnList = "persona_id")
})
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_key", length = 64, unique = true)
    private String userKey;

    @Column(name = "name", length = 50)
    private String name;

    // 배치가 나중에 매핑할 수 있도록 nullable 허용
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "persona_id")
    private Persona persona;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
