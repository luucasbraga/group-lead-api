package com.grouplead.integration.ai;

import com.grouplead.config.AIProperties;
import com.grouplead.integration.ai.dto.ClaudeMessage;
import com.grouplead.integration.ai.dto.ClaudeRequest;
import com.grouplead.integration.ai.dto.ClaudeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClaudeClient {

    private final WebClient webClient;
    private final AIProperties aiProperties;

    private static final String CLAUDE_API_URL = "https://api.anthropic.com/v1/messages";
    private static final String ANTHROPIC_VERSION = "2024-01-01";

    public ClaudeResponse complete(String prompt) {
        log.debug("Sending prompt to Claude API");

        ClaudeRequest request = ClaudeRequest.builder()
                .model(aiProperties.getClaude().getModel())
                .maxTokens(aiProperties.getClaude().getMaxTokens())
                .messages(List.of(
                        ClaudeMessage.builder()
                                .role("user")
                                .content(prompt)
                                .build()
                ))
                .build();

        return sendRequest(request);
    }

    public ClaudeResponse chat(List<ClaudeMessage> messages) {
        log.debug("Sending chat messages to Claude API");

        ClaudeRequest request = ClaudeRequest.builder()
                .model(aiProperties.getClaude().getModel())
                .maxTokens(aiProperties.getClaude().getMaxTokens())
                .messages(messages)
                .build();

        return sendRequest(request);
    }

    public ClaudeResponse completeWithSystem(String systemPrompt, String userMessage) {
        log.debug("Sending prompt with system message to Claude API");

        ClaudeRequest request = ClaudeRequest.builder()
                .model(aiProperties.getClaude().getModel())
                .maxTokens(aiProperties.getClaude().getMaxTokens())
                .system(systemPrompt)
                .messages(List.of(
                        ClaudeMessage.builder()
                                .role("user")
                                .content(userMessage)
                                .build()
                ))
                .build();

        return sendRequest(request);
    }

    private ClaudeResponse sendRequest(ClaudeRequest request) {
        try {
            return webClient.post()
                    .uri(CLAUDE_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("x-api-key", aiProperties.getClaude().getApiKey())
                    .header("anthropic-version", ANTHROPIC_VERSION)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(ClaudeResponse.class)
                    .block();
        } catch (Exception e) {
            log.error("Error calling Claude API", e);
            throw new RuntimeException("Failed to call Claude API: " + e.getMessage(), e);
        }
    }
}
