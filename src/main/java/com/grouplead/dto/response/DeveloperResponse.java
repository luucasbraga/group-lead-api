package com.grouplead.dto.response;

import com.grouplead.domain.entity.Developer;

import java.time.LocalDateTime;
import java.util.Map;

public record DeveloperResponse(
        Long id,
        String name,
        String email,
        String avatarUrl,
        String role,
        Long teamId,
        String teamName,
        boolean active,
        Map<String, String> externalIds,
        LocalDateTime createdAt
) {
    public static DeveloperResponse from(Developer developer) {
        return new DeveloperResponse(
                developer.getId(),
                developer.getName(),
                developer.getEmail(),
                developer.getAvatarUrl(),
                developer.getRole(),
                developer.getTeam() != null ? developer.getTeam().getId() : null,
                developer.getTeam() != null ? developer.getTeam().getName() : null,
                developer.getActive(),
                developer.getExternalIds(),
                developer.getCreatedAt()
        );
    }
}
