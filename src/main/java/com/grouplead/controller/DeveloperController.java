package com.grouplead.controller;

import com.grouplead.dto.request.DeveloperRequest;
import com.grouplead.dto.response.DeveloperResponse;
import com.grouplead.service.core.DeveloperService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/developers")
@RequiredArgsConstructor
@Tag(name = "Developers", description = "Developer management endpoints")
public class DeveloperController {

    private final DeveloperService developerService;

    @GetMapping
    @Operation(summary = "Get all developers")
    public ResponseEntity<List<DeveloperResponse>> getAllDevelopers() {
        return ResponseEntity.ok(developerService.getAllDevelopers());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get developer by ID")
    public ResponseEntity<DeveloperResponse> getDeveloperById(@PathVariable Long id) {
        return ResponseEntity.ok(developerService.getDeveloperById(id));
    }

    @GetMapping("/team/{teamId}")
    @Operation(summary = "Get developers by team")
    public ResponseEntity<List<DeveloperResponse>> getDevelopersByTeam(@PathVariable Long teamId) {
        return ResponseEntity.ok(developerService.getDevelopersByTeam(teamId));
    }

    @PostMapping
    @Operation(summary = "Create a new developer")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TECH_LEAD')")
    public ResponseEntity<DeveloperResponse> createDeveloper(@Valid @RequestBody DeveloperRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(developerService.createDeveloper(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a developer")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TECH_LEAD')")
    public ResponseEntity<DeveloperResponse> updateDeveloper(
            @PathVariable Long id,
            @Valid @RequestBody DeveloperRequest request) {
        return ResponseEntity.ok(developerService.updateDeveloper(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate a developer")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TECH_LEAD')")
    public ResponseEntity<Void> deleteDeveloper(@PathVariable Long id) {
        developerService.deleteDeveloper(id);
        return ResponseEntity.noContent().build();
    }
}
