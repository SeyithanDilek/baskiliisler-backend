package com.baskiliisler.backend.model;

import com.baskiliisler.backend.type.Unit;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Product extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "products_id_seq")
    @SequenceGenerator(name = "products_id_seq", sequenceName = "products_id_seq", allocationSize = 1)
    private Long id;
    
    @Column(nullable = false)
    private String name;                    // "Orta Karton Bardak"
    
    @Column(columnDefinition = "TEXT")
    private String description;             // Ürün açıklaması
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Unit unit;                      // ADET, KG, METRE vb.
    
    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;           // Teklif için taban fiyat
    
    @Column(name = "tax_rate", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal taxRate = BigDecimal.valueOf(18.00);  // KDV oranı

    @Builder.Default
    private boolean active = true;
}
