package com.grouplead.integration.jira;

import com.grouplead.config.IntegrationProperties;
import com.grouplead.integration.jira.dto.JiraIssue;
import com.grouplead.integration.jira.dto.JiraSearchResponse;
import com.grouplead.integration.jira.dto.JiraSprint;
import com.grouplead.integration.jira.dto.JiraSprintResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JiraClient {

    private final WebClient webClient;
    private final IntegrationProperties properties;

    private static final DateTimeFormatter JIRA_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public List<JiraIssue> getUpdatedIssues(LocalDateTime since) {
        log.debug("Fetching JIRA issues updated since {}", since);

        String jql = String.format(
                "project in (%s) AND updated >= '%s' ORDER BY updated DESC",
                properties.getJira().getProjectKeys(),
                since.format(JIRA_DATE_FORMAT)
        );

        try {
            JiraSearchResponse response = webClient.get()
                    .uri(properties.getJira().getBaseUrl() + "/rest/api/3/search?jql={jql}&maxResults=100&fields=summary,status,assignee,created,updated,customfield_10016,priority,issuetype,labels",
                            jql)
                    .header("Authorization", getAuthHeader())
                    .retrieve()
                    .bodyToMono(JiraSearchResponse.class)
                    .block();

            return response != null ? response.getIssues() : List.of();
        } catch (Exception e) {
            log.error("Error fetching JIRA issues", e);
            return List.of();
        }
    }

    public List<JiraSprint> getActiveSprints() {
        log.debug("Fetching active JIRA sprints");

        try {
            JiraSprintResponse response = webClient.get()
                    .uri(properties.getJira().getBaseUrl() + "/rest/agile/1.0/board/{boardId}/sprint?state=active",
                            properties.getJira().getBoardId())
                    .header("Authorization", getAuthHeader())
                    .retrieve()
                    .bodyToMono(JiraSprintResponse.class)
                    .block();

            return response != null ? response.getValues() : List.of();
        } catch (Exception e) {
            log.error("Error fetching JIRA sprints", e);
            return List.of();
        }
    }

    public List<JiraSprint> getAllSprints() {
        log.debug("Fetching all JIRA sprints");

        try {
            JiraSprintResponse response = webClient.get()
                    .uri(properties.getJira().getBaseUrl() + "/rest/agile/1.0/board/{boardId}/sprint",
                            properties.getJira().getBoardId())
                    .header("Authorization", getAuthHeader())
                    .retrieve()
                    .bodyToMono(JiraSprintResponse.class)
                    .block();

            return response != null ? response.getValues() : List.of();
        } catch (Exception e) {
            log.error("Error fetching JIRA sprints", e);
            return List.of();
        }
    }

    public List<JiraIssue> getSprintIssues(String sprintId) {
        log.debug("Fetching issues for sprint {}", sprintId);

        String jql = String.format("sprint = %s ORDER BY rank", sprintId);

        try {
            JiraSearchResponse response = webClient.get()
                    .uri(properties.getJira().getBaseUrl() + "/rest/api/3/search?jql={jql}&maxResults=100",
                            jql)
                    .header("Authorization", getAuthHeader())
                    .retrieve()
                    .bodyToMono(JiraSearchResponse.class)
                    .block();

            return response != null ? response.getIssues() : List.of();
        } catch (Exception e) {
            log.error("Error fetching sprint issues", e);
            return List.of();
        }
    }

    private String getAuthHeader() {
        String credentials = properties.getJira().getEmail() + ":" + properties.getJira().getApiToken();
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
    }
}
