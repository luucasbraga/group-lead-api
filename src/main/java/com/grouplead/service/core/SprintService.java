package com.grouplead.service.core;

import com.grouplead.domain.entity.Sprint;
import com.grouplead.domain.entity.Team;
import com.grouplead.domain.entity.Ticket;
import com.grouplead.domain.enums.SprintStatus;
import com.grouplead.domain.enums.TicketStatus;
import com.grouplead.domain.vo.SprintMetrics;
import com.grouplead.exception.ResourceNotFoundException;
import com.grouplead.repository.SprintRepository;
import com.grouplead.repository.TeamRepository;
import com.grouplead.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class SprintService {

    private static final Logger log = LoggerFactory.getLogger(SprintService.class);

    private final SprintRepository sprintRepository;
    private final TeamRepository teamRepository;
    private final TicketRepository ticketRepository;

    public SprintService(SprintRepository sprintRepository,
                        TeamRepository teamRepository,
                        TicketRepository ticketRepository) {
        this.sprintRepository = sprintRepository;
        this.teamRepository = teamRepository;
        this.ticketRepository = ticketRepository;
    }

    @Transactional(readOnly = true)
    public Page<Sprint> getSprints(Long teamId, Pageable pageable) {
        if (teamId != null) {
            return sprintRepository.findByTeamIdOrderByStartDateDesc(teamId, pageable);
        }
        return sprintRepository.findAllByOrderByStartDateDesc(pageable);
    }

    @Transactional(readOnly = true)
    public Sprint getSprint(Long sprintId) {
        return sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint", sprintId));
    }

    @Transactional(readOnly = true)
    public Optional<Sprint> getCurrentSprint(Long teamId) {
        return sprintRepository.findByTeamIdAndStatus(teamId, SprintStatus.ACTIVE)
                .stream()
                .findFirst();
    }

    @Transactional(readOnly = true)
    public List<Sprint> getRecentSprints(Long teamId, int count) {
        return sprintRepository.findTopByTeamIdOrderByEndDateDesc(teamId, Pageable.ofSize(count));
    }

    @Transactional
    public Sprint createSprint(Long teamId, String name, String externalId,
                               LocalDate startDate, LocalDate endDate, String goal) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", teamId));

        Sprint sprint = Sprint.builder()
                .team(team)
                .name(name)
                .externalId(externalId)
                .startDate(startDate)
                .endDate(endDate)
                .goal(goal)
                .status(SprintStatus.PLANNED)
                .build();

        Sprint saved = sprintRepository.save(sprint);
        log.info("Created sprint: {} - {}", saved.getId(), saved.getName());

        return saved;
    }

    @Transactional
    public Sprint startSprint(Long sprintId) {
        Sprint sprint = getSprint(sprintId);

        if (sprint.getStatus() != SprintStatus.PLANNED) {
            throw new IllegalStateException("Sprint must be in PLANNED status to start");
        }

        // Calculate committed points from tickets
        int committedPoints = calculateCommittedPoints(sprintId);
        sprint.setCommittedPoints(committedPoints);
        sprint.setStatus(SprintStatus.ACTIVE);

        if (sprint.getStartDate() == null) {
            sprint.setStartDate(LocalDate.now());
        }

        Sprint saved = sprintRepository.save(sprint);
        log.info("Started sprint: {} with {} committed points", saved.getName(), committedPoints);

        return saved;
    }

    @Transactional
    public Sprint completeSprint(Long sprintId) {
        Sprint sprint = getSprint(sprintId);

        if (sprint.getStatus() != SprintStatus.ACTIVE) {
            throw new IllegalStateException("Sprint must be in ACTIVE status to complete");
        }

        // Calculate completed points
        int completedPoints = calculateCompletedPoints(sprintId);
        sprint.setCompletedPoints(completedPoints);
        sprint.setStatus(SprintStatus.COMPLETED);

        if (sprint.getEndDate() == null) {
            sprint.setEndDate(LocalDate.now());
        }

        Sprint saved = sprintRepository.save(sprint);
        log.info("Completed sprint: {} with {} completed points out of {} committed",
                saved.getName(), completedPoints, saved.getCommittedPoints());

        return saved;
    }

    @Transactional
    public Sprint updateSprintPoints(Long sprintId) {
        Sprint sprint = getSprint(sprintId);

        int committedPoints = calculateCommittedPoints(sprintId);
        int completedPoints = calculateCompletedPoints(sprintId);

        sprint.setCommittedPoints(committedPoints);
        sprint.setCompletedPoints(completedPoints);

        return sprintRepository.save(sprint);
    }

    @Transactional(readOnly = true)
    public SprintMetrics getSprintMetrics(Long sprintId) {
        Sprint sprint = getSprint(sprintId);
        List<Ticket> tickets = ticketRepository.findBySprintId(sprintId);

        int totalTickets = tickets.size();
        int completedTickets = (int) tickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.DONE)
                .count();
        int inProgressTickets = (int) tickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.IN_PROGRESS)
                .count();
        int todoTickets = (int) tickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.TODO || t.getStatus() == TicketStatus.BACKLOG)
                .count();

        int totalPoints = tickets.stream()
                .mapToInt(t -> t.getStoryPoints() != null ? t.getStoryPoints() : 0)
                .sum();
        int completedPoints = tickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.DONE)
                .mapToInt(t -> t.getStoryPoints() != null ? t.getStoryPoints() : 0)
                .sum();

        double completionRate = totalTickets > 0 ?
                (double) completedTickets / totalTickets * 100 : 0;

        // Calculate days remaining
        long daysRemaining = 0;
        long totalDays = 0;
        double progressPercentage = 0;

        if (sprint.getStartDate() != null && sprint.getEndDate() != null) {
            LocalDate today = LocalDate.now();
            totalDays = ChronoUnit.DAYS.between(sprint.getStartDate(), sprint.getEndDate());

            if (today.isBefore(sprint.getEndDate())) {
                daysRemaining = ChronoUnit.DAYS.between(today, sprint.getEndDate());
            }

            long daysElapsed = ChronoUnit.DAYS.between(sprint.getStartDate(), today);
            progressPercentage = totalDays > 0 ?
                    Math.min(100, (double) daysElapsed / totalDays * 100) : 0;
        }

        // Calculate velocity (points per day)
        double velocity = 0;
        if (sprint.getStatus() == SprintStatus.COMPLETED && totalDays > 0) {
            velocity = (double) completedPoints / totalDays;
        } else if (sprint.getStartDate() != null) {
            long daysElapsed = ChronoUnit.DAYS.between(sprint.getStartDate(), LocalDate.now());
            if (daysElapsed > 0) {
                velocity = (double) completedPoints / daysElapsed;
            }
        }

        return SprintMetrics.builder()
                .sprintId(sprintId)
                .sprintName(sprint.getName())
                .status(sprint.getStatus())
                .totalTickets(totalTickets)
                .completedTickets(completedTickets)
                .inProgressTickets(inProgressTickets)
                .todoTickets(todoTickets)
                .totalPoints(totalPoints)
                .completedPoints(completedPoints)
                .committedPoints(sprint.getCommittedPoints() != null ? sprint.getCommittedPoints() : totalPoints)
                .completionRate(completionRate)
                .daysRemaining((int) daysRemaining)
                .totalDays((int) totalDays)
                .progressPercentage(progressPercentage)
                .velocity(velocity)
                .startDate(sprint.getStartDate())
                .endDate(sprint.getEndDate())
                .build();
    }

    @Transactional(readOnly = true)
    public double calculateAverageVelocity(Long teamId, int lastNSprints) {
        List<Sprint> completedSprints = sprintRepository.findByTeamIdAndStatusOrderByEndDateDesc(
                teamId, SprintStatus.COMPLETED, Pageable.ofSize(lastNSprints));

        if (completedSprints.isEmpty()) {
            return 0;
        }

        return completedSprints.stream()
                .mapToInt(s -> s.getCompletedPoints() != null ? s.getCompletedPoints() : 0)
                .average()
                .orElse(0);
    }

    @Transactional(readOnly = true)
    public List<SprintMetrics> getSprintHistory(Long teamId, int lastNSprints) {
        List<Sprint> sprints = sprintRepository.findByTeamIdOrderByEndDateDesc(
                teamId, Pageable.ofSize(lastNSprints));

        return sprints.stream()
                .map(s -> getSprintMetrics(s.getId()))
                .toList();
    }

    private int calculateCommittedPoints(Long sprintId) {
        List<Ticket> tickets = ticketRepository.findBySprintId(sprintId);
        return tickets.stream()
                .mapToInt(t -> t.getStoryPoints() != null ? t.getStoryPoints() : 0)
                .sum();
    }

    private int calculateCompletedPoints(Long sprintId) {
        List<Ticket> tickets = ticketRepository.findBySprintId(sprintId);
        return tickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.DONE)
                .mapToInt(t -> t.getStoryPoints() != null ? t.getStoryPoints() : 0)
                .sum();
    }
}
