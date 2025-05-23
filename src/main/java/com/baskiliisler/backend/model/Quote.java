package com.baskiliisler.backend.model;

import com.baskiliisler.backend.model.QuoteItem;
import com.baskiliisler.backend.type.QuoteStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@Table(name = "quotes")
public class Quote {

    @Id @GeneratedValue
    private Long id;

    /* ------------ İlişkiler ------------ */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Brand brand;                              // kime teklif verildi?

    @OneToMany(mappedBy = "quote", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuoteItem> items;                    // ürün satırları  // yeni

    /* ------------ Temel Alanlar ------------ */
    @CreationTimestamp
    private LocalDateTime createdAt;                  // otomatik         // değişmedi

    @Column(nullable = false)
    private BigDecimal totalPrice;                    // items'den derive  // (re-calc)

    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "TRY";                  // ISO-4217          // değişmedi

    private String pdfUrl;                            // render edilmiş PDF

    /* ------------ Ek Alanlar ------------ */
    private LocalDate validUntil;                     // teklif geçerlilik // yeni

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private QuoteStatus status = QuoteStatus.DRAFT;   // yeni

    private LocalDateTime acceptedAt;                 // marka onay zamanı // yeni

    @UpdateTimestamp
    private LocalDateTime updatedAt;                  // otomatik          // yeni
}
