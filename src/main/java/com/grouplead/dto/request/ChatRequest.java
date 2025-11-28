package com.grouplead.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ChatRequest(
        @NotBlank(message = "Message is required")
        @Size(max = 4000, message = "Message must be less than 4000 characters")
        String message,

        List<ChatMessage> history,

        String sessionId
) {
    public record ChatMessage(
            String role,
            String content
    ) {}
}
