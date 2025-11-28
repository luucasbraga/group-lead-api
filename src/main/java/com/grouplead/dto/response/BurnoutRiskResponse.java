package com.grouplead.dto.response;

import java.util.List;

public record BurnoutRiskResponse(
        Long developerId,
        String developerName,
        Double riskScore,
        String riskLevel,
        List<String> indicators,
        String recommendation
) {}
