package com.grouplead.integration.gitlab.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitLabMergeRequest {
    private Long id;
    private Long iid;

    @JsonProperty("project_id")
    private Long projectId;

    private String title;
    private String description;
    private String state;

    @JsonProperty("source_branch")
    private String sourceBranch;

    @JsonProperty("target_branch")
    private String targetBranch;

    private GitLabAuthor author;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;

    @JsonProperty("merged_at")
    private String mergedAt;

    @JsonProperty("closed_at")
    private String closedAt;

    @JsonProperty("web_url")
    private String webUrl;

    @JsonProperty("user_notes_count")
    private Integer userNotesCount;

    @JsonProperty("changes_count")
    private String changesCount;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GitLabAuthor {
        private Long id;
        private String username;
        private String name;
        private String email;

        @JsonProperty("avatar_url")
        private String avatarUrl;
    }
}
