package com.grouplead.integration.jira;

import com.grouplead.domain.entity.Sprint;
import com.grouplead.domain.entity.Ticket;
import com.grouplead.domain.enums.SprintStatus;
import com.grouplead.domain.enums.TicketSource;
import com.grouplead.domain.enums.TicketStatus;
import com.grouplead.integration.jira.dto.JiraIssue;
import com.grouplead.integration.jira.dto.JiraSprint;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;

@Component
public class JiraMapper {

    private static final DateTimeFormatter JIRA_DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    private static final DateTimeFormatter JIRA_DATE_FORMAT = DateTimeFormatter.ISO_DATE_TIME;

    public Ticket toTicket(JiraIssue issue) {
        JiraIssue.JiraFields fields = issue.getFields();

        Ticket ticket = Ticket.builder()
                .externalId(issue.getKey())
                .source(TicketSource.JIRA)
                .title(fields.getSummary())
                .description(fields.getDescription())
                .status(mapStatus(fields.getStatus()))
                .priority(fields.getPriority() != null ? fields.getPriority().getName() : null)
                .ticketType(fields.getIssuetype() != null ? fields.getIssuetype().getName() : null)
                .storyPoints(fields.getCustomfield_10016() != null ? fields.getCustomfield_10016().intValue() : null)
                .labels(fields.getLabels() != null ? new HashSet<>(fields.getLabels()) : new HashSet<>())
                .createdAt(parseDateTime(fields.getCreated()))
                .externalUpdatedAt(parseDateTime(fields.getUpdated()))
                .build();

        return ticket;
    }

    public Sprint toSprint(JiraSprint jiraSprint) {
        return Sprint.builder()
                .externalId(String.valueOf(jiraSprint.getId()))
                .name(jiraSprint.getName())
                .goal(jiraSprint.getGoal())
                .startDate(parseDate(jiraSprint.getStartDate()))
                .endDate(parseDate(jiraSprint.getEndDate()))
                .status(mapSprintStatus(jiraSprint.getState()))
                .build();
    }

    private TicketStatus mapStatus(JiraIssue.JiraStatus status) {
        if (status == null || status.getStatusCategory() == null) {
            return TicketStatus.BACKLOG;
        }

        String categoryKey = status.getStatusCategory().getKey();
        String statusName = status.getName().toLowerCase();

        return switch (categoryKey) {
            case "done" -> TicketStatus.DONE;
            case "indeterminate" -> {
                if (statusName.contains("review")) yield TicketStatus.IN_REVIEW;
                if (statusName.contains("testing") || statusName.contains("qa")) yield TicketStatus.TESTING;
                if (statusName.contains("blocked")) yield TicketStatus.BLOCKED;
                yield TicketStatus.IN_PROGRESS;
            }
            default -> {
                if (statusName.contains("todo") || statusName.contains("to do")) yield TicketStatus.TODO;
                yield TicketStatus.BACKLOG;
            }
        };
    }

    private SprintStatus mapSprintStatus(String state) {
        if (state == null) return SprintStatus.PLANNED;

        return switch (state.toLowerCase()) {
            case "active" -> SprintStatus.ACTIVE;
            case "closed" -> SprintStatus.COMPLETED;
            case "future" -> SprintStatus.PLANNED;
            default -> SprintStatus.PLANNED;
        };
    }

    private LocalDateTime parseDateTime(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateString, JIRA_DATE_FORMAT);
        } catch (Exception e) {
            try {
                return LocalDateTime.parse(dateString.substring(0, 19));
            } catch (Exception ex) {
                return null;
            }
        }
    }

    private LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateString.substring(0, 10));
        } catch (Exception e) {
            return null;
        }
    }
}
