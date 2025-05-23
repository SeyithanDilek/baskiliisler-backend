package com.baskiliisler.backend.repository;

import com.baskiliisler.backend.model.Order;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Order o join fetch o.quote q join fetch o.items oi where o.id = :id")
    Optional<Order> findByIdForUpdate(Long id);
}
