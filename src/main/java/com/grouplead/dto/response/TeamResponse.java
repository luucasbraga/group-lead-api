package com.grouplead.dto.response;

import com.grouplead.domain.entity.Team;

import java.time.LocalDateTime;
import java.util.List;

public record TeamResponse(
        Long id,
        String name,
        String description,
        int developerCount,
        LocalDateTime createdAt,
        List<DeveloperResponse> developers
) {
    public static TeamResponse from(Team team) {
        return new TeamResponse(
                team.getId(),
                team.getName(),
                team.getDescription(),
                team.getDevelopers().size(),
                team.getCreatedAt(),
                team.getDevelopers().stream()
                        .map(DeveloperResponse::from)
                        .toList()
        );
    }

    public static TeamResponse withoutDevelopers(Team team) {
        return new TeamResponse(
                team.getId(),
                team.getName(),
                team.getDescription(),
                team.getDevelopers().size(),
                team.getCreatedAt(),
                null
        );
    }
}
