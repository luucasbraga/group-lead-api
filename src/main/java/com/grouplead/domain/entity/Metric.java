package com.grouplead.domain.entity;

import com.grouplead.domain.enums.MetricType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "metrics", indexes = {
        @Index(name = "idx_metrics_type_timestamp", columnList = "type, timestamp")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Metric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    private MetricType type;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double value;

    @Column(length = 50)
    private String unit;

    @Column(nullable = false, length = 100)
    private String source;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @ElementCollection
    @CollectionTable(name = "metric_tags", joinColumns = @JoinColumn(name = "metric_id"))
    @MapKeyColumn(name = "tag_key")
    @Column(name = "tag_value")
    @Builder.Default
    private Map<String, String> tags = new HashMap<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public void addTag(String key, String value) {
        this.tags.put(key, value);
    }

    public String getTag(String key) {
        return this.tags.get(key);
    }
}
