package com.grouplead.domain.vo;

import com.grouplead.domain.enums.DoraClassification;

public record DoraMetrics(
        DeploymentFrequency deploymentFrequency,
        LeadTimeForChanges leadTimeForChanges,
        ChangeFailureRate changeFailureRate,
        MeanTimeToRecovery meanTimeToRecovery
) {
    public record DeploymentFrequency(
            int totalDeployments,
            double deploymentsPerDay,
            double deploymentsPerWeek,
            DoraClassification classification,
            String trend
    ) {
        public static DeploymentFrequency empty() {
            return new DeploymentFrequency(0, 0, 0, DoraClassification.LOW, "stable");
        }
    }

    public record LeadTimeForChanges(
            double averageHours,
            double medianHours,
            double p90Hours,
            DoraClassification classification
    ) {
        public static LeadTimeForChanges empty() {
            return new LeadTimeForChanges(0, 0, 0, DoraClassification.LOW);
        }
    }

    public record ChangeFailureRate(
            int totalDeployments,
            int failedDeployments,
            double rate,
            DoraClassification classification
    ) {
        public static ChangeFailureRate empty() {
            return new ChangeFailureRate(0, 0, 0, DoraClassification.ELITE);
        }
    }

    public record MeanTimeToRecovery(
            double averageMinutes,
            int incidentCount,
            DoraClassification classification
    ) {
        public static MeanTimeToRecovery empty() {
            return new MeanTimeToRecovery(0, 0, DoraClassification.ELITE);
        }
    }

    public DoraClassification getOverallClassification() {
        int score = 0;
        score += classificationScore(deploymentFrequency.classification());
        score += classificationScore(leadTimeForChanges.classification());
        score += classificationScore(changeFailureRate.classification());
        score += classificationScore(meanTimeToRecovery.classification());

        double average = score / 4.0;
        if (average >= 3.5) return DoraClassification.ELITE;
        if (average >= 2.5) return DoraClassification.HIGH;
        if (average >= 1.5) return DoraClassification.MEDIUM;
        return DoraClassification.LOW;
    }

    private int classificationScore(DoraClassification classification) {
        return switch (classification) {
            case ELITE -> 4;
            case HIGH -> 3;
            case MEDIUM -> 2;
            case LOW -> 1;
        };
    }
}
