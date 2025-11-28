package com.grouplead.integration.jira.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class JiraSprintResponse {
    private int maxResults;
    private int startAt;
    private boolean isLast;
    private List<JiraSprint> values;
}
