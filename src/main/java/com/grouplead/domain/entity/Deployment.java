package com.grouplead.domain.entity;

import com.grouplead.domain.enums.DeploymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "deployments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Deployment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merge_request_id")
    private MergeRequest mergeRequest;

    @Column(nullable = false, length = 100)
    private String environment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeploymentStatus status;

    @Column(length = 100)
    private String version;

    @Column(name = "caused_incident")
    @Builder.Default
    private Boolean causedIncident = false;

    @Column(name = "deployed_at", nullable = false)
    private LocalDateTime deployedAt;

    @OneToMany(mappedBy = "deployment", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Incident> incidents = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public boolean isSuccess() {
        return status == DeploymentStatus.SUCCESS;
    }

    public boolean isFailed() {
        return status == DeploymentStatus.FAILED;
    }

    public boolean isProduction() {
        return "production".equalsIgnoreCase(environment) || "prod".equalsIgnoreCase(environment);
    }
}
