package com.grouplead.integration.gitlab.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitLabCommit {
    private String id;

    @JsonProperty("short_id")
    private String shortId;

    private String title;
    private String message;

    @JsonProperty("author_name")
    private String authorName;

    @JsonProperty("author_email")
    private String authorEmail;

    @JsonProperty("authored_date")
    private String authoredDate;

    @JsonProperty("committer_name")
    private String committerName;

    @JsonProperty("committer_email")
    private String committerEmail;

    @JsonProperty("committed_date")
    private String committedDate;

    @JsonProperty("web_url")
    private String webUrl;

    private Stats stats;

    @JsonProperty("parent_ids")
    private List<String> parentIds;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Stats {
        private Integer additions;
        private Integer deletions;
        private Integer total;
    }
}
