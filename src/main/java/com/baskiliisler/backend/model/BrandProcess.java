package com.baskiliisler.backend.model;

import com.baskiliisler.backend.type.ProcessStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "brand_process")
public class BrandProcess {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    private Brand brand;

    @Enumerated(EnumType.STRING)
    private ProcessStatus status;

    @Version
    private long version;

    @Column(columnDefinition = "VARCHAR(4000)")
    private String payload;

    private LocalDateTime updatedAt;
}
