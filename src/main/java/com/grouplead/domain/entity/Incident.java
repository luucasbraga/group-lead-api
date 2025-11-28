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

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Long getRecoveryTimeMinutes() {
        if (createdAt == null || resolvedAt == null) {
            return null;
        }
        return Duration.between(createdAt, resolvedAt).toMinutes();
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
}
