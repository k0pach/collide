package com.collide.backend.service;

import com.collide.backend.dto.FavoritesDto;
import com.collide.backend.dto.CollectionSummaryDto;
import com.collide.backend.dto.ItemSummaryDto;
import com.collide.backend.repository.FavoriteCollectionRepository;
import com.collide.backend.repository.FavoriteItemRepository;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FavoriteService {
    private final FavoriteItemRepository favoriteItemRepository;
    private final FavoriteCollectionRepository favoriteCollectionRepository;
    private final ItemService itemService;
    private final CollectionService collectionService;

    public FavoriteService(FavoriteItemRepository favoriteItemRepository, FavoriteCollectionRepository favoriteCollectionRepository, ItemService itemService, CollectionService collectionService) {
        this.favoriteItemRepository = favoriteItemRepository;
        this.favoriteCollectionRepository = favoriteCollectionRepository;
        this.itemService = itemService;
        this.collectionService = collectionService;
    }

    @Transactional(readOnly = true)
    public FavoritesDto favorites(UUID userId, String query, String sort) {
        String q = query == null ? "" : query.trim().toLowerCase();
        List<ItemSummaryDto> items = favoriteItemRepository.findByIdUserId(userId).stream().map(f -> itemService.summary(f.getItem(), userId)).filter(item -> q.isBlank() || item.title().toLowerCase().contains(q)).sorted(itemComparator(sort)).toList();
        List<CollectionSummaryDto> collections = favoriteCollectionRepository.findByIdUserId(userId).stream().map(f -> collectionService.summary(f.getCollection())).filter(c -> q.isBlank() || c.title().toLowerCase().contains(q)).sorted(collectionComparator(sort)).toList();
        return new FavoritesDto(items, collections);
    }

    private Comparator<ItemSummaryDto> itemComparator(String sort) {
        return switch (sort == null ? "new" : sort) {
            case "alphabet", "title" -> Comparator.comparing(i -> i.title().toLowerCase());
            case "popular", "likes" -> Comparator.comparing(ItemSummaryDto::likesCount).reversed();
            default ->
                    Comparator.comparing(ItemSummaryDto::createdAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed();
        };
    }

    private Comparator<CollectionSummaryDto> collectionComparator(String sort) {
        return switch (sort == null ? "new" : sort) {
            case "alphabet", "title" -> Comparator.comparing(c -> c.title().toLowerCase());
            case "popular" -> Comparator.comparing(CollectionSummaryDto::itemsCount).reversed();
            default ->
                    Comparator.comparing(CollectionSummaryDto::createdAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed();
        };
    }
}
