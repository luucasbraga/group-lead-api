package com.grouplead.dto.response;

public record AuthResponse(
        String token,
        String tokenType,
        Long expiresIn,
        UserInfo user
) {
    public record UserInfo(
            Long id,
            String username,
            String email,
            String fullName,
            String role
    ) {}

    public static AuthResponse of(String token, long expiresInSeconds, UserInfo user) {
        return new AuthResponse(token, "Bearer", expiresInSeconds, user);
    }
}
