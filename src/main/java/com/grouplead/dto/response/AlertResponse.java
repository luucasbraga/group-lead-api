package com.grouplead.dto.response;

import com.grouplead.domain.enums.AlertSeverity;
import com.grouplead.domain.enums.AlertType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertResponse {
    private Long id;
    private Long teamId;
    private String teamName;
    private AlertType type;
    private AlertSeverity severity;
    private String title;
    private String message;
    private String source;
    private Map<String, String> metadata;
    private boolean resolved;
    private boolean acknowledged;
    private String resolution;
    private LocalDateTime createdAt;
    private LocalDateTime acknowledgedAt;
    private LocalDateTime resolvedAt;
}
