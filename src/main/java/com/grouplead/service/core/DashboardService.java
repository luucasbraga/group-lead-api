package com.grouplead.service.core;

import com.grouplead.domain.enums.PeriodType;
import com.grouplead.domain.enums.SprintStatus;
import com.grouplead.domain.enums.TicketStatus;
import com.grouplead.domain.vo.DateRange;
import com.grouplead.dto.response.*;
import com.grouplead.repository.AlertRepository;
import com.grouplead.repository.SprintRepository;
import com.grouplead.repository.TicketRepository;
import com.grouplead.service.collector.JiraCollectorService;
import com.grouplead.service.processor.DoraMetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final SprintRepository sprintRepository;
    private final TicketRepository ticketRepository;
    private final AlertRepository alertRepository;
    private final JiraCollectorService jiraCollectorService;
    private final DoraMetricsService doraMetricsService;

    @Cacheable(value = "dashboard", key = "#range.start().toString()")
    public DashboardSummaryResponse getSummary(DateRange range) {
        // Get completed tickets
        var completedTickets = ticketRepository.findCompletedSince(range.start());
        int ticketsCompleted = completedTickets.size();
        int storyPointsCompleted = completedTickets.stream()
                .filter(t -> t.getStoryPoints() != null)
                .mapToInt(t -> t.getStoryPoints())
                .sum();

        // Get current sprint (if any active)
        var activeSprints = sprintRepository.findActiveSprints();
        SprintProgressResponse currentSprint = null;
        if (!activeSprints.isEmpty()) {
            var sprint = activeSprints.get(0);
            var metrics = jiraCollectorService.calculateSprintMetrics(sprint.getExternalId());
            currentSprint = new SprintProgressResponse(
                    sprint.getId(),
                    sprint.getExternalId(),
                    sprint.getName(),
                    sprint.getGoal(),
                    sprint.getStartDate(),
                    sprint.getEndDate(),
                    sprint.getDaysRemaining(),
                    sprint.getStatus(),
                    new SprintProgressResponse.ProgressDetails(
                            metrics.totalTickets(),
                            metrics.completedTickets(),
                            metrics.inProgressTickets(),
                            metrics.todoTickets(),
                            metrics.blockedTickets(),
                            metrics.totalStoryPoints(),
                            metrics.completedStoryPoints(),
                            metrics.getProgress()
                    )
            );
        }

        // Get recent alerts
        var recentAlerts = alertRepository.findRecentAlerts(LocalDateTime.now().minusDays(7));
        var alertResponses = recentAlerts.stream()
                .limit(5)
                .map(AlertResponse::from)
                .toList();

        return new DashboardSummaryResponse(
                ticketsCompleted,
                0.0, // Change calculation would need previous period
                storyPointsCompleted,
                0.0,
                0, // Deployments - would come from deployment repo
                0.0,
                99.9, // Uptime placeholder
                0.0,
                currentSprint,
                List.of(), // Velocity history
                storyPointsCompleted,
                null, // Infrastructure metrics
                alertResponses,
                List.of() // AI insights
        );
    }

    public TeamDashboardResponse getTeamDashboard(Long teamId, PeriodType period) {
        var activeSprint = sprintRepository.findActiveSprintByTeamId(teamId).orElse(null);

        SprintProgressResponse sprintProgress = null;
        if (activeSprint != null) {
            var metrics = jiraCollectorService.calculateSprintMetrics(activeSprint.getExternalId());
            sprintProgress = SprintProgressResponse.from(activeSprint,
                    new SprintProgressResponse.ProgressDetails(
                            metrics.totalTickets(),
                            metrics.completedTickets(),
                            metrics.inProgressTickets(),
                            metrics.todoTickets(),
                            metrics.blockedTickets(),
                            metrics.totalStoryPoints(),
                            metrics.completedStoryPoints(),
                            metrics.getProgress()
                    ));
        }

        return new TeamDashboardResponse(
                teamId,
                "Team", // Would come from team entity
                sprintProgress,
                List.of(),
                0,
                0,
                List.of(),
                new TeamDashboardResponse.TeamMetrics(0, 0, 0.0, 0, 0, 0.0)
        );
    }

    public InfrastructureMetricsResponse getInfrastructureMetrics(PeriodType period) {
        // Placeholder - would integrate with CloudWatch
        return new InfrastructureMetricsResponse(
                List.of(
                        new InfrastructureMetricsResponse.ServiceMetrics(
                                "api-gateway", "healthy", 89.0, 0.02, 45.0, 62.0, 30.0, 10000
                        ),
                        new InfrastructureMetricsResponse.ServiceMetrics(
                                "backend-api", "healthy", 120.0, 0.05, 55.0, 70.0, 40.0, 8000
                        )
                ),
                99.95,
                120.0,
                0.05,
                (int) alertRepository.countActiveAlerts()
        );
    }

    private DateRange getDateRangeForPeriod(PeriodType period) {
        return switch (period) {
            case DAILY -> DateRange.lastDays(1);
            case WEEKLY -> DateRange.lastDays(7);
            case BIWEEKLY -> DateRange.lastDays(14);
            case MONTHLY -> DateRange.lastDays(30);
            case QUARTERLY -> DateRange.lastDays(90);
            case YEARLY -> DateRange.lastDays(365);
            default -> DateRange.lastDays(7);
        };
    }
}
