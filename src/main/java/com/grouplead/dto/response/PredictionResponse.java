package com.grouplead.dto.response;

import com.grouplead.domain.enums.RiskLevel;
import com.grouplead.domain.vo.PredictionResult;

import java.time.LocalDate;
import java.util.List;

public record PredictionResponse(
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

    public static PredictionResponse from(PredictionResult result) {
        return new PredictionResponse(
                result.sprintId(),
                result.predictedCompletionDate(),
                result.confidenceScore(),
                result.completionProbability(),
                result.riskLevel(),
                result.riskFactors(),
                result.recommendations(),
                new ScenarioAnalysis(
                        new Scenario(result.scenarioAnalysis().optimistic().date(), result.scenarioAnalysis().optimistic().probability()),
                        new Scenario(result.scenarioAnalysis().realistic().date(), result.scenarioAnalysis().realistic().probability()),
                        new Scenario(result.scenarioAnalysis().pessimistic().date(), result.scenarioAnalysis().pessimistic().probability())
                )
        );
    }
}
