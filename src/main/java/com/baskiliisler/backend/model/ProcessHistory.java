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
    @JoinColumn(name = "process_id")
    private BrandProcess process;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status")
    private ProcessStatus fromStatus;

    @Enumerated(EnumType.STRING) 
    @Column(name = "to_status", nullable = false)
    private ProcessStatus toStatus;

    @Column(name = "actor_id", nullable = false)
    private Long actorId;

    @Column(name = "changed_at", nullable = false)
    private java.time.LocalDateTime changedAt;

    @Column(columnDefinition = "TEXT")
    @JdbcTypeCode(SqlTypes.LONG32VARCHAR)
    private String payload;
}