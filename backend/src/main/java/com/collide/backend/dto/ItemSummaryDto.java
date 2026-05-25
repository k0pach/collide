package com.collide.backend.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ItemSummaryDto(
        UUID id,
        UUID ownerId,
        String ownerName,
        UUID collectionId,
        String collectionTitle,
        String title,
        String description,
        String shortDescription,
        String fullDescription,
        String imageUrl,
        String placeholderColor,
        CategoryDto category,
        String status,
        String statusLabel,
        BigDecimal priceAmount,
        String price,
        long likesCount,
        long commentsCount,
        boolean liked,
        boolean favorite,
        OffsetDateTime createdAt
) {}
