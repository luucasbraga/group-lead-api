package com.grouplead.service.ai;

import com.grouplead.domain.entity.AIInsight;
import com.grouplead.domain.entity.ChatHistory;
import com.grouplead.domain.entity.User;
import com.grouplead.domain.enums.InsightType;
import com.grouplead.domain.enums.PeriodType;
import com.grouplead.domain.enums.TargetType;
import com.grouplead.dto.request.ChatRequest;
import com.grouplead.dto.response.AnomalyResponse;
import com.grouplead.dto.response.BurnoutRiskResponse;
import com.grouplead.dto.response.ChatResponse;
import com.grouplead.dto.response.DeveloperInsightsResponse;
import com.grouplead.integration.ai.AIResponseParser;
import com.grouplead.integration.ai.ClaudeClient;
import com.grouplead.integration.ai.dto.ClaudeMessage;
import com.grouplead.repository.AIInsightRepository;
import com.grouplead.repository.ChatHistoryRepository;
import com.grouplead.repository.UserRepository;
import com.grouplead.service.processor.MetricsProcessorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIOrchestrationService {

    private final ClaudeClient claudeClient;
    private final PromptBuilderService promptBuilder;
    private final MetricsProcessorService metricsProcessor;
    private final AIInsightRepository insightRepository;
    private final ChatHistoryRepository chatHistoryRepository;
    private final UserRepository userRepository;
    private final AIResponseParser responseParser;

    @Cacheable(value = "ai-insights", key = "#type + '-' + #targetId + '-' + #period")
    public AIInsight generateInsight(InsightType type, String targetId, PeriodType period) {
        log.info("Generating AI insight: type={}, target={}", type, targetId);

        String prompt = promptBuilder.buildPromptForInsight(type, targetId, period);
        var response = claudeClient.complete(prompt);

        AIInsight insight = AIInsight.builder()
                .type(type)
                .targetId(targetId)
                .targetType(getTargetType(type))
                .content(response.getTextContent())
                .confidenceScore(0.8)
                .generatedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(getExpirationHours(type)))
                .build();

        return insightRepository.save(insight);
    }

    @Transactional
    public ChatResponse chat(Long userId, ChatRequest request) {
        log.info("Processing chat request for user {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String sessionId = request.sessionId() != null ? request.sessionId() : UUID.randomUUID().toString();

        // Build context
        String systemPrompt = promptBuilder.buildChatSystemPrompt(user);

        // Get conversation history
        List<ClaudeMessage> messages = new ArrayList<>();
        if (request.history() != null) {
            messages.addAll(request.history().stream()
                    .map(m -> new ClaudeMessage(m.role(), m.content()))
                    .toList());
        }
        messages.add(ClaudeMessage.user(request.message()));

        // Call Claude with system prompt
        var response = claudeClient.completeWithSystem(systemPrompt,
                messages.stream()
                        .map(m -> m.getRole() + ": " + m.getContent())
                        .collect(Collectors.joining("\n")));

        String responseText = response.getTextContent();

        // Save chat history
        saveChatHistory(user, sessionId, "user", request.message());
        saveChatHistory(user, sessionId, "assistant", responseText);

        // Extract suggested actions
        List<String> suggestedActions = extractSuggestedActions(responseText);

        // Extract related metrics
        List<ChatResponse.RelatedMetric> relatedMetrics = extractRelatedMetrics(responseText);

        return new ChatResponse(
                responseText,
                suggestedActions,
                relatedMetrics,
                false
        );
    }

    public DeveloperInsightsResponse generateDeveloperInsights(Long developerId, PeriodType period) {
        log.info("Generating developer insights for developer {} for period {}", developerId, period);

        String prompt = promptBuilder.buildDeveloperAnalysisPrompt(developerId, period);
        var response = claudeClient.complete(prompt);

        // Parse the response and return structured insights
        // This would be enhanced with proper parsing
        return null; // Placeholder - would be implemented with full parsing logic
    }

    public List<AnomalyResponse> getRecentAnomalies(int hoursBack) {
        LocalDateTime since = LocalDateTime.now().minusHours(hoursBack);
        var insights = insightRepository.findValidByType(InsightType.ANOMALY_DETECTION, LocalDateTime.now());

        return insights.stream()
                .filter(i -> i.getGeneratedAt().isAfter(since))
                .map(this::toAnomalyResponse)
                .toList();
    }

    public List<BurnoutRiskResponse> analyzeBurnoutRisk() {
        var insights = insightRepository.findValidByType(InsightType.BURNOUT_DETECTION, LocalDateTime.now());

        return insights.stream()
                .map(this::toBurnoutRiskResponse)
                .toList();
    }

    private void saveChatHistory(User user, String sessionId, String role, String content) {
        ChatHistory history = ChatHistory.builder()
                .user(user)
                .sessionId(sessionId)
                .role(role)
                .content(content)
                .build();
        chatHistoryRepository.save(history);
    }

    private List<String> extractSuggestedActions(String response) {
        List<String> actions = new ArrayList<>();
        // Simple extraction - would be enhanced
        if (response.contains("ticket")) {
            actions.add("Ver detalhes dos tickets");
        }
        if (response.contains("sprint")) {
            actions.add("Analisar progresso da sprint");
        }
        if (response.contains("desenvolvedor") || response.contains("developer")) {
            actions.add("Ver métricas individuais");
        }
        return actions;
    }

    private List<ChatResponse.RelatedMetric> extractRelatedMetrics(String response) {
        // Placeholder - would parse metrics from response
        return List.of();
    }

    private TargetType getTargetType(InsightType type) {
        return switch (type) {
            case SPRINT_SUMMARY, DELIVERY_PREDICTION -> TargetType.SPRINT;
            case DEVELOPER_ANALYSIS, BURNOUT_DETECTION -> TargetType.DEVELOPER;
            case TEAM_HEALTH -> TargetType.TEAM;
            case ANOMALY_DETECTION, INFRASTRUCTURE_HEALTH -> TargetType.INFRASTRUCTURE;
            case CODE_QUALITY -> TargetType.PROJECT;
        };
    }

    private int getExpirationHours(InsightType type) {
        return switch (type) {
            case SPRINT_SUMMARY -> 24;
            case DELIVERY_PREDICTION -> 4;
            case DEVELOPER_ANALYSIS -> 168; // 1 week
            case ANOMALY_DETECTION -> 1;
            case BURNOUT_DETECTION -> 24;
            case TEAM_HEALTH -> 12;
            case CODE_QUALITY -> 24;
            case INFRASTRUCTURE_HEALTH -> 1;
        };
    }

    private AnomalyResponse toAnomalyResponse(AIInsight insight) {
        return new AnomalyResponse(
                true,
                "unknown",
                "unknown",
                "N/A",
                "N/A",
                "N/A",
                insight.getContent(),
                List.of(),
                insight.getGeneratedAt()
        );
    }

    private BurnoutRiskResponse toBurnoutRiskResponse(AIInsight insight) {
        return new BurnoutRiskResponse(
                Long.parseLong(insight.getTargetId()),
                "Developer",
                insight.getConfidenceScore(),
                "MEDIUM",
                List.of(),
                insight.getContent()
        );
    }
}
