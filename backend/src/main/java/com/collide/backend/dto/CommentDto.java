package com.collide.backend.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CommentDto(
        UUID id,
        UUID authorId,
        String authorName,
        String authorHandle,
        String body,
        OffsetDateTime createdAt
) {}
