package com.grouplead.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "developers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Developer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(length = 100)
    private String role;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @ElementCollection
    @CollectionTable(name = "developer_external_ids", joinColumns = @JoinColumn(name = "developer_id"))
    @MapKeyColumn(name = "source")
    @Column(name = "external_id")
    @Builder.Default
    private Map<String, String> externalIds = new HashMap<>();

    @OneToMany(mappedBy = "developer", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Ticket> tickets = new ArrayList<>();

    @OneToMany(mappedBy = "developer", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Commit> commits = new ArrayList<>();

    @OneToMany(mappedBy = "developer", cascade = CascadeType.ALL)
    @Builder.Default
    private List<MergeRequest> mergeRequests = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public void addExternalId(String source, String externalId) {
        this.externalIds.put(source, externalId);
    }

    public String getExternalId(String source) {
        return this.externalIds.get(source);
    }
}
