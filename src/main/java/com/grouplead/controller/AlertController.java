package com.grouplead.controller;

import com.grouplead.domain.entity.Alert;
import com.grouplead.domain.entity.User;
import com.grouplead.dto.response.AlertResponse;
import com.grouplead.repository.AlertRepository;
import com.grouplead.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/alerts")
@RequiredArgsConstructor
@Tag(name = "Alerts", description = "Alert management endpoints")
public class AlertController {

    private final AlertRepository alertRepository;
    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "Get all active alerts")
    public ResponseEntity<List<AlertResponse>> getActiveAlerts() {
        var alerts = alertRepository.findActiveAlerts();
        return ResponseEntity.ok(alerts.stream().map(AlertResponse::from).toList());
    }

    @GetMapping("/unacknowledged")
    @Operation(summary = "Get unacknowledged alerts")
    public ResponseEntity<List<AlertResponse>> getUnacknowledgedAlerts() {
        var alerts = alertRepository.findUnacknowledgedAlerts();
        return ResponseEntity.ok(alerts.stream().map(AlertResponse::from).toList());
    }

    @PostMapping("/{id}/acknowledge")
    @Operation(summary = "Acknowledge an alert")
    public ResponseEntity<AlertResponse> acknowledgeAlert(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alert not found: " + id));

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        alert.acknowledge(user);
        alert = alertRepository.save(alert);

        return ResponseEntity.ok(AlertResponse.from(alert));
    }

    @PostMapping("/{id}/resolve")
    @Operation(summary = "Resolve an alert")
    public ResponseEntity<AlertResponse> resolveAlert(@PathVariable Long id) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alert not found: " + id));

        alert.resolve();
        alert = alertRepository.save(alert);

        return ResponseEntity.ok(AlertResponse.from(alert));
    }
}
