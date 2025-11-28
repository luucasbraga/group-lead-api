package com.grouplead.domain.entity;

import com.grouplead.domain.enums.MergeRequestStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "merge_requests", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"external_id", "project_id"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MergeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "developer_id")
    private Developer developer;

    @Column(name = "external_id", nullable = false)
    private String externalId;

    @Column(name = "project_id")
    private String projectId;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "source_branch")
    private String sourceBranch;

    @Column(name = "target_branch")
    private String targetBranch;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MergeRequestStatus status;

    @Column(nullable = false)
    @Builder.Default
    private Integer additions = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer deletions = 0;

    @Column(name = "comments_count", nullable = false)
    @Builder.Default
    private Integer commentsCount = 0;

    @OneToMany(mappedBy = "mergeRequest", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Deployment> deployments = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "merged_at")
    private LocalDateTime mergedAt;

    @Column(name = "deployed_at")
    private LocalDateTime deployedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Long getLeadTimeHours() {
        if (createdAt == null || deployedAt == null) {
            return null;
        }
        return Duration.between(createdAt, deployedAt).toHours();
    }

    public Long getReviewTimeHours() {
        if (createdAt == null || mergedAt == null) {
            return null;
        }
        return Duration.between(createdAt, mergedAt).toHours();
    }

    public boolean isMerged() {
        return status == MergeRequestStatus.MERGED;
    }

    public boolean isOpen() {
        return status == MergeRequestStatus.OPEN;
    }

    public int getTotalChanges() {
        return additions + deletions;
    }
}
