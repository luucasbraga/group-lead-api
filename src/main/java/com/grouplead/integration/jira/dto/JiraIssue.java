package com.grouplead.integration.jira.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class JiraIssue {
    private String id;
    private String key;
    private String self;
    private JiraFields fields;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class JiraFields {
        private String summary;
        private String description;
        private JiraStatus status;
        private JiraAssignee assignee;
        private JiraPriority priority;
        private JiraIssueType issuetype;
        private String created;
        private String updated;
        private Double customfield_10016; // Story points
        private List<String> labels;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class JiraStatus {
        private String name;
        private String id;
        private JiraStatusCategory statusCategory;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class JiraStatusCategory {
        private String name;
        private String key;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class JiraAssignee {
        private String accountId;
        private String displayName;
        private String emailAddress;
        private Map<String, String> avatarUrls;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class JiraPriority {
        private String name;
        private String id;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class JiraIssueType {
        private String name;
        private String id;
    }
}
