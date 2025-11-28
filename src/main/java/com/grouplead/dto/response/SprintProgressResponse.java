package com.grouplead.dto.response;

import com.grouplead.domain.enums.SprintStatus;

import java.time.LocalDate;

public record SprintProgressResponse(
        Long id,
        String externalId,
        String name,
        String goal,
        LocalDate startDate,
        LocalDate endDate,
        int daysRemaining,
        SprintStatus status,
        ProgressDetails progress
) {
    public record ProgressDetails(
            int totalTickets,
            int completedTickets,
            int inProgressTickets,
            int todoTickets,
            int blockedTickets,
            int totalStoryPoints,
            int completedStoryPoints,
            Double completionPercentage
    ) {}

    public static SprintProgressResponse from(com.grouplead.domain.entity.Sprint sprint, ProgressDetails progress) {
        return new SprintProgressResponse(
                sprint.getId(),
                sprint.getExternalId(),
                sprint.getName(),
                sprint.getGoal(),
                sprint.getStartDate(),
                sprint.getEndDate(),
                sprint.getDaysRemaining(),
                sprint.getStatus(),
                progress
        );
    }
}
