package com.baskiliisler.backend.repository;

import com.baskiliisler.backend.model.Dealer;
import com.baskiliisler.backend.model.Order;
import com.baskiliisler.backend.model.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT o FROM Order o ORDER BY o.updatedAt DESC")
    List<Order> findAll();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Order o join fetch o.quote q join fetch o.items oi where o.id = :id")
    Optional<Order> findByIdForUpdate(Long id);
    
    List<Order> findByQuoteBrandId(Long brandId);
    
    List<Order> findByFactoryId(Long factoryId);
    
    @Query("SELECT o FROM Order o JOIN FETCH o.quote q JOIN FETCH q.brand b WHERE b.dealer = :dealer")
    List<Order> findByDealer(@Param("dealer") Dealer dealer);
    
    @Query("SELECT COUNT(o) FROM Order o JOIN o.quote q JOIN q.brand b WHERE b.dealer = :dealer AND o.status = :status")
    Integer countByDealerAndStatus(@Param("dealer") Dealer dealer, @Param("status") com.baskiliisler.backend.type.OrderStatus status);
    
    // Authorization queries for dealer users
    @Query("SELECT o FROM Order o JOIN FETCH o.quote q JOIN FETCH q.brand b WHERE b.assignedUser = :user ORDER BY o.updatedAt DESC")
    List<Order> findByQuoteBrandAssignedUser(@Param("user") User user);
    
    @Query("SELECT o FROM Order o JOIN FETCH o.quote q JOIN FETCH q.brand b LEFT JOIN FETCH o.items WHERE b.assignedUser = :user ORDER BY o.updatedAt DESC")
    List<Order> findByQuoteBrandAssignedUserWithItems(@Param("user") User user);
    
    @Query("SELECT o FROM Order o JOIN FETCH o.quote q JOIN FETCH q.brand b WHERE b.assignedUser = :user AND o.status = :status ORDER BY o.updatedAt DESC")
    List<Order> findByQuoteBrandAssignedUserAndStatus(@Param("user") User user, @Param("status") com.baskiliisler.backend.type.OrderStatus status);
    
    @Query("SELECT COUNT(o) FROM Order o JOIN o.quote q JOIN q.brand b WHERE b.assignedUser = :user")
    Integer countByQuoteBrandAssignedUser(@Param("user") User user);
    
    @Query("SELECT COUNT(o) FROM Order o JOIN o.quote q JOIN q.brand b WHERE b.assignedUser = :user AND o.status = :status")
    Integer countByQuoteBrandAssignedUserAndStatus(@Param("user") User user, @Param("status") com.baskiliisler.backend.type.OrderStatus status);
}
