package com.baskiliisler.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuoteItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Quote quote;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Product product;

    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;
}