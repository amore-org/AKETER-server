package com.amore.aketer.domain.persona;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonaRepository extends JpaRepository<Persona, Long> {
    List<Persona> findByCreatedAtAfter(Instant createdAt);
}
