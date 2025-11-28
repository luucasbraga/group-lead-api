package com.grouplead.domain.entity;

import com.grouplead.domain.enums.IncidentSeverity;
import com.grouplead.domain.enums.IncidentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "incidents")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deployment_id")
    private Deployment deployment;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IncidentSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private IncidentStatus status = IncidentStatus.OPEN;

    @Column(length = 100)
    private String source;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "acknowledged_at")
    private LocalDateTime acknowledgedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "mttr_minutes")
    private Long mttrMinutes;

    @Column(columnDefinition = "TEXT")
    private String timeline;

    @Column(name = "root_cause", columnDefinition = "TEXT")
    private String rootCause;

    @Column(columnDefinition = "TEXT")
    private String resolution;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Long getRecoveryTimeMinutes() {
        if (mttrMinutes != null) {
            return mttrMinutes;
        }
        if (startedAt == null || resolvedAt == null) {
            return null;
        }
        return Duration.between(startedAt, resolvedAt).toMinutes();
    }

    public boolean isResolved() {
        return status == IncidentStatus.RESOLVED || resolvedAt != null;
    }

    public boolean isOpen() {
        return status == IncidentStatus.OPEN;
    }

    public boolean isCritical() {
        return severity == IncidentSeverity.CRITICAL;
    }

    public boolean isInvestigating() {
        return status == IncidentStatus.INVESTIGATING;
    }
}
