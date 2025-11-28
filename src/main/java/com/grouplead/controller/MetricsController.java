package com.grouplead.controller;

import com.grouplead.domain.enums.MetricType;
import com.grouplead.domain.enums.PeriodType;
import com.grouplead.domain.vo.DateRange;
import com.grouplead.dto.response.CodeQualityResponse;
import com.grouplead.dto.response.DeveloperMetricsResponse;
import com.grouplead.dto.response.TimeSeriesResponse;
import com.grouplead.dto.response.VelocityResponse;
import com.grouplead.service.core.DeveloperService;
import com.grouplead.service.processor.MetricsProcessorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/metrics")
@RequiredArgsConstructor
@Tag(name = "Metrics", description = "Metrics endpoints")
public class MetricsController {

    private final MetricsProcessorService metricsService;
    private final DeveloperService developerService;

    @GetMapping("/team/{teamId}/velocity")
    @Operation(summary = "Get team velocity")
    public ResponseEntity<VelocityResponse> getTeamVelocity(
            @PathVariable Long teamId,
            @RequestParam(defaultValue = "10") int sprintCount) {

        return ResponseEntity.ok(metricsService.getTeamVelocity(teamId, sprintCount));
    }

    @GetMapping("/developer/{developerId}")
    @Operation(summary = "Get individual developer metrics")
    public ResponseEntity<DeveloperMetricsResponse> getDeveloperMetrics(
            @PathVariable Long developerId,
            @RequestParam(defaultValue = "MONTHLY") PeriodType period) {

        return ResponseEntity.ok(developerService.getMetrics(developerId, period));
    }

    @GetMapping("/code-quality")
    @Operation(summary = "Get code quality metrics")
    public ResponseEntity<CodeQualityResponse> getCodeQuality(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        DateRange dateRange = DateRange.of(startDate, endDate);
        return ResponseEntity.ok(metricsService.getCodeQualityMetrics(dateRange));
    }

    @GetMapping("/time-series")
    @Operation(summary = "Get time series metrics")
    public ResponseEntity<TimeSeriesResponse> getTimeSeries(
            @RequestParam MetricType metricType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "DAILY") String granularity) {

        return ResponseEntity.ok(metricsService.getTimeSeries(metricType, startDate, endDate, granularity));
    }
}
