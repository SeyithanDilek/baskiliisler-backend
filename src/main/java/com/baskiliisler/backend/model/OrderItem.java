package com.baskiliisler.backend.model;

import com.baskiliisler.backend.type.OrderItemStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    private Integer quantity;

    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    @Column(name = "line_total")
    private BigDecimal lineTotal;

    @Column(name = "planned_delivery")
    private LocalDate plannedDelivery; // müşteriyle mutabık tarih

    @Column(name = "delivered_at")
    private LocalDate deliveredAt;  // siparişin teslim edildiği tarih

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OrderItemStatus status = OrderItemStatus.PENDING;
}
