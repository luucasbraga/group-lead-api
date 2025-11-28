package com.grouplead.scheduler;

import com.grouplead.domain.enums.InsightType;
import com.grouplead.domain.enums.PeriodType;
import com.grouplead.domain.enums.SprintStatus;
import com.grouplead.repository.SprintRepository;
import com.grouplead.service.ai.AIOrchestrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class AIInsightScheduler {

    private final AIOrchestrationService aiOrchestrationService;
    private final SprintRepository sprintRepository;

    @Scheduled(cron = "${scheduler.ai-insights.sprint-summary-cron}")
    public void generateSprintSummaries() {
        log.info("Starting scheduled sprint summary generation");
        try {
            var activeSprints = sprintRepository.findByStatus(SprintStatus.ACTIVE);

            for (var sprint : activeSprints) {
                try {
                    aiOrchestrationService.generateInsight(
                            InsightType.SPRINT_SUMMARY,
                            sprint.getExternalId(),
                            PeriodType.WEEKLY
                    );
                    log.info("Generated summary for sprint: {}", sprint.getName());
                } catch (Exception e) {
                    log.error("Error generating summary for sprint: {}", sprint.getName(), e);
                }
            }
        } catch (Exception e) {
            log.error("Error during scheduled sprint summary generation", e);
        }
    }

    @Scheduled(cron = "${scheduler.ai-insights.anomaly-check-cron}")
    public void checkForAnomalies() {
        log.info("Starting scheduled anomaly detection");
        try {
            aiOrchestrationService.generateInsight(
                    InsightType.ANOMALY_DETECTION,
                    "infrastructure",
                    PeriodType.DAILY
            );
            log.info("Anomaly detection completed");
        } catch (Exception e) {
            log.error("Error during scheduled anomaly detection", e);
        }
    }
}
