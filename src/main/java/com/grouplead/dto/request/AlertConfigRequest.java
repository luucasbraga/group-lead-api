package com.grouplead.dto.request;

import com.grouplead.domain.enums.AlertSeverity;
import com.grouplead.domain.enums.AlertType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertConfigRequest {

    private Long teamId;

    @NotNull(message = "Alert type is required")
    private AlertType type;

    @NotNull(message = "Severity is required")
    private AlertSeverity severity;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Message is required")
    private String message;

    private String source;

    private Map<String, String> metadata;

    // For threshold-based alerts
    private String metricName;
    private Double thresholdValue;
    private boolean enabled;
}
