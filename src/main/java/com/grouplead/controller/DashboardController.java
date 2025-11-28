package com.grouplead.controller;

import com.grouplead.domain.enums.PeriodType;
import com.grouplead.domain.vo.DateRange;
import com.grouplead.dto.response.DashboardSummaryResponse;
import com.grouplead.dto.response.DoraMetricsResponse;
import com.grouplead.dto.response.InfrastructureMetricsResponse;
import com.grouplead.dto.response.TeamDashboardResponse;
import com.grouplead.service.core.DashboardService;
import com.grouplead.service.processor.DoraMetricsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Dashboard endpoints")
public class DashboardController {

    private final DashboardService dashboardService;
    private final DoraMetricsService doraMetricsService;

    @GetMapping("/summary")
    @Operation(summary = "Get dashboard summary")
    public ResponseEntity<DashboardSummaryResponse> getSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        DateRange dateRange = DateRange.of(startDate, endDate);
        return ResponseEntity.ok(dashboardService.getSummary(dateRange));
    }

    @GetMapping("/team/{teamId}")
    @Operation(summary = "Get team dashboard")
    public ResponseEntity<TeamDashboardResponse> getTeamDashboard(
            @PathVariable Long teamId,
            @RequestParam(defaultValue = "WEEKLY") PeriodType period) {

        return ResponseEntity.ok(dashboardService.getTeamDashboard(teamId, period));
    }

    @GetMapping("/dora-metrics")
    @Operation(summary = "Get DORA metrics")
    public ResponseEntity<DoraMetricsResponse> getDoraMetrics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        DateRange dateRange = DateRange.of(startDate, endDate);
        var metrics = doraMetricsService.calculateMetrics(dateRange);

        return ResponseEntity.ok(DoraMetricsResponse.from(metrics));
    }

    @GetMapping("/infrastructure")
    @Operation(summary = "Get infrastructure metrics")
    public ResponseEntity<InfrastructureMetricsResponse> getInfrastructureMetrics(
            @RequestParam(defaultValue = "DAILY") PeriodType period) {

        return ResponseEntity.ok(dashboardService.getInfrastructureMetrics(period));
    }
}
