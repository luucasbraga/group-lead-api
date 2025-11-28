package com.grouplead.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record AnomalyResponse(
        boolean anomalyDetected,
        String service,
        String metric,
        String currentValue,
        String baselineValue,
        String deviation,
        String analysis,
        List<String> suggestedActions,
        LocalDateTime detectedAt
) {}
