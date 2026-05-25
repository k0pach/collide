package com.collide.backend.dto;

import java.util.List;

public record FavoritesDto(
        List<ItemSummaryDto> items,
        List<CollectionSummaryDto> collections
) {}
