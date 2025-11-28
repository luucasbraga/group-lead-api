package com.grouplead.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TeamRequest(
        @NotBlank(message = "Team name is required")
        @Size(max = 255, message = "Team name must be less than 255 characters")
        String name,

        @Size(max = 1000, message = "Description must be less than 1000 characters")
        String description
) {}
