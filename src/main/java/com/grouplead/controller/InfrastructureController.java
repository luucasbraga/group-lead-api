package com.grouplead.controller;

import com.grouplead.integration.aws.dto.CostData;
import com.grouplead.integration.aws.dto.CostForecast;
import com.grouplead.service.collector.CloudWatchCollectorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/infrastructure")
@RequiredArgsConstructor
@Tag(name = "Infrastructure", description = "Infrastructure and cost metrics endpoints")
public class InfrastructureController {

    private final CloudWatchCollectorService cloudWatchCollectorService;

    // ==================== EC2 Metrics ====================

    @PostMapping("/collect/ec2/{instanceId}")
    @Operation(summary = "Collect EC2 metrics for an instance")
    public ResponseEntity<CollectionResponse> collectEC2Metrics(
            @PathVariable String instanceId,
            @RequestParam Long teamId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since) {

        if (since == null) {
            since = LocalDateTime.now().minusHours(1);
        }

        var result = cloudWatchCollectorService.collectEC2Metrics(instanceId, teamId, since);
        return ResponseEntity.ok(new CollectionResponse(result.collected(), result.errors(), "EC2 metrics collected"));
    }

    // ==================== RDS Metrics ====================

    @PostMapping("/collect/rds/{dbInstanceId}")
    @Operation(summary = "Collect RDS metrics for a database instance")
    public ResponseEntity<CollectionResponse> collectRDSMetrics(
            @PathVariable String dbInstanceId,
            @RequestParam Long teamId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since) {

        if (since == null) {
            since = LocalDateTime.now().minusHours(1);
        }

        var result = cloudWatchCollectorService.collectRDSMetrics(dbInstanceId, teamId, since);
        return ResponseEntity.ok(new CollectionResponse(result.collected(), result.errors(), "RDS metrics collected"));
    }

    // ==================== ECS Metrics ====================

    @PostMapping("/collect/ecs/{clusterName}/{serviceName}")
    @Operation(summary = "Collect ECS metrics for a service")
    public ResponseEntity<CollectionResponse> collectECSMetrics(
            @PathVariable String clusterName,
            @PathVariable String serviceName,
            @RequestParam Long teamId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since) {

        if (since == null) {
            since = LocalDateTime.now().minusHours(1);
        }

        var result = cloudWatchCollectorService.collectECSMetrics(clusterName, serviceName, teamId, since);
        return ResponseEntity.ok(new CollectionResponse(result.collected(), result.errors(), "ECS metrics collected"));
    }

    // ==================== Lambda Metrics ====================

    @PostMapping("/collect/lambda/{functionName}")
    @Operation(summary = "Collect Lambda metrics for a function")
    public ResponseEntity<CollectionResponse> collectLambdaMetrics(
            @PathVariable String functionName,
            @RequestParam Long teamId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since) {

        if (since == null) {
            since = LocalDateTime.now().minusHours(1);
        }

        var result = cloudWatchCollectorService.collectLambdaMetrics(functionName, teamId, since);
        return ResponseEntity.ok(new CollectionResponse(result.collected(), result.errors(), "Lambda metrics collected"));
    }

    // ==================== Cost Metrics ====================

    @PostMapping("/collect/cost")
    @Operation(summary = "Collect AWS cost metrics for a team")
    public ResponseEntity<CollectionResponse> collectCostMetrics(
            @RequestParam Long teamId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        var result = cloudWatchCollectorService.collectCostMetrics(teamId, startDate, endDate);
        return ResponseEntity.ok(new CollectionResponse(result.collected(), result.errors(), "Cost metrics collected"));
    }

    @GetMapping("/cost/forecast")
    @Operation(summary = "Get AWS cost forecast")
    public ResponseEntity<CostForecast> getCostForecast(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        return ResponseEntity.ok(cloudWatchCollectorService.getCostForecast(startDate, endDate));
    }

    @GetMapping("/cost/top-resources")
    @Operation(summary = "Get top cost resources")
    public ResponseEntity<List<CostData.ResourceCost>> getTopCostResources(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "10") int limit) {

        return ResponseEntity.ok(cloudWatchCollectorService.getTopCostResources(startDate, endDate, limit));
    }

    // ==================== Response DTOs ====================

    public record CollectionResponse(int collected, int errors, String message) {}
}
