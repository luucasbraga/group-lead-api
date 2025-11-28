package com.grouplead.domain.entity;

import com.grouplead.domain.enums.AlertSeverity;
import com.grouplead.domain.enums.AlertType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "alerts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    private AlertType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AlertSeverity severity;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "metric_name")
    private String metricName;

    @Column(name = "metric_value")
    private Double metricValue;

    @Column(name = "threshold_value")
    private Double thresholdValue;

    @Column(nullable = false)
    @Builder.Default
    private Boolean acknowledged = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acknowledged_by")
    private User acknowledgedBy;

    @Column(name = "acknowledged_at")
    private LocalDateTime acknowledgedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public boolean isActive() {
        return resolvedAt == null;
    }

    public boolean isCritical() {
        return severity == AlertSeverity.CRITICAL;
    }

    public void acknowledge(User user) {
        this.acknowledged = true;
        this.acknowledgedBy = user;
        this.acknowledgedAt = LocalDateTime.now();
    }

    public void resolve() {
        this.resolvedAt = LocalDateTime.now();
    }
}
