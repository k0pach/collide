package com.collide.backend.dto;

import java.util.UUID;

public record UserDto(
        UUID id,
        String username,
        String handle,
        String name,
        String displayName,
        String about,
        String avatarUrl,
        String avatarTone,
        String status,
        boolean following
) {}
