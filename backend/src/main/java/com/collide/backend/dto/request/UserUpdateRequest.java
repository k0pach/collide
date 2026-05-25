package com.collide.backend.dto.request;

import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
        @Size(max = 80) String displayName,
        @Size(max = 500) String bio,
        String avatarUrl,
        @Size(max = 120) String statusMessage
) {}
