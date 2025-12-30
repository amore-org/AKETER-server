package com.amore.aketer.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    long countByPersona_Id(Long personaId);
}