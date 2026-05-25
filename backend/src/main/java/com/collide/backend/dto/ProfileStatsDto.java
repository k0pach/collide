package com.collide.backend.dto;

import java.math.BigDecimal;

public record ProfileStatsDto(
        long collectionsCount,
        long itemsCount,
        long totalItemLikes,
        long followersCount,
        long followingCount,
        BigDecimal totalCollectionsValue,
        String totalCollectionsValueLabel,
        double collectionAverageRating
) {}
