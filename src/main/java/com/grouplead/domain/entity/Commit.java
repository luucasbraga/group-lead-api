package com.grouplead.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "commits")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Commit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "developer_id")
    private Developer developer;

    @Column(nullable = false, unique = true)
    private String sha;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false)
    @Builder.Default
    private Integer additions = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer deletions = 0;

    @Column(name = "files_changed", nullable = false)
    @Builder.Default
    private Integer filesChanged = 0;

    @Column(name = "project_id")
    private String projectId;

    private String branch;

    @Column(name = "committed_at", nullable = false)
    private LocalDateTime committedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public int getTotalChanges() {
        return additions + deletions;
    }

    public boolean isAfterHours() {
        int hour = committedAt.getHour();
        return hour < 9 || hour >= 20;
    }
}
