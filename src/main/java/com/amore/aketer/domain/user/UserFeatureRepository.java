package com.amore.aketer.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserFeatureRepository extends JpaRepository<UserFeature, Long> {
    Optional<UserFeature> findByUserKey(String userKey);
}