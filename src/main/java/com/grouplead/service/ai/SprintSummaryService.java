package com.grouplead.service.ai;

import com.grouplead.domain.entity.AIInsight;
import com.grouplead.domain.enums.InsightType;
import com.grouplead.domain.enums.TargetType;
import com.grouplead.dto.response.SprintSummaryResponse;
import com.grouplead.integration.ai.ClaudeClient;
import com.grouplead.repository.AIInsightRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class SprintSummaryService {

    private final ClaudeClient claudeClient;
    private final PromptBuilderService promptBuilder;
    private final AIInsightRepository insightRepository;

    public SprintSummaryResponse getSummary(String sprintId, boolean regenerate) {
        log.info("Getting sprint summary for {} (regenerate: {})", sprintId, regenerate);

        if (!regenerate) {
            var cached = insightRepository.findLatestByTypeAndTargetId(InsightType.SPRINT_SUMMARY, sprintId);
            if (cached.isPresent() && cached.get().isValid()) {
                AIInsight insight = cached.get();
                return new SprintSummaryResponse(
                        sprintId,
                        "Sprint",
                        insight.getContent(),
                        insight.getGeneratedAt(),
                        insight.getConfidenceScore()
                );
            }
        }

        // Generate new summary
        String prompt = promptBuilder.buildSprintSummaryPrompt(sprintId);
        var response = claudeClient.complete(prompt);

        // Save insight
        AIInsight insight = AIInsight.builder()
                .type(InsightType.SPRINT_SUMMARY)
                .targetId(sprintId)
                .targetType(TargetType.SPRINT)
                .content(response.getTextContent())
                .confidenceScore(0.85)
                .generatedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();

        insightRepository.save(insight);

        return new SprintSummaryResponse(
                sprintId,
                "Sprint",
                response.getTextContent(),
                LocalDateTime.now(),
                0.85
        );
    }
}
