package com.grouplead.domain.vo;

import com.grouplead.domain.enums.SprintStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SprintMetrics {
    private Long sprintId;
    private String sprintName;
    private SprintStatus status;

    // Ticket counts
    private int totalTickets;
    private int completedTickets;
    private int inProgressTickets;
    private int todoTickets;
    private int blockedTickets;

    // Story points
    private int totalPoints;
    private int completedPoints;
    private int committedPoints;

    // Rates and percentages
    private double completionRate;
    private double progressPercentage;
    private double velocity;

    // Time related
    private int daysRemaining;
    private int totalDays;
    private LocalDate startDate;
    private LocalDate endDate;

    // Computed methods
    public double getProgress() {
        if (totalPoints == 0) return 0.0;
        return (double) completedPoints / totalPoints * 100;
    }

    public boolean isOnTrack() {
        return completionRate >= 80;
    }

    public boolean hasBlockers() {
        return blockedTickets > 0;
    }

    public double getBurndownRate() {
        if (totalDays == 0) return 0.0;
        int daysElapsed = totalDays - daysRemaining;
        if (daysElapsed == 0) return 0.0;
        return (double) completedPoints / daysElapsed;
    }

    public double getExpectedProgress() {
        if (totalDays == 0) return 0.0;
        int daysElapsed = totalDays - daysRemaining;
        return (double) daysElapsed / totalDays * 100;
    }

    public boolean isAheadOfSchedule() {
        return progressPercentage > getExpectedProgress();
    }
}
