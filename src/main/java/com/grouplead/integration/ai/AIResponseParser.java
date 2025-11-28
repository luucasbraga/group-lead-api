package com.grouplead.integration.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grouplead.domain.enums.RiskLevel;
import com.grouplead.domain.vo.PredictionResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AIResponseParser {

    private final ObjectMapper objectMapper;

    public PredictionResult parsePredictionResponse(String response, String sprintId) {
        try {
            // Extract JSON from response (it might be wrapped in markdown)
            String jsonContent = extractJson(response);
            JsonNode root = objectMapper.readTree(jsonContent);

            return new PredictionResult(
                    sprintId,
                    parseDate(root.path("predicted_completion_date").asText()),
                    root.path("confidence_score").asDouble(0.5),
                    root.path("completion_probability").asDouble(0.5),
                    parseRiskLevel(root.path("risk_level").asText("MEDIUM")),
                    parseStringList(root.path("risk_factors")),
                    parseStringList(root.path("recommendations")),
                    parseScenarioAnalysis(root.path("scenario_analysis"))
            );
        } catch (Exception e) {
            log.error("Error parsing prediction response", e);
            return createDefaultPrediction(sprintId);
        }
    }

    public List<String> parseInsights(String response) {
        List<String> insights = new ArrayList<>();

        // Simple parsing - split by newlines and bullet points
        String[] lines = response.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("-") || line.startsWith("*") || line.startsWith("•")) {
                insights.add(line.substring(1).trim());
            } else if (line.matches("^\\d+\\..*")) {
                insights.add(line.replaceFirst("^\\d+\\.\\s*", ""));
            }
        }

        return insights;
    }

    private String extractJson(String text) {
        // Try to find JSON in the response
        int jsonStart = text.indexOf('{');
        int jsonEnd = text.lastIndexOf('}');

        if (jsonStart >= 0 && jsonEnd > jsonStart) {
            return text.substring(jsonStart, jsonEnd + 1);
        }

        return text;
    }

    private LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr);
        } catch (Exception e) {
            return LocalDate.now().plusDays(7);
        }
    }

    private RiskLevel parseRiskLevel(String level) {
        try {
            return RiskLevel.valueOf(level.toUpperCase());
        } catch (Exception e) {
            return RiskLevel.MEDIUM;
        }
    }

    private List<String> parseStringList(JsonNode node) {
        List<String> list = new ArrayList<>();
        if (node.isArray()) {
            for (JsonNode item : node) {
                list.add(item.asText());
            }
        }
        return list;
    }

    private PredictionResult.ScenarioAnalysis parseScenarioAnalysis(JsonNode node) {
        return new PredictionResult.ScenarioAnalysis(
                parseScenario(node.path("optimistic")),
                parseScenario(node.path("realistic")),
                parseScenario(node.path("pessimistic"))
        );
    }

    private PredictionResult.Scenario parseScenario(JsonNode node) {
        return new PredictionResult.Scenario(
                parseDate(node.path("date").asText()),
                node.path("probability").asDouble(0.5)
        );
    }

    private PredictionResult createDefaultPrediction(String sprintId) {
        LocalDate now = LocalDate.now();
        return new PredictionResult(
                sprintId,
                now.plusDays(7),
                0.5,
                0.5,
                RiskLevel.MEDIUM,
                List.of("Unable to analyze - insufficient data"),
                List.of("Gather more historical data"),
                new PredictionResult.ScenarioAnalysis(
                        new PredictionResult.Scenario(now.plusDays(5), 0.3),
                        new PredictionResult.Scenario(now.plusDays(7), 0.5),
                        new PredictionResult.Scenario(now.plusDays(10), 0.2)
                )
        );
    }
}
