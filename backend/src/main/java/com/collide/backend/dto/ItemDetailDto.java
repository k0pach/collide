package com.collide.backend.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record ItemDetailDto(
        ItemSummaryDto item,
        UUID ownerId,
        UserDto owner,
        List<CommentDto> comments,
        OffsetDateTime updatedAt
) {}
