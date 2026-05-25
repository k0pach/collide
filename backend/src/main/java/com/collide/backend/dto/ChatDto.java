package com.collide.backend.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ChatDto(
        UUID id,
        UUID companionId,
        String name,
        String handle,
        String avatarTone,
        String preview,
        int unread,
        OffsetDateTime updatedAt
) {}
