package com.baskiliisler.backend.model;

import com.baskiliisler.backend.type.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class Order {

    @Id @GeneratedValue
    private Long id;

    /** Sipariş, kabul edilmiş tek bir teklife bağlıdır */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    private Quote quote;

    /** Üretimi yapacak fabrika; henüz atanmamışsa null */
    @ManyToOne(fetch = FetchType.LAZY)
    private Factory factory;

    /** Kalemler (bardak, sticker …) */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;      // sipariş açıldığı an

    private LocalDate     deadline;       // patronun koyduğu üst teslim tarihi
    private LocalDateTime deliveredAt;    // tüm kalemler teslim olunca set edilir
    private LocalDateTime updatedAt;      // her başlık güncellemesinde otomatik set

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPrice;        // tekliften kopyalanır – değişmez

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;
}
