package com.grouplead.dto.response;

import java.util.List;

public record DeveloperMetricsResponse(
        Long developerId,
        String developerName,
        String period,
        ProductivityMetrics productivity,
        QualityMetrics quality,
        CollaborationMetrics collaboration,
        List<String> insights,
        List<String> growthOpportunities
) {
    public record ProductivityMetrics(
            int ticketsCompleted,
            int storyPointsCompleted,
            int commits,
            int linesAdded,
            int linesDeleted,
            Double averageCycleTimeHours
    ) {}

    public record QualityMetrics(
            int bugsIntroduced,
            int bugsFixed,
            Double testCoverage,
            int codeReviewsReceived,
            int codeReviewComments
    ) {}

    public record CollaborationMetrics(
            int prReviewsDone,
            Double averagePrReviewTimeHours,
            int pairProgrammingSessions,
            int mentoringSessions
    ) {}
}
