package com.baskiliisler.backend.model;

import com.baskiliisler.backend.type.OrderStatus;
import com.baskiliisler.backend.util.StringListConverter;
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

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Sipariş, kabul edilmiş tek bir teklife bağlıdır */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quote_id")
    private Quote quote;

    /** Üretimi yapacak fabrika; henüz atanmamışsa null */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factory_id")
    private Factory factory;

    /** Kalemler (bardak, sticker …) */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;      // sipariş açıldığı an

    private LocalDate     deadline;       // patronun koyduğu üst teslim tarihi
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;    // tüm kalemler teslim olunca set edilir
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;      // her başlık güncellemesinde otomatik set

    @Column(name = "total_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPrice;        // tekliften kopyalanır – değişmez

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    /* ------------ Müşteri Bilgileri ------------ */
    @Column(name = "customer_logo_url")
    private String customerLogoUrl;                   // müşterinin logosu

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;                       // sipariş açıklaması

    @Column(name = "customer_tax_number")
    private String customerTaxNumber;                 // müşterinin vergi numarası

    @Column(name = "image_urls", columnDefinition = "TEXT")
    @Convert(converter = StringListConverter.class)
    @Builder.Default
    private List<String> imageUrls = new ArrayList<>();  // üretim için gönderilen image URL'leri

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dealer_id")
    private Dealer dealer;
    
    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
