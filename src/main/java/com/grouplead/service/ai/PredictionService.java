package com.grouplead.service.ai;

import com.grouplead.domain.vo.PredictionResult;
import com.grouplead.integration.ai.AIResponseParser;
import com.grouplead.integration.ai.ClaudeClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PredictionService {

    private final ClaudeClient claudeClient;
    private final PromptBuilderService promptBuilder;
    private final AIResponseParser responseParser;

    @Cacheable(value = "predictions", key = "#sprintId")
    public PredictionResult predictSprintCompletion(String sprintId) {
        log.info("Generating sprint completion prediction for {}", sprintId);

        String prompt = promptBuilder.buildPredictionPrompt(sprintId);
        var response = claudeClient.complete(prompt);

        return responseParser.parsePredictionResponse(response.getTextContent(), sprintId);
    }
}
