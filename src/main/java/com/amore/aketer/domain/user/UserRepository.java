package com.amore.aketer.domain.user;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findByPersonaId(Long personaId);

    long countByPersona_Id(Long personaId);
}