package com.amore.aketer.domain.item;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ItemFeatureRepository extends JpaRepository<ItemFeature, Long> {
    Optional<ItemFeature> findByItemName(String itemName);
}