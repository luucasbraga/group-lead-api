package com.grouplead.dto.response;

import java.util.List;

public record TeamDashboardResponse(
        Long teamId,
        String teamName,
        SprintProgressResponse currentSprint,
        List<DashboardSummaryResponse.VelocityDataPoint> velocityHistory,
        int currentVelocity,
        int averageVelocity,
        List<DeveloperSummary> developers,
        TeamMetrics metrics
) {
    public record DeveloperSummary(
            Long id,
            String name,
            String avatarUrl,
            int ticketsInProgress,
            int ticketsCompleted,
            int storyPointsCompleted
    ) {}

    public record TeamMetrics(
            int totalTicketsCompleted,
            int totalStoryPoints,
            Double averageCycleTimeHours,
            int blockedTickets,
            int openPullRequests,
            Double codeCoverage
    ) {}
}
