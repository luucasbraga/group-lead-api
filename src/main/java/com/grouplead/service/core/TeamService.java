package com.grouplead.service.core;

import com.grouplead.domain.entity.Team;
import com.grouplead.dto.request.TeamRequest;
import com.grouplead.dto.response.TeamResponse;
import com.grouplead.exception.ResourceNotFoundException;
import com.grouplead.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamService {

    private final TeamRepository teamRepository;

    public List<TeamResponse> getAllTeams() {
        return teamRepository.findAll().stream()
                .map(TeamResponse::withoutDevelopers)
                .toList();
    }

    public TeamResponse getTeamById(Long id) {
        return teamRepository.findByIdWithDevelopers(id)
                .map(TeamResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + id));
    }

    @Transactional
    public TeamResponse createTeam(TeamRequest request) {
        log.info("Creating team: {}", request.name());

        Team team = Team.builder()
                .name(request.name())
                .description(request.description())
                .build();

        team = teamRepository.save(team);
        return TeamResponse.from(team);
    }

    @Transactional
    public TeamResponse updateTeam(Long id, TeamRequest request) {
        log.info("Updating team: {}", id);

        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + id));

        team.setName(request.name());
        team.setDescription(request.description());

        team = teamRepository.save(team);
        return TeamResponse.from(team);
    }

    @Transactional
    public void deleteTeam(Long id) {
        log.info("Deleting team: {}", id);

        if (!teamRepository.existsById(id)) {
            throw new ResourceNotFoundException("Team not found: " + id);
        }

        teamRepository.deleteById(id);
    }
}
