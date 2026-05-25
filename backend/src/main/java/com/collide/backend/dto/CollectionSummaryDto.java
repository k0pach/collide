package com.collide.backend.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record CollectionSummaryDto(
        UUID id,
        UUID ownerId,
        String ownerName,
        String title,
        String description,
        String coverImageUrl,
        String placeholderColor,
        String coverTone,
        CategoryDto category,
        long itemsCount,
        BigDecimal totalValue,
        String totalValueLabel,
        long commentCount,
        OffsetDateTime createdAt
) {}
