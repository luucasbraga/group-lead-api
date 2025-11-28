package com.grouplead.controller;

import com.grouplead.domain.entity.Sprint;
import com.grouplead.domain.vo.SprintMetrics;
import com.grouplead.service.core.SprintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/sprints")
@RequiredArgsConstructor
@Tag(name = "Sprints", description = "Sprint management endpoints")
public class SprintController {

    private final SprintService sprintService;

    @GetMapping
    @Operation(summary = "Get all sprints", description = "Returns paginated list of sprints")
    public ResponseEntity<Page<Sprint>> getSprints(
            @RequestParam(required = false) Long teamId,
            Pageable pageable) {
        return ResponseEntity.ok(sprintService.getSprints(teamId, pageable));
    }

    @GetMapping("/{sprintId}")
    @Operation(summary = "Get sprint by ID")
    public ResponseEntity<Sprint> getSprint(@PathVariable Long sprintId) {
        return ResponseEntity.ok(sprintService.getSprint(sprintId));
    }

    @GetMapping("/team/{teamId}/current")
    @Operation(summary = "Get current active sprint for a team")
    public ResponseEntity<Sprint> getCurrentSprint(@PathVariable Long teamId) {
        Optional<Sprint> sprint = sprintService.getCurrentSprint(teamId);
        return sprint.map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/team/{teamId}/recent")
    @Operation(summary = "Get recent sprints for a team")
    public ResponseEntity<List<Sprint>> getRecentSprints(
            @PathVariable Long teamId,
            @RequestParam(defaultValue = "5") int count) {
        return ResponseEntity.ok(sprintService.getRecentSprints(teamId, count));
    }

    @PostMapping
    @Operation(summary = "Create a new sprint")
    public ResponseEntity<Sprint> createSprint(
            @RequestParam Long teamId,
            @RequestParam String name,
            @RequestParam(required = false) String externalId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(required = false) String goal) {
        Sprint sprint = sprintService.createSprint(teamId, name, externalId, startDate, endDate, goal);
        return ResponseEntity.ok(sprint);
    }

    @PostMapping("/{sprintId}/start")
    @Operation(summary = "Start a sprint")
    public ResponseEntity<Sprint> startSprint(@PathVariable Long sprintId) {
        return ResponseEntity.ok(sprintService.startSprint(sprintId));
    }

    @PostMapping("/{sprintId}/complete")
    @Operation(summary = "Complete a sprint")
    public ResponseEntity<Sprint> completeSprint(@PathVariable Long sprintId) {
        return ResponseEntity.ok(sprintService.completeSprint(sprintId));
    }

    @GetMapping("/{sprintId}/metrics")
    @Operation(summary = "Get sprint metrics")
    public ResponseEntity<SprintMetrics> getSprintMetrics(@PathVariable Long sprintId) {
        return ResponseEntity.ok(sprintService.getSprintMetrics(sprintId));
    }

    @GetMapping("/team/{teamId}/velocity")
    @Operation(summary = "Get average velocity for a team")
    public ResponseEntity<Double> getAverageVelocity(
            @PathVariable Long teamId,
            @RequestParam(defaultValue = "5") int lastNSprints) {
        return ResponseEntity.ok(sprintService.calculateAverageVelocity(teamId, lastNSprints));
    }

    @GetMapping("/team/{teamId}/history")
    @Operation(summary = "Get sprint history with metrics for a team")
    public ResponseEntity<List<SprintMetrics>> getSprintHistory(
            @PathVariable Long teamId,
            @RequestParam(defaultValue = "10") int lastNSprints) {
        return ResponseEntity.ok(sprintService.getSprintHistory(teamId, lastNSprints));
    }

    @PutMapping("/{sprintId}/refresh")
    @Operation(summary = "Refresh sprint points calculation")
    public ResponseEntity<Sprint> refreshSprintPoints(@PathVariable Long sprintId) {
        return ResponseEntity.ok(sprintService.updateSprintPoints(sprintId));
    }
}
