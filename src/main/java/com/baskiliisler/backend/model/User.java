package com.baskiliisler.backend.model;

import com.baskiliisler.backend.common.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "password_hash", nullable = false, length = 200)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /** Kullanıcı yalnızca bir bayiye (dealer) bağlı olabilir. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dealer_id")  // NULL ise ⇒ SUPER_ADMIN
    private Dealer dealer;
    
    /** FACTORY_USER rolündeki kullanıcılar için fabrika ataması */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factory_id")  // NULL ise ⇒ FACTORY_USER değil
    private Factory factory;
    
    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
