package com.grouplead.integration.gitlab;

import com.grouplead.config.IntegrationProperties;
import com.grouplead.integration.gitlab.dto.GitLabCommit;
import com.grouplead.integration.gitlab.dto.GitLabMergeRequest;
import com.grouplead.integration.gitlab.dto.GitLabPipeline;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class GitLabClient {

    private final WebClient webClient;
    private final IntegrationProperties properties;

    public List<GitLabCommit> getCommits(String projectId, LocalDateTime since) {
        log.debug("Fetching GitLab commits for project {} since {}", projectId, since);

        try {
            return webClient.get()
                    .uri(properties.getGitlab().getBaseUrl() +
                                    "/api/v4/projects/{projectId}/repository/commits?since={since}&per_page=100",
                            projectId, since.format(DateTimeFormatter.ISO_DATE_TIME))
                    .header("PRIVATE-TOKEN", properties.getGitlab().getPrivateToken())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<GitLabCommit>>() {})
                    .block();
        } catch (Exception e) {
            log.error("Error fetching GitLab commits", e);
            return List.of();
        }
    }

    public List<GitLabMergeRequest> getMergeRequests(String projectId, String state) {
        log.debug("Fetching GitLab merge requests for project {} with state {}", projectId, state);

        try {
            return webClient.get()
                    .uri(properties.getGitlab().getBaseUrl() +
                                    "/api/v4/projects/{projectId}/merge_requests?state={state}&per_page=100",
                            projectId, state)
                    .header("PRIVATE-TOKEN", properties.getGitlab().getPrivateToken())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<GitLabMergeRequest>>() {})
                    .block();
        } catch (Exception e) {
            log.error("Error fetching GitLab merge requests", e);
            return List.of();
        }
    }

    public List<GitLabMergeRequest> getMergedMergeRequests(String projectId, LocalDateTime since) {
        log.debug("Fetching merged GitLab merge requests for project {} since {}", projectId, since);

        try {
            return webClient.get()
                    .uri(properties.getGitlab().getBaseUrl() +
                                    "/api/v4/projects/{projectId}/merge_requests?state=merged&updated_after={since}&per_page=100",
                            projectId, since.format(DateTimeFormatter.ISO_DATE_TIME))
                    .header("PRIVATE-TOKEN", properties.getGitlab().getPrivateToken())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<GitLabMergeRequest>>() {})
                    .block();
        } catch (Exception e) {
            log.error("Error fetching merged GitLab merge requests", e);
            return List.of();
        }
    }

    public GitLabCommit getCommitDetails(String projectId, String sha) {
        log.debug("Fetching GitLab commit details for {} in project {}", sha, projectId);

        try {
            return webClient.get()
                    .uri(properties.getGitlab().getBaseUrl() +
                                    "/api/v4/projects/{projectId}/repository/commits/{sha}",
                            projectId, sha)
                    .header("PRIVATE-TOKEN", properties.getGitlab().getPrivateToken())
                    .retrieve()
                    .bodyToMono(GitLabCommit.class)
                    .block();
        } catch (Exception e) {
            log.error("Error fetching GitLab commit details", e);
            return null;
        }
    }

    public List<GitLabPipeline> getPipelines(String projectId, LocalDateTime since) {
        log.debug("Fetching GitLab pipelines for project {} since {}", projectId, since);

        try {
            return webClient.get()
                    .uri(properties.getGitlab().getBaseUrl() +
                                    "/api/v4/projects/{projectId}/pipelines?updated_after={since}&per_page=100",
                            projectId, since.format(DateTimeFormatter.ISO_DATE_TIME))
                    .header("PRIVATE-TOKEN", properties.getGitlab().getPrivateToken())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<GitLabPipeline>>() {})
                    .block();
        } catch (Exception e) {
            log.error("Error fetching GitLab pipelines", e);
            return List.of();
        }
    }

    public List<String> getProjectIds() {
        String projectIds = properties.getGitlab().getProjectIds();
        if (projectIds == null || projectIds.isEmpty()) {
            return List.of();
        }
        return Arrays.asList(projectIds.split(","));
    }
}
