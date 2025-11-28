package com.grouplead.integration.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClaudeResponse {
    private String id;
    private String type;
    private String role;
    private List<Content> content;
    private String model;

    @JsonProperty("stop_reason")
    private String stopReason;

    @JsonProperty("stop_sequence")
    private String stopSequence;

    private Usage usage;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Content {
        private String type;
        private String text;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Usage {
        @JsonProperty("input_tokens")
        private int inputTokens;

        @JsonProperty("output_tokens")
        private int outputTokens;
    }

    public String getTextContent() {
        if (content == null || content.isEmpty()) {
            return "";
        }
        return content.stream()
                .filter(c -> "text".equals(c.getType()))
                .map(Content::getText)
                .findFirst()
                .orElse("");
    }
}
