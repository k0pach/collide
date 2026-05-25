package com.collide.backend.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MessageDto(
        UUID id,
        UUID chatId,
        UUID senderId,
        String senderName,
        String body,
        boolean mine,
        OffsetDateTime createdAt
) {}
