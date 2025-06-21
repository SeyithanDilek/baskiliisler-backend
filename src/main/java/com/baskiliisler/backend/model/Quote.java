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
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@Table(name = "quotes")
public class Quote {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* ------------ İlişkiler ------------ */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;                              // kime teklif verildi?

    @OneToMany(mappedBy = "quote", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<QuoteItem> items = new ArrayList<>(); // ürün satırları  // yeni

    /* ------------ Temel Alanlar ------------ */
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;                  // otomatik         // değişmedi

    @Column(name = "total_price", nullable = false)
    private BigDecimal totalPrice;                    // items'den derive  // (re-calc)

    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "TRY";                  // ISO-4217          // değişmedi

    @Column(name = "pdf_url")
    private String pdfUrl;                            // render edilmiş PDF

    /* ------------ Ek Alanlar ------------ */
    @Column(name = "valid_until")
    private LocalDate validUntil;                     // teklif geçerlilik // yeni

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private QuoteStatus status = QuoteStatus.DRAFT;   // yeni

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;                 // marka onay zamanı // yeni

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;                  // otomatik          // yeni
}
