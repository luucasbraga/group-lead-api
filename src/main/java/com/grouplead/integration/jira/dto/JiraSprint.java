package com.grouplead.integration.jira.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class JiraSprint {
    private Long id;
    private String self;
    private String state;
    private String name;
    private String startDate;
    private String endDate;
    private String completeDate;
    private Long originBoardId;
    private String goal;
}
