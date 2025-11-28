package com.grouplead.dto.response;

import java.util.List;

public record ChatResponse(
        String message,
        List<String> suggestedActions,
        List<RelatedMetric> relatedMetrics,
        boolean shouldRefreshMetrics
) {
    public record RelatedMetric(
            String name,
            String value,
            String trend
    ) {}
}
