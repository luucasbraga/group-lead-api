package com.grouplead.dto.response;

import java.util.List;

public record VelocityResponse(
        Long teamId,
        int sprintCount,
        Double averageVelocity,
        Double velocityTrend,
        List<SprintVelocity> sprints
) {
    public record SprintVelocity(
            String sprintId,
            String sprintName,
            int plannedStoryPoints,
            int completedStoryPoints,
            Double completionRate
    ) {}
}
