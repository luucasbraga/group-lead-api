package com.grouplead.dto.response;

import java.util.List;

public record CodeQualityResponse(
        Double testCoverage,
        Double testCoverageTrend,
        int totalTests,
        int passingTests,
        int failingTests,
        int bugsFound,
        int bugsTrend,
        Double technicalDebtHours,
        List<CodeSmell> topCodeSmells,
        List<SecurityIssue> securityIssues
) {
    public record CodeSmell(
            String type,
            String description,
            int count,
            String severity
    ) {}

    public record SecurityIssue(
            String type,
            String description,
            String severity,
            String file,
            int line
    ) {}
}
