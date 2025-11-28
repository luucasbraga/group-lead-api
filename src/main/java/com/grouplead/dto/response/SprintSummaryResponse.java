package com.grouplead.dto.response;

import java.time.LocalDateTime;

public record SprintSummaryResponse(
        String sprintId,
        String sprintName,
        String summary,
        LocalDateTime generatedAt,
        Double confidenceScore
) {}
