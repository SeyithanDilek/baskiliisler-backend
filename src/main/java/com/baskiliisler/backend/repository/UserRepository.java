package com.baskiliisler.backend.repository;

import com.baskiliisler.backend.model.User;
import com.baskiliisler.backend.model.Dealer;
import com.baskiliisler.backend.common.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u ORDER BY u.updatedAt DESC")
    List<User> findAll();
    
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByRole(Role role);
    List<User> findByDealer(Dealer dealer);
    long countByDealer(Dealer dealer);
    
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.factory.id = :factoryId")
    List<User> findByRoleAndFactoryId(@Param("role") Role role, @Param("factoryId") Long factoryId);
}
