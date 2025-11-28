package com.grouplead.controller;

import com.grouplead.domain.entity.Incident;
import com.grouplead.domain.enums.IncidentSeverity;
import com.grouplead.domain.enums.IncidentStatus;
import com.grouplead.service.core.IncidentService;
import com.grouplead.service.core.IncidentService.IncidentMetrics;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/incidents")
@RequiredArgsConstructor
@Tag(name = "Incidents", description = "Incident management endpoints")
public class IncidentController {

    private final IncidentService incidentService;

    @GetMapping
    @Operation(summary = "Get all incidents", description = "Returns paginated list of incidents")
    public ResponseEntity<Page<Incident>> getIncidents(
            @RequestParam(required = false) Long teamId,
            Pageable pageable) {
        return ResponseEntity.ok(incidentService.getIncidents(teamId, pageable));
    }

    @GetMapping("/active")
    @Operation(summary = "Get active (unresolved) incidents")
    public ResponseEntity<List<Incident>> getActiveIncidents(
            @RequestParam(required = false) Long teamId) {
        return ResponseEntity.ok(incidentService.getActiveIncidents(teamId));
    }

    @GetMapping("/{incidentId}")
    @Operation(summary = "Get incident by ID")
    public ResponseEntity<Incident> getIncident(@PathVariable Long incidentId) {
        return ResponseEntity.ok(incidentService.getIncident(incidentId));
    }

    @PostMapping
    @Operation(summary = "Create a new incident")
    public ResponseEntity<Incident> createIncident(
            @RequestParam(required = false) Long teamId,
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam IncidentSeverity severity,
            @RequestParam(required = false) String source) {
        Incident incident = incidentService.createIncident(teamId, title, description, severity, source);
        return ResponseEntity.ok(incident);
    }

    @PutMapping("/{incidentId}/status")
    @Operation(summary = "Update incident status")
    public ResponseEntity<Incident> updateStatus(
            @PathVariable Long incidentId,
            @RequestParam IncidentStatus status) {
        return ResponseEntity.ok(incidentService.updateIncidentStatus(incidentId, status));
    }

    @PostMapping("/{incidentId}/resolve")
    @Operation(summary = "Resolve an incident")
    public ResponseEntity<Incident> resolveIncident(
            @PathVariable Long incidentId,
            @RequestParam String resolution,
            @RequestParam(required = false) String rootCause) {
        return ResponseEntity.ok(incidentService.resolveIncident(incidentId, resolution, rootCause));
    }

    @PostMapping("/{incidentId}/timeline")
    @Operation(summary = "Add a timeline entry to an incident")
    public ResponseEntity<Incident> addTimelineEntry(
            @PathVariable Long incidentId,
            @RequestParam String entry) {
        return ResponseEntity.ok(incidentService.addTimelineEntry(incidentId, entry));
    }

    @GetMapping("/metrics")
    @Operation(summary = "Get incident metrics for a period")
    public ResponseEntity<IncidentMetrics> getIncidentMetrics(
            @RequestParam(required = false) Long teamId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(incidentService.getIncidentMetrics(teamId, startDate, endDate));
    }

    @GetMapping("/mttr")
    @Operation(summary = "Get average MTTR (Mean Time To Recovery) for a period")
    public ResponseEntity<Double> getAverageMTTR(
            @RequestParam(required = false) Long teamId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(incidentService.calculateAverageMTTR(teamId, startDate, endDate));
    }
}
