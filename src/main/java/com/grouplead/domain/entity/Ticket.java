package com.grouplead.domain.entity;

import com.grouplead.domain.enums.TicketSource;
import com.grouplead.domain.enums.TicketStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tickets", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"external_id", "source"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", nullable = false)
    private String externalId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketSource source;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "developer_id")
    private Developer developer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sprint_id")
    private Sprint sprint;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status;

    @Column(length = 50)
    private String priority;

    @Column(name = "ticket_type", length = 50)
    private String ticketType;

    @Column(name = "story_points")
    private Integer storyPoints;

    @ElementCollection
    @CollectionTable(name = "ticket_labels", joinColumns = @JoinColumn(name = "ticket_id"))
    @Column(name = "label")
    @Builder.Default
    private Set<String> labels = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "external_updated_at")
    private LocalDateTime externalUpdatedAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Long getCycleTimeHours() {
        if (startedAt == null || completedAt == null) {
            return null;
        }
        return java.time.Duration.between(startedAt, completedAt).toHours();
    }

    public boolean isCompleted() {
        return status == TicketStatus.DONE || status == TicketStatus.CLOSED;
    }

    public boolean isInProgress() {
        return status == TicketStatus.IN_PROGRESS;
    }

    public boolean isBlocked() {
        return status == TicketStatus.BLOCKED;
    }
}
