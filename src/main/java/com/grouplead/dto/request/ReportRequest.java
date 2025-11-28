package com.grouplead.dto.request;

import com.grouplead.domain.enums.PeriodType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record ReportRequest(
        @NotNull(message = "Report type is required")
        String reportType,

        @NotNull(message = "Period type is required")
        PeriodType periodType,

        LocalDate startDate,
        LocalDate endDate,

        Long teamId,
        List<Long> developerIds,

        String format,

        boolean includeCharts,
        boolean includeAIInsights
) {}
