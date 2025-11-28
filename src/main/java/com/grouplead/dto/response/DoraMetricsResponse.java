package com.grouplead.dto.response;

import com.grouplead.domain.enums.DoraClassification;
import com.grouplead.domain.vo.DoraMetrics;

public record DoraMetricsResponse(
        DeploymentFrequencyResponse deploymentFrequency,
        LeadTimeResponse leadTimeForChanges,
        ChangeFailureRateResponse changeFailureRate,
        MTTRResponse meanTimeToRecovery,
        DoraClassification overallClassification
) {
    public record DeploymentFrequencyResponse(
            int totalDeployments,
            double deploymentsPerDay,
            double deploymentsPerWeek,
            DoraClassification classification,
            String trend
    ) {}

    public record LeadTimeResponse(
            double averageHours,
            double medianHours,
            double p90Hours,
            DoraClassification classification
    ) {}

    public record ChangeFailureRateResponse(
            int totalDeployments,
            int failedDeployments,
            double rate,
            DoraClassification classification
    ) {}

    public record MTTRResponse(
            double averageMinutes,
            int incidentCount,
            DoraClassification classification
    ) {}

    public static DoraMetricsResponse from(DoraMetrics metrics) {
        return new DoraMetricsResponse(
                new DeploymentFrequencyResponse(
                        metrics.deploymentFrequency().totalDeployments(),
                        metrics.deploymentFrequency().deploymentsPerDay(),
                        metrics.deploymentFrequency().deploymentsPerWeek(),
                        metrics.deploymentFrequency().classification(),
                        metrics.deploymentFrequency().trend()
                ),
                new LeadTimeResponse(
                        metrics.leadTimeForChanges().averageHours(),
                        metrics.leadTimeForChanges().medianHours(),
                        metrics.leadTimeForChanges().p90Hours(),
                        metrics.leadTimeForChanges().classification()
                ),
                new ChangeFailureRateResponse(
                        metrics.changeFailureRate().totalDeployments(),
                        metrics.changeFailureRate().failedDeployments(),
                        metrics.changeFailureRate().rate(),
                        metrics.changeFailureRate().classification()
                ),
                new MTTRResponse(
                        metrics.meanTimeToRecovery().averageMinutes(),
                        metrics.meanTimeToRecovery().incidentCount(),
                        metrics.meanTimeToRecovery().classification()
                ),
                metrics.getOverallClassification()
        );
    }
}
