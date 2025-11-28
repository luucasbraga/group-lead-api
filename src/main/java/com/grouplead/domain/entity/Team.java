package com.grouplead.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "teams")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Developer> developers = new ArrayList<>();

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Sprint> sprints = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "aws_resources", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, String> awsResources = new HashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "integration_config", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, String> integrationConfig = new HashMap<>();

    @Column(name = "jira_project_key")
    private String jiraProjectKey;

    @Column(name = "gitlab_project_id")
    private String gitlabProjectId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public void addDeveloper(Developer developer) {
        developers.add(developer);
        developer.setTeam(this);
    }

    public void removeDeveloper(Developer developer) {
        developers.remove(developer);
        developer.setTeam(null);
    }
}
