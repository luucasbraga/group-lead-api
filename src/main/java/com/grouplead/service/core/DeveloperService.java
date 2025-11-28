package com.grouplead.service.core;

import com.grouplead.domain.entity.Developer;
import com.grouplead.domain.entity.Team;
import com.grouplead.domain.enums.PeriodType;
import com.grouplead.domain.vo.DateRange;
import com.grouplead.dto.request.DeveloperRequest;
import com.grouplead.dto.response.DeveloperMetricsResponse;
import com.grouplead.dto.response.DeveloperResponse;
import com.grouplead.exception.ResourceNotFoundException;
import com.grouplead.repository.CommitRepository;
import com.grouplead.repository.DeveloperRepository;
import com.grouplead.repository.MergeRequestRepository;
import com.grouplead.repository.TeamRepository;
import com.grouplead.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeveloperService {

    private final DeveloperRepository developerRepository;
    private final TeamRepository teamRepository;
    private final TicketRepository ticketRepository;
    private final CommitRepository commitRepository;
    private final MergeRequestRepository mergeRequestRepository;

    public List<DeveloperResponse> getAllDevelopers() {
        return developerRepository.findByActiveTrue().stream()
                .map(DeveloperResponse::from)
                .toList();
    }

    public DeveloperResponse getDeveloperById(Long id) {
        return developerRepository.findById(id)
                .map(DeveloperResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Developer not found: " + id));
    }

    public List<DeveloperResponse> getDevelopersByTeam(Long teamId) {
        return developerRepository.findActiveByTeamId(teamId).stream()
                .map(DeveloperResponse::from)
                .toList();
    }

    @Transactional
    public DeveloperResponse createDeveloper(DeveloperRequest request) {
        log.info("Creating developer: {}", request.email());

        if (developerRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Developer with email already exists: " + request.email());
        }

        Team team = teamRepository.findById(request.teamId())
                .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + request.teamId()));

        Developer developer = Developer.builder()
                .name(request.name())
                .email(request.email())
                .team(team)
                .role(request.role())
                .avatarUrl(request.avatarUrl())
                .externalIds(request.externalIds() != null ? request.externalIds() : new java.util.HashMap<>())
                .build();

        developer = developerRepository.save(developer);
        return DeveloperResponse.from(developer);
    }

    @Transactional
    public DeveloperResponse updateDeveloper(Long id, DeveloperRequest request) {
        log.info("Updating developer: {}", id);

        Developer developer = developerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Developer not found: " + id));

        Team team = teamRepository.findById(request.teamId())
                .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + request.teamId()));

        developer.setName(request.name());
        developer.setEmail(request.email());
        developer.setTeam(team);
        developer.setRole(request.role());
        developer.setAvatarUrl(request.avatarUrl());

        if (request.externalIds() != null) {
            developer.setExternalIds(request.externalIds());
        }

        developer = developerRepository.save(developer);
        return DeveloperResponse.from(developer);
    }

    @Transactional
    public void deleteDeveloper(Long id) {
        log.info("Deactivating developer: {}", id);

        Developer developer = developerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Developer not found: " + id));

        developer.setActive(false);
        developerRepository.save(developer);
    }

    public DeveloperMetricsResponse getMetrics(Long developerId, PeriodType period) {
        Developer developer = developerRepository.findById(developerId)
                .orElseThrow(() -> new ResourceNotFoundException("Developer not found: " + developerId));

        DateRange range = getDateRangeForPeriod(period);

        // Tickets
        var completedTickets = ticketRepository.findCompletedByDeveloperInPeriod(
                developerId, range.start(), range.end());
        int ticketsCompleted = completedTickets.size();
        int storyPointsCompleted = completedTickets.stream()
                .filter(t -> t.getStoryPoints() != null)
                .mapToInt(t -> t.getStoryPoints())
                .sum();

        // Commits
        long commits = commitRepository.countByDeveloperIdInPeriod(developerId, range.start(), range.end());
        int linesAdded = commitRepository.sumAdditionsByDeveloperIdInPeriod(developerId, range.start(), range.end());
        int linesDeleted = commitRepository.sumDeletionsByDeveloperIdInPeriod(developerId, range.start(), range.end());

        // Average cycle time
        Double avgCycleTime = completedTickets.stream()
                .filter(t -> t.getCycleTimeHours() != null)
                .mapToLong(t -> t.getCycleTimeHours())
                .average()
                .orElse(0);

        // Merge requests
        long prReviews = mergeRequestRepository.countMergedByDeveloperInPeriod(developerId, range.start(), range.end());

        return new DeveloperMetricsResponse(
                developerId,
                developer.getName(),
                period.name(),
                new DeveloperMetricsResponse.ProductivityMetrics(
                        ticketsCompleted,
                        storyPointsCompleted,
                        (int) commits,
                        linesAdded,
                        linesDeleted,
                        avgCycleTime
                ),
                new DeveloperMetricsResponse.QualityMetrics(0, 0, 0.0, 0, 0),
                new DeveloperMetricsResponse.CollaborationMetrics((int) prReviews, 0.0, 0, 0),
                List.of(),
                List.of()
        );
    }

    private DateRange getDateRangeForPeriod(PeriodType period) {
        return switch (period) {
            case DAILY -> DateRange.lastDays(1);
            case WEEKLY -> DateRange.lastDays(7);
            case BIWEEKLY -> DateRange.lastDays(14);
            case MONTHLY -> DateRange.lastDays(30);
            case QUARTERLY -> DateRange.lastDays(90);
            case YEARLY -> DateRange.lastDays(365);
            default -> DateRange.lastDays(7);
        };
    }
}
