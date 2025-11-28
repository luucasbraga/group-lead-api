package com.grouplead.integration.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaudeMessage {
    private String role;
    private String content;

    public static ClaudeMessage user(String content) {
        return new ClaudeMessage("user", content);
    }

    public static ClaudeMessage assistant(String content) {
        return new ClaudeMessage("assistant", content);
    }
}
