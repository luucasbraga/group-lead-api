package com.grouplead.dto.response;

import java.util.List;

public record InfrastructureMetricsResponse(
        List<ServiceMetrics> services,
        Double overallUptime,
        Double overallLatencyP99,
        Double overallErrorRate,
        int activeAlerts
) {
    public record ServiceMetrics(
            String name,
            String status,
            Double latencyP99,
            Double errorRate,
            Double cpu,
            Double memory,
            Double disk,
            int requestCount
    ) {}
}
