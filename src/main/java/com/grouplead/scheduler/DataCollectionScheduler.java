package com.grouplead.scheduler;

import com.grouplead.domain.entity.Team;
import com.grouplead.repository.TeamRepository;
import com.grouplead.service.collector.CloudWatchCollectorService;
import com.grouplead.service.collector.GitLabCollectorService;
import com.grouplead.service.collector.JiraCollectorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class DataCollectionScheduler {

    private final JiraCollectorService jiraCollectorService;
    private final GitLabCollectorService gitLabCollectorService;
    private final CloudWatchCollectorService cloudWatchCollectorService;
    private final TeamRepository teamRepository;

    @Scheduled(cron = "${scheduler.data-collection.jira-cron}")
    public void collectJiraData() {
        log.info("Starting scheduled JIRA data collection");
        try {
            LocalDateTime since = LocalDateTime.now().minusMinutes(20);
            var result = jiraCollectorService.collectTickets(since);
            log.info("JIRA collection completed: {} tickets collected", result.count());

            int sprints = jiraCollectorService.collectSprints();
            log.info("JIRA sprint collection completed: {} sprints collected", sprints);
        } catch (Exception e) {
            log.error("Error during scheduled JIRA collection", e);
        }
    }

    @Scheduled(cron = "${scheduler.data-collection.gitlab-cron}")
    public void collectGitLabData() {
        log.info("Starting scheduled GitLab data collection");
        try {
            LocalDateTime since = LocalDateTime.now().minusMinutes(15);

            var commitResult = gitLabCollectorService.collectCommits(since);
            log.info("GitLab commit collection completed: {} commits collected", commitResult.count());

            var mrResult = gitLabCollectorService.collectMergeRequests(since);
            log.info("GitLab MR collection completed: {} merge requests collected", mrResult.count());
        } catch (Exception e) {
            log.error("Error during scheduled GitLab collection", e);
        }
    }

    @Scheduled(cron = "${scheduler.data-collection.cloudwatch-cron}")
    public void collectCloudWatchData() {
        log.info("Starting scheduled CloudWatch data collection");
        try {
            LocalDateTime since = LocalDateTime.now().minusMinutes(10);
            List<Team> teams = teamRepository.findAll();

            for (Team team : teams) {
                if (team.getAwsResources() != null && !team.getAwsResources().isEmpty()) {
                    collectTeamAwsMetrics(team, since);
                }
            }

            log.info("CloudWatch collection completed for {} teams", teams.size());
        } catch (Exception e) {
            log.error("Error during scheduled CloudWatch collection", e);
        }
    }

    @Scheduled(cron = "${scheduler.data-collection.cost-cron:0 0 6 * * *}")
    public void collectCostData() {
        log.info("Starting scheduled AWS Cost data collection");
        try {
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(1);

            List<Team> teams = teamRepository.findAll();

            for (Team team : teams) {
                try {
                    var result = cloudWatchCollectorService.collectCostMetrics(
                            team.getId(), startDate, endDate);
                    log.info("Cost metrics collected for team {}: {} metrics",
                            team.getName(), result.collected());
                } catch (Exception e) {
                    log.error("Error collecting cost metrics for team {}: {}",
                            team.getName(), e.getMessage());
                }
            }

            log.info("AWS Cost collection completed");
        } catch (Exception e) {
            log.error("Error during scheduled AWS Cost collection", e);
        }
    }

    private void collectTeamAwsMetrics(Team team, LocalDateTime since) {
        var resources = team.getAwsResources();

        // Collect EC2 metrics
        if (resources.containsKey("ec2_instances")) {
            for (String instanceId : resources.get("ec2_instances").split(",")) {
                try {
                    var result = cloudWatchCollectorService.collectEC2Metrics(
                            instanceId.trim(), team.getId(), since);
                    log.debug("EC2 metrics collected for {}: {} metrics", instanceId, result.collected());
                } catch (Exception e) {
                    log.error("Error collecting EC2 metrics for {}: {}", instanceId, e.getMessage());
                }
            }
        }

        // Collect RDS metrics
        if (resources.containsKey("rds_instances")) {
            for (String dbInstanceId : resources.get("rds_instances").split(",")) {
                try {
                    var result = cloudWatchCollectorService.collectRDSMetrics(
                            dbInstanceId.trim(), team.getId(), since);
                    log.debug("RDS metrics collected for {}: {} metrics", dbInstanceId, result.collected());
                } catch (Exception e) {
                    log.error("Error collecting RDS metrics for {}: {}", dbInstanceId, e.getMessage());
                }
            }
        }

        // Collect ECS metrics
        if (resources.containsKey("ecs_services")) {
            for (String service : resources.get("ecs_services").split(",")) {
                String[] parts = service.trim().split("/");
                if (parts.length == 2) {
                    try {
                        var result = cloudWatchCollectorService.collectECSMetrics(
                                parts[0], parts[1], team.getId(), since);
                        log.debug("ECS metrics collected for {}: {} metrics", service, result.collected());
                    } catch (Exception e) {
                        log.error("Error collecting ECS metrics for {}: {}", service, e.getMessage());
                    }
                }
            }
        }

        // Collect Lambda metrics
        if (resources.containsKey("lambda_functions")) {
            for (String functionName : resources.get("lambda_functions").split(",")) {
                try {
                    var result = cloudWatchCollectorService.collectLambdaMetrics(
                            functionName.trim(), team.getId(), since);
                    log.debug("Lambda metrics collected for {}: {} metrics", functionName, result.collected());
                } catch (Exception e) {
                    log.error("Error collecting Lambda metrics for {}: {}", functionName, e.getMessage());
                }
            }
        }
    }
}
