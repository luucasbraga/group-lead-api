package com.grouplead.dto.response;

import java.util.List;

public record DeveloperInsightsResponse(
        Long developerId,
        String developerName,
        String period,
        DeveloperMetricsResponse.ProductivityMetrics metrics,
        List<String> insights,
        List<String> growthOpportunities,
        List<String> talkingPoints,
        BurnoutIndicators burnoutIndicators
) {
    public record BurnoutIndicators(
            Double riskScore,
            int afterHoursCommitsPercent,
            int consecutiveHighLoadDays,
            Double workloadComparedToTeam
    ) {}
}
