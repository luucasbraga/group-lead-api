package com.grouplead.domain.vo;

public record SprintMetrics(
        String sprintId,
        String sprintName,
        int totalTickets,
        int completedTickets,
        int inProgressTickets,
        int todoTickets,
        int blockedTickets,
        int totalStoryPoints,
        int completedStoryPoints,
        Double averageCycleTimeHours,
        int daysRemaining,
        Double completionRate,
        Double velocityTrend
) {
    public static SprintMetrics empty(String sprintId) {
        return new SprintMetrics(
                sprintId, "", 0, 0, 0, 0, 0, 0, 0, 0.0, 0, 0.0, 0.0
        );
    }

    public Double getProgress() {
        if (totalStoryPoints == 0) return 0.0;
        return (double) completedStoryPoints / totalStoryPoints * 100;
    }

    public boolean isOnTrack() {
        return completionRate != null && completionRate >= 80;
    }

    public boolean hasBlockers() {
        return blockedTickets > 0;
    }
}
