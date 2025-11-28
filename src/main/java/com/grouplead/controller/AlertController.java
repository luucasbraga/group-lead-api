package com.grouplead.controller;

import com.grouplead.dto.request.AlertConfigRequest;
import com.grouplead.dto.response.AlertResponse;
import com.grouplead.service.core.AlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
@Tag(name = "Alerts", description = "Alert management endpoints")
public class AlertController {

    private final AlertService alertService;

    @GetMapping
    @Operation(summary = "Get all alerts", description = "Returns paginated list of alerts")
    public ResponseEntity<Page<AlertResponse>> getAlerts(
            @RequestParam(required = false) Long teamId,
            Pageable pageable) {
        return ResponseEntity.ok(alertService.getAlerts(teamId, pageable));
    }

    @GetMapping("/active")
    @Operation(summary = "Get active (unresolved) alerts")
    public ResponseEntity<List<AlertResponse>> getActiveAlerts(
            @RequestParam(required = false) Long teamId) {
        return ResponseEntity.ok(alertService.getActiveAlerts(teamId));
    }

    @GetMapping("/{alertId}")
    @Operation(summary = "Get alert by ID")
    public ResponseEntity<AlertResponse> getAlert(@PathVariable Long alertId) {
        return ResponseEntity.ok(alertService.getAlert(alertId));
    }

    @PostMapping
    @Operation(summary = "Create a new alert")
    public ResponseEntity<AlertResponse> createAlert(@RequestBody AlertConfigRequest request) {
        return ResponseEntity.ok(alertService.createAlert(request));
    }

    @PostMapping("/{alertId}/acknowledge")
    @Operation(summary = "Acknowledge an alert")
    public ResponseEntity<AlertResponse> acknowledgeAlert(@PathVariable Long alertId) {
        return ResponseEntity.ok(alertService.acknowledgeAlert(alertId));
    }

    @PostMapping("/{alertId}/resolve")
    @Operation(summary = "Resolve an alert")
    public ResponseEntity<AlertResponse> resolveAlert(
            @PathVariable Long alertId,
            @RequestParam(required = false) String resolution) {
        return ResponseEntity.ok(alertService.resolveAlert(alertId, resolution));
    }

    @PostMapping("/check/velocity/{teamId}")
    @Operation(summary = "Check velocity thresholds for a team")
    public ResponseEntity<Void> checkVelocityThresholds(@PathVariable Long teamId) {
        alertService.checkVelocityThresholds(teamId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/check/burnout/{developerId}")
    @Operation(summary = "Check burnout risk for a developer")
    public ResponseEntity<Void> checkBurnoutRisk(@PathVariable Long developerId) {
        alertService.checkBurnoutRisk(developerId);
        return ResponseEntity.ok().build();
    }
}
