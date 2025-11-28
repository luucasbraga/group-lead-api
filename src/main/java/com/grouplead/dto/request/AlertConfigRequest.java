package com.grouplead.dto.request;

import com.grouplead.domain.enums.AlertSeverity;
import com.grouplead.domain.enums.AlertType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AlertConfigRequest(
        @NotNull(message = "Alert type is required")
        AlertType type,

        @NotNull(message = "Threshold value is required")
        @Positive(message = "Threshold value must be positive")
        Double thresholdValue,

        @NotNull(message = "Severity is required")
        AlertSeverity severity,

        String metricName,

        boolean enabled
) {}
