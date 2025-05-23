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
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Product product;

    private Integer quantity;

    private BigDecimal unitPrice;

    private BigDecimal lineTotal;

    private LocalDate plannedDelivery; // müşteriyle mutabık tarih

    private LocalDate deliveredAt;  // siparişin teslim edildiği tarih

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OrderItemStatus status = OrderItemStatus.PENDING;
}
