package com.grouplead.domain.vo;

import com.grouplead.domain.enums.RiskLevel;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record PredictionResult(
        String sprintId,
        LocalDate predictedCompletionDate,
        Double confidenceScore,
        Double completionProbability,
        RiskLevel riskLevel,
        List<String> riskFactors,
        List<String> recommendations,
        ScenarioAnalysis scenarioAnalysis
) {
    public record ScenarioAnalysis(
            Scenario optimistic,
            Scenario realistic,
            Scenario pessimistic
    ) {}

    public record Scenario(
            LocalDate date,
            Double probability
    ) {}

    public boolean isAtRisk() {
        return riskLevel == RiskLevel.HIGH || riskLevel == RiskLevel.CRITICAL;
    }

    public boolean isOnTrack() {
        return completionProbability >= 0.8;
    }
}
