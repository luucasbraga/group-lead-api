package com.grouplead.integration.jira.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class JiraSearchResponse {
    private String expand;
    private int startAt;
    private int maxResults;
    private int total;
    private List<JiraIssue> issues;
}
