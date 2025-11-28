package com.grouplead.dto.response;

import java.util.List;

public record DashboardSummaryResponse(
        int ticketsCompleted,
        Double ticketsCompletedChange,
        int storyPointsCompleted,
        Double storyPointsChange,
        int deployments,
        Double deploymentsChange,
        Double uptime,
        Double uptimeChange,
        SprintProgressResponse currentSprint,
        List<VelocityDataPoint> velocityHistory,
        int currentVelocity,
        InfrastructureMetricsResponse infrastructure,
        List<AlertResponse> recentAlerts,
        List<AIInsightSummary> aiInsights
) {
    public record VelocityDataPoint(
            String sprint,
            int planned,
            int completed
    ) {}

    public record AIInsightSummary(
            String type,
            String title,
            String summary,
            String severity
    ) {}
}
