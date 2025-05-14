package com.baskiliisler.backend.model;

import com.baskiliisler.backend.type.ProcessStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Getter @Builder @NoArgsConstructor @AllArgsConstructor
@Table(name = "process_history")
public class ProcessHistory {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private BrandProcess process;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private ProcessStatus fromStatus;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private ProcessStatus toStatus;

    @Column(nullable = false)
    private Long actorId;

    @Column(nullable = false)
    private java.time.LocalDateTime changedAt;

    @Column(columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String payload;
}