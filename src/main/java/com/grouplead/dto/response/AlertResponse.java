package com.grouplead.dto.response;

import com.grouplead.domain.entity.Alert;
import com.grouplead.domain.enums.AlertSeverity;
import com.grouplead.domain.enums.AlertType;

import java.time.LocalDateTime;

public record AlertResponse(
        Long id,
        AlertType type,
        AlertSeverity severity,
        String message,
        String metricName,
        Double metricValue,
        Double thresholdValue,
        boolean acknowledged,
        String acknowledgedBy,
        LocalDateTime acknowledgedAt,
        LocalDateTime createdAt,
        LocalDateTime resolvedAt,
        boolean active
) {
    public static AlertResponse from(Alert alert) {
        return new AlertResponse(
                alert.getId(),
                alert.getType(),
                alert.getSeverity(),
                alert.getMessage(),
                alert.getMetricName(),
                alert.getMetricValue(),
                alert.getThresholdValue(),
                alert.getAcknowledged(),
                alert.getAcknowledgedBy() != null ? alert.getAcknowledgedBy().getFullName() : null,
                alert.getAcknowledgedAt(),
                alert.getCreatedAt(),
                alert.getResolvedAt(),
                alert.isActive()
        );
    }
}
