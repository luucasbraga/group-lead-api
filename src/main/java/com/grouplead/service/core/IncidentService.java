package com.grouplead.service.core;

import com.grouplead.domain.entity.Incident;
import com.grouplead.domain.entity.Team;
import com.grouplead.domain.enums.IncidentSeverity;
import com.grouplead.domain.enums.IncidentStatus;
import com.grouplead.exception.ResourceNotFoundException;
import com.grouplead.repository.IncidentRepository;
import com.grouplead.repository.TeamRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class IncidentService {

    private static final Logger log = LoggerFactory.getLogger(IncidentService.class);

    private final IncidentRepository incidentRepository;
    private final TeamRepository teamRepository;

    public IncidentService(IncidentRepository incidentRepository,
                          TeamRepository teamRepository) {
        this.incidentRepository = incidentRepository;
        this.teamRepository = teamRepository;
    }

    @Transactional(readOnly = true)
    public Page<Incident> getIncidents(Long teamId, Pageable pageable) {
        if (teamId != null) {
            return incidentRepository.findByTeamIdOrderByStartedAtDesc(teamId, pageable);
        }
        return incidentRepository.findAllByOrderByStartedAtDesc(pageable);
    }

    @Transactional(readOnly = true)
    public List<Incident> getActiveIncidents(Long teamId) {
        if (teamId != null) {
            return incidentRepository.findByTeamIdAndStatusNotOrderByStartedAtDesc(
                    teamId, IncidentStatus.RESOLVED);
        }
        return incidentRepository.findByStatusNotOrderByStartedAtDesc(IncidentStatus.RESOLVED);
    }

    @Transactional(readOnly = true)
    public Incident getIncident(Long incidentId) {
        return incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident", incidentId));
    }

    @Transactional
    public Incident createIncident(Long teamId, String title, String description,
                                   IncidentSeverity severity, String source) {
        Team team = teamId != null ?
                teamRepository.findById(teamId).orElse(null) : null;

        Incident incident = Incident.builder()
                .team(team)
                .title(title)
                .description(description)
                .severity(severity)
                .status(IncidentStatus.OPEN)
                .source(source)
                .startedAt(LocalDateTime.now())
                .build();

        Incident saved = incidentRepository.save(incident);
        log.info("Created incident: {} - {} ({})", saved.getId(), saved.getTitle(), saved.getSeverity());

        return saved;
    }

    @Transactional
    public Incident updateIncidentStatus(Long incidentId, IncidentStatus newStatus) {
        Incident incident = getIncident(incidentId);
        IncidentStatus oldStatus = incident.getStatus();

        incident.setStatus(newStatus);

        if (newStatus == IncidentStatus.INVESTIGATING && oldStatus == IncidentStatus.OPEN) {
            incident.setAcknowledgedAt(LocalDateTime.now());
        } else if (newStatus == IncidentStatus.RESOLVED) {
            incident.setResolvedAt(LocalDateTime.now());
            calculateMTTR(incident);
        }

        Incident saved = incidentRepository.save(incident);
        log.info("Updated incident {} status from {} to {}", incidentId, oldStatus, newStatus);

        return saved;
    }

    @Transactional
    public Incident resolveIncident(Long incidentId, String resolution, String rootCause) {
        Incident incident = getIncident(incidentId);

        incident.setStatus(IncidentStatus.RESOLVED);
        incident.setResolvedAt(LocalDateTime.now());
        incident.setResolution(resolution);
        incident.setRootCause(rootCause);
        calculateMTTR(incident);

        Incident saved = incidentRepository.save(incident);
        log.info("Resolved incident: {} - MTTR: {} minutes", saved.getId(), saved.getMttrMinutes());

        return saved;
    }

    @Transactional
    public Incident addTimelineEntry(Long incidentId, String entry) {
        Incident incident = getIncident(incidentId);

        String timestamp = LocalDateTime.now().toString();
        String timelineEntry = "[" + timestamp + "] " + entry;

        if (incident.getTimeline() == null || incident.getTimeline().isEmpty()) {
            incident.setTimeline(timelineEntry);
        } else {
            incident.setTimeline(incident.getTimeline() + "\n" + timelineEntry);
        }

        return incidentRepository.save(incident);
    }

    @Transactional(readOnly = true)
    public IncidentMetrics getIncidentMetrics(Long teamId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Incident> incidents;
        if (teamId != null) {
            incidents = incidentRepository.findByTeamIdAndStartedAtBetween(teamId, startDate, endDate);
        } else {
            incidents = incidentRepository.findByStartedAtBetween(startDate, endDate);
        }

        long totalIncidents = incidents.size();
        long resolvedIncidents = incidents.stream()
                .filter(i -> i.getStatus() == IncidentStatus.RESOLVED)
                .count();

        double averageMTTR = incidents.stream()
                .filter(i -> i.getMttrMinutes() != null)
                .mapToLong(Incident::getMttrMinutes)
                .average()
                .orElse(0.0);

        Map<IncidentSeverity, Long> bySeverity = Map.of(
                IncidentSeverity.CRITICAL, countBySeverity(incidents, IncidentSeverity.CRITICAL),
                IncidentSeverity.HIGH, countBySeverity(incidents, IncidentSeverity.HIGH),
                IncidentSeverity.MEDIUM, countBySeverity(incidents, IncidentSeverity.MEDIUM),
                IncidentSeverity.LOW, countBySeverity(incidents, IncidentSeverity.LOW)
        );

        return new IncidentMetrics(
                totalIncidents,
                resolvedIncidents,
                totalIncidents - resolvedIncidents,
                averageMTTR,
                bySeverity
        );
    }

    @Transactional(readOnly = true)
    public double calculateAverageMTTR(Long teamId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Incident> resolvedIncidents;
        if (teamId != null) {
            resolvedIncidents = incidentRepository.findByTeamIdAndStatusAndResolvedAtBetween(
                    teamId, IncidentStatus.RESOLVED, startDate, endDate);
        } else {
            resolvedIncidents = incidentRepository.findByStatusAndResolvedAtBetween(
                    IncidentStatus.RESOLVED, startDate, endDate);
        }

        return resolvedIncidents.stream()
                .filter(i -> i.getMttrMinutes() != null)
                .mapToLong(Incident::getMttrMinutes)
                .average()
                .orElse(0.0);
    }

    private void calculateMTTR(Incident incident) {
        if (incident.getStartedAt() != null && incident.getResolvedAt() != null) {
            Duration duration = Duration.between(incident.getStartedAt(), incident.getResolvedAt());
            incident.setMttrMinutes(duration.toMinutes());
        }
    }

    private long countBySeverity(List<Incident> incidents, IncidentSeverity severity) {
        return incidents.stream()
                .filter(i -> i.getSeverity() == severity)
                .count();
    }

    public record IncidentMetrics(
            long totalIncidents,
            long resolvedIncidents,
            long openIncidents,
            double averageMTTRMinutes,
            Map<IncidentSeverity, Long> bySeverity
    ) {}
}
