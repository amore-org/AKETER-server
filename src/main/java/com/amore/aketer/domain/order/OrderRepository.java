package com.amore.aketer.domain.order;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
    
    boolean existsByUserIdAndItemId(Long userId, Long itemId);
}
