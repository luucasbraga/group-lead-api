package com.grouplead.integration.ai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClaudeRequest {
    private String model;

    @JsonProperty("max_tokens")
    private int maxTokens;

    private String system;

    private List<ClaudeMessage> messages;

    private Double temperature;

    @JsonProperty("top_p")
    private Double topP;

    @JsonProperty("top_k")
    private Integer topK;

    @JsonProperty("stop_sequences")
    private List<String> stopSequences;
}
