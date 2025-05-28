package com.baskiliisler.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 100)
    private String code;                    // "PAP_CUP_M" vb.
    private String name;                    // "Orta Karton Bardak"
    private String unit;                    // "adet", "kg"
    @Column(nullable = false)
    private BigDecimal unitPrice;           // Teklif için taban fiyat
    @Builder.Default
    private boolean active = true;
}
