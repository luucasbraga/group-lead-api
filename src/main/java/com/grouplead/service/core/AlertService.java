package com.grouplead.service.core;

import com.grouplead.config.AlertProperties;
import com.grouplead.domain.entity.Alert;
import com.grouplead.domain.entity.Developer;
import com.grouplead.domain.entity.Metric;
import com.grouplead.domain.entity.Team;
import com.grouplead.domain.enums.AlertSeverity;
import com.grouplead.domain.enums.AlertType;
import com.grouplead.dto.request.AlertConfigRequest;
import com.grouplead.dto.response.AlertResponse;
import com.grouplead.exception.ResourceNotFoundException;
import com.grouplead.repository.AlertRepository;
import com.grouplead.repository.DeveloperRepository;
import com.grouplead.repository.MetricRepository;
import com.grouplead.repository.TeamRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class AlertService {

    private static final Logger log = LoggerFactory.getLogger(AlertService.class);

    private final AlertRepository alertRepository;
    private final TeamRepository teamRepository;
    private final DeveloperRepository developerRepository;
    private final MetricRepository metricRepository;
    private final AlertProperties alertProperties;

    public AlertService(AlertRepository alertRepository,
                       TeamRepository teamRepository,
                       DeveloperRepository developerRepository,
                       MetricRepository metricRepository,
                       AlertProperties alertProperties) {
        this.alertRepository = alertRepository;
        this.teamRepository = teamRepository;
        this.developerRepository = developerRepository;
        this.metricRepository = metricRepository;
        this.alertProperties = alertProperties;
    }

    @Transactional(readOnly = true)
    public Page<AlertResponse> getAlerts(Long teamId, Pageable pageable) {
        Page<Alert> alerts = teamId != null ?
                alertRepository.findByTeamIdOrderByCreatedAtDesc(teamId, pageable) :
                alertRepository.findAllByOrderByCreatedAtDesc(pageable);

        return alerts.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public List<AlertResponse> getActiveAlerts(Long teamId) {
        List<Alert> alerts = teamId != null ?
                alertRepository.findByTeamIdAndResolvedFalseOrderByCreatedAtDesc(teamId) :
                alertRepository.findByResolvedFalseOrderByCreatedAtDesc();

        return alerts.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AlertResponse getAlert(Long alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert", alertId));
        return mapToResponse(alert);
    }

    @Transactional
    public AlertResponse createAlert(AlertConfigRequest request) {
        Team team = request.getTeamId() != null ?
                teamRepository.findById(request.getTeamId()).orElse(null) : null;

        Alert alert = Alert.builder()
                .team(team)
                .type(request.getType())
                .severity(request.getSeverity())
                .title(request.getTitle())
                .message(request.getMessage())
                .source(request.getSource())
                .metadata(request.getMetadata())
                .resolved(false)
                .createdAt(LocalDateTime.now())
                .build();

        Alert saved = alertRepository.save(alert);
        log.info("Created alert: {} - {}", saved.getId(), saved.getTitle());

        return mapToResponse(saved);
    }

    @Transactional
    public AlertResponse resolveAlert(Long alertId, String resolution) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert", alertId));

        alert.setResolved(true);
        alert.setResolvedAt(LocalDateTime.now());
        alert.setResolution(resolution);

        Alert saved = alertRepository.save(alert);
        log.info("Resolved alert: {} - {}", saved.getId(), saved.getTitle());

        return mapToResponse(saved);
    }

    @Transactional
    public AlertResponse acknowledgeAlert(Long alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert", alertId));

        alert.setAcknowledged(true);
        alert.setAcknowledgedAt(LocalDateTime.now());

        Alert saved = alertRepository.save(alert);
        log.info("Acknowledged alert: {} - {}", saved.getId(), saved.getTitle());

        return mapToResponse(saved);
    }

    @Transactional
    public void checkVelocityThresholds(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", teamId));

        // Check velocity drop
        Double currentVelocity = metricRepository.findAverageVelocityForTeam(teamId,
                LocalDateTime.now().minusDays(14), LocalDateTime.now());
        Double previousVelocity = metricRepository.findAverageVelocityForTeam(teamId,
                LocalDateTime.now().minusDays(28), LocalDateTime.now().minusDays(14));

        if (currentVelocity != null && previousVelocity != null && previousVelocity > 0) {
            double dropPercentage = ((previousVelocity - currentVelocity) / previousVelocity) * 100;

            if (dropPercentage >= alertProperties.getVelocityDropThreshold()) {
                createVelocityAlert(team, dropPercentage, currentVelocity, previousVelocity);
            }
        }
    }

    @Transactional
    public void checkBurnoutRisk(Long developerId) {
        Developer developer = developerRepository.findById(developerId)
                .orElseThrow(() -> new ResourceNotFoundException("Developer", developerId));

        LocalDateTime twoWeeksAgo = LocalDateTime.now().minusDays(14);

        // Check for after-hours commits
        long afterHoursCommits = metricRepository.countAfterHoursCommits(developerId, twoWeeksAgo);
        long totalCommits = metricRepository.countTotalCommits(developerId, twoWeeksAgo);

        if (totalCommits > 0) {
            double afterHoursPercentage = (double) afterHoursCommits / totalCommits * 100;

            if (afterHoursPercentage >= alertProperties.getAfterHoursThreshold()) {
                createBurnoutAlert(developer, afterHoursPercentage);
            }
        }

        // Check for weekend work
        long weekendCommits = metricRepository.countWeekendCommits(developerId, twoWeeksAgo);
        if (weekendCommits >= alertProperties.getWeekendWorkThreshold()) {
            createWeekendWorkAlert(developer, weekendCommits);
        }
    }

    @Transactional
    public void checkInfrastructureThresholds(Long teamId, List<Metric> metrics) {
        Team team = teamRepository.findById(teamId).orElse(null);

        for (Metric metric : metrics) {
            // Check CPU threshold
            if (metric.getName().toLowerCase().contains("cpu") &&
                    metric.getValue() >= alertProperties.getCpuThreshold()) {
                createInfrastructureAlert(team, "High CPU Utilization",
                        String.format("CPU utilization is at %.1f%%, exceeding threshold of %.1f%%",
                                metric.getValue(), alertProperties.getCpuThreshold()),
                        AlertSeverity.WARNING, metric);
            }

            // Check Memory threshold
            if (metric.getName().toLowerCase().contains("memory") &&
                    metric.getValue() >= alertProperties.getMemoryThreshold()) {
                createInfrastructureAlert(team, "High Memory Utilization",
                        String.format("Memory utilization is at %.1f%%, exceeding threshold of %.1f%%",
                                metric.getValue(), alertProperties.getMemoryThreshold()),
                        AlertSeverity.WARNING, metric);
            }

            // Check Error rate threshold
            if (metric.getName().toLowerCase().contains("error") &&
                    metric.getValue() >= alertProperties.getErrorRateThreshold()) {
                createInfrastructureAlert(team, "High Error Rate",
                        String.format("Error rate is at %.2f%%, exceeding threshold of %.2f%%",
                                metric.getValue(), alertProperties.getErrorRateThreshold()),
                        AlertSeverity.CRITICAL, metric);
            }
        }
    }

    private void createVelocityAlert(Team team, double dropPercentage, double current, double previous) {
        Alert alert = Alert.builder()
                .team(team)
                .type(AlertType.VELOCITY_DROP)
                .severity(dropPercentage >= 30 ? AlertSeverity.CRITICAL : AlertSeverity.WARNING)
                .title("Velocity Drop Detected")
                .message(String.format("Team velocity dropped by %.1f%% (from %.1f to %.1f points)",
                        dropPercentage, previous, current))
                .source("velocity-monitor")
                .metadata(Map.of(
                        "current_velocity", String.valueOf(current),
                        "previous_velocity", String.valueOf(previous),
                        "drop_percentage", String.valueOf(dropPercentage)
                ))
                .resolved(false)
                .createdAt(LocalDateTime.now())
                .build();

        alertRepository.save(alert);
        log.warn("Created velocity drop alert for team {}: {}% drop", team.getName(), dropPercentage);
    }

    private void createBurnoutAlert(Developer developer, double afterHoursPercentage) {
        Alert alert = Alert.builder()
                .team(developer.getTeam())
                .type(AlertType.BURNOUT_RISK)
                .severity(afterHoursPercentage >= 50 ? AlertSeverity.HIGH : AlertSeverity.MEDIUM)
                .title("Burnout Risk Detected")
                .message(String.format("Developer %s has %.1f%% of commits outside business hours",
                        developer.getName(), afterHoursPercentage))
                .source("burnout-monitor")
                .metadata(Map.of(
                        "developer_id", String.valueOf(developer.getId()),
                        "developer_name", developer.getName(),
                        "after_hours_percentage", String.valueOf(afterHoursPercentage)
                ))
                .resolved(false)
                .createdAt(LocalDateTime.now())
                .build();

        alertRepository.save(alert);
        log.warn("Created burnout risk alert for developer {}", developer.getName());
    }

    private void createWeekendWorkAlert(Developer developer, long weekendCommits) {
        Alert alert = Alert.builder()
                .team(developer.getTeam())
                .type(AlertType.BURNOUT_RISK)
                .severity(AlertSeverity.MEDIUM)
                .title("Frequent Weekend Work Detected")
                .message(String.format("Developer %s has %d commits on weekends in the last 2 weeks",
                        developer.getName(), weekendCommits))
                .source("burnout-monitor")
                .metadata(Map.of(
                        "developer_id", String.valueOf(developer.getId()),
                        "developer_name", developer.getName(),
                        "weekend_commits", String.valueOf(weekendCommits)
                ))
                .resolved(false)
                .createdAt(LocalDateTime.now())
                .build();

        alertRepository.save(alert);
        log.warn("Created weekend work alert for developer {}", developer.getName());
    }

    private void createInfrastructureAlert(Team team, String title, String message,
                                           AlertSeverity severity, Metric metric) {
        Alert alert = Alert.builder()
                .team(team)
                .type(AlertType.INFRASTRUCTURE)
                .severity(severity)
                .title(title)
                .message(message)
                .source("infrastructure-monitor")
                .metadata(Map.of(
                        "metric_name", metric.getName(),
                        "metric_value", String.valueOf(metric.getValue()),
                        "metric_unit", metric.getUnit() != null ? metric.getUnit() : ""
                ))
                .resolved(false)
                .createdAt(LocalDateTime.now())
                .build();

        alertRepository.save(alert);
        log.warn("Created infrastructure alert: {}", title);
    }

    private AlertResponse mapToResponse(Alert alert) {
        return AlertResponse.builder()
                .id(alert.getId())
                .teamId(alert.getTeam() != null ? alert.getTeam().getId() : null)
                .teamName(alert.getTeam() != null ? alert.getTeam().getName() : null)
                .type(alert.getType())
                .severity(alert.getSeverity())
                .title(alert.getTitle())
                .message(alert.getMessage())
                .source(alert.getSource())
                .metadata(alert.getMetadata())
                .resolved(alert.isResolved())
                .acknowledged(alert.isAcknowledged())
                .resolution(alert.getResolution())
                .createdAt(alert.getCreatedAt())
                .acknowledgedAt(alert.getAcknowledgedAt())
                .resolvedAt(alert.getResolvedAt())
                .build();
    }
}
