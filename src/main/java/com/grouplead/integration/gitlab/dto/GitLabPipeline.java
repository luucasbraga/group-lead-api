package com.grouplead.integration.gitlab.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitLabPipeline {
    private Long id;

    @JsonProperty("project_id")
    private Long projectId;

    private String sha;
    private String ref;
    private String status;
    private String source;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;

    @JsonProperty("web_url")
    private String webUrl;
}
