package com.grouplead.service.collector;

import com.grouplead.domain.entity.Developer;
import com.grouplead.domain.entity.Sprint;
import com.grouplead.domain.entity.Ticket;
import com.grouplead.domain.enums.TicketSource;
import com.grouplead.domain.enums.TicketStatus;
import com.grouplead.domain.vo.SprintMetrics;
import com.grouplead.integration.jira.JiraClient;
import com.grouplead.integration.jira.JiraMapper;
import com.grouplead.integration.jira.dto.JiraIssue;
import com.grouplead.repository.DeveloperRepository;
import com.grouplead.repository.SprintRepository;
import com.grouplead.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class JiraCollectorService {

    private final JiraClient jiraClient;
    private final JiraMapper jiraMapper;
    private final TicketRepository ticketRepository;
    private final SprintRepository sprintRepository;
    private final DeveloperRepository developerRepository;

    @Transactional
    public CollectionResult collectTickets(LocalDateTime since) {
        log.info("Starting JIRA ticket collection since {}", since);

        var jiraIssues = jiraClient.getUpdatedIssues(since);
        int savedCount = 0;

        for (JiraIssue issue : jiraIssues) {
            try {
                Ticket ticket = jiraMapper.toTicket(issue);

                // Check if ticket exists
                var existingTicket = ticketRepository.findByExternalIdAndSource(
                        ticket.getExternalId(), TicketSource.JIRA);

                if (existingTicket.isPresent()) {
                    // Update existing ticket
                    Ticket existing = existingTicket.get();
                    existing.setTitle(ticket.getTitle());
                    existing.setDescription(ticket.getDescription());
                    existing.setStatus(ticket.getStatus());
                    existing.setPriority(ticket.getPriority());
                    existing.setStoryPoints(ticket.getStoryPoints());
                    existing.setLabels(ticket.getLabels());
                    existing.setExternalUpdatedAt(ticket.getExternalUpdatedAt());

                    // Update status timestamps
                    updateStatusTimestamps(existing, ticket.getStatus());

                    ticketRepository.save(existing);
                } else {
                    // Link to developer if assignee exists
                    if (issue.getFields().getAssignee() != null) {
                        String email = issue.getFields().getAssignee().getEmailAddress();
                        developerRepository.findByEmail(email)
                                .ifPresent(ticket::setDeveloper);
                    }

                    ticketRepository.save(ticket);
                }
                savedCount++;
            } catch (Exception e) {
                log.error("Error processing JIRA issue {}", issue.getKey(), e);
            }
        }

        log.info("Collected {} tickets from JIRA", savedCount);
        return new CollectionResult(TicketSource.JIRA, savedCount);
    }

    @Transactional
    public int collectSprints() {
        log.info("Starting JIRA sprint collection");

        var jiraSprints = jiraClient.getAllSprints();
        int savedCount = 0;

        for (var jiraSprint : jiraSprints) {
            try {
                String externalId = String.valueOf(jiraSprint.getId());

                if (!sprintRepository.existsByExternalId(externalId)) {
                    Sprint sprint = jiraMapper.toSprint(jiraSprint);
                    sprintRepository.save(sprint);
                    savedCount++;
                }
            } catch (Exception e) {
                log.error("Error processing JIRA sprint {}", jiraSprint.getId(), e);
            }
        }

        log.info("Collected {} sprints from JIRA", savedCount);
        return savedCount;
    }

    public SprintMetrics calculateSprintMetrics(String sprintId) {
        var sprint = sprintRepository.findByExternalId(sprintId)
                .orElseThrow(() -> new RuntimeException("Sprint not found: " + sprintId));

        var tickets = ticketRepository.findBySprintId(sprint.getId());

        int totalTickets = tickets.size();
        int completedTickets = (int) tickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.DONE || t.getStatus() == TicketStatus.CLOSED)
                .count();
        int inProgressTickets = (int) tickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.IN_PROGRESS)
                .count();
        int todoTickets = (int) tickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.TODO || t.getStatus() == TicketStatus.BACKLOG)
                .count();
        int blockedTickets = (int) tickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.BLOCKED)
                .count();

        int totalStoryPoints = tickets.stream()
                .filter(t -> t.getStoryPoints() != null)
                .mapToInt(Ticket::getStoryPoints)
                .sum();
        int completedStoryPoints = tickets.stream()
                .filter(t -> t.isCompleted() && t.getStoryPoints() != null)
                .mapToInt(Ticket::getStoryPoints)
                .sum();

        Double averageCycleTime = tickets.stream()
                .filter(t -> t.getCycleTimeHours() != null)
                .mapToLong(Ticket::getCycleTimeHours)
                .average()
                .orElse(0);

        double completionRate = totalTickets > 0 ? (double) completedTickets / totalTickets * 100 : 0;

        return new SprintMetrics(
                sprintId,
                sprint.getName(),
                totalTickets,
                completedTickets,
                inProgressTickets,
                todoTickets,
                blockedTickets,
                totalStoryPoints,
                completedStoryPoints,
                averageCycleTime,
                sprint.getDaysRemaining(),
                completionRate,
                0.0 // velocity trend calculated separately
        );
    }

    private void updateStatusTimestamps(Ticket ticket, TicketStatus newStatus) {
        if (newStatus == TicketStatus.IN_PROGRESS && ticket.getStartedAt() == null) {
            ticket.setStartedAt(LocalDateTime.now());
        }
        if ((newStatus == TicketStatus.DONE || newStatus == TicketStatus.CLOSED)
                && ticket.getCompletedAt() == null) {
            ticket.setCompletedAt(LocalDateTime.now());
        }
    }

    public record CollectionResult(TicketSource source, int count) {}
}
