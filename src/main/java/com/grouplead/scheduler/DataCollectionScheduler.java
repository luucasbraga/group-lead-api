package com.grouplead.scheduler;

import com.grouplead.service.collector.GitLabCollectorService;
import com.grouplead.service.collector.JiraCollectorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class DataCollectionScheduler {

    private final JiraCollectorService jiraCollectorService;
    private final GitLabCollectorService gitLabCollectorService;

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
}
