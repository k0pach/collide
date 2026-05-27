package com.collide.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.collide.backend.dto.CollectionSummaryDto;
import com.collide.backend.dto.FavoritesDto;
import com.collide.backend.dto.ItemSummaryDto;
import com.collide.backend.model.entity.CollectionEntity;
import com.collide.backend.model.entity.FavoriteCollection;
import com.collide.backend.model.entity.FavoriteItem;
import com.collide.backend.model.entity.Item;
import com.collide.backend.repository.FavoriteCollectionRepository;
import com.collide.backend.repository.FavoriteItemRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

    @Mock
    private FavoriteItemRepository favoriteItemRepository;

    @Mock
    private FavoriteCollectionRepository favoriteCollectionRepository;

    @Mock
    private ItemService itemService;

    @Mock
    private CollectionService collectionService;

    private FavoriteService service;

    @BeforeEach
    void setUp() {
        service = new FavoriteService(favoriteItemRepository, favoriteCollectionRepository, itemService, collectionService);
    }

    @Test
    void favoritesSortsAlphabeticallyWhenSortIsAlphabet() {
        UUID userId = UUID.randomUUID();
        Item firstItem = item(UUID.randomUUID());
        Item secondItem = item(UUID.randomUUID());
        CollectionEntity firstCollection = collection(UUID.randomUUID());
        CollectionEntity secondCollection = collection(UUID.randomUUID());

        when(favoriteItemRepository.findByIdUserId(userId)).thenReturn(List.of(favoriteItem(firstItem), favoriteItem(secondItem)));
        when(favoriteCollectionRepository.findByIdUserId(userId)).thenReturn(List.of(favoriteCollection(firstCollection), favoriteCollection(secondCollection)));

        when(itemService.summary(firstItem, userId)).thenReturn(itemDto("zeta", 1, time(1)));
        when(itemService.summary(secondItem, userId)).thenReturn(itemDto("alpha", 9, time(2)));
        when(collectionService.summary(firstCollection)).thenReturn(collectionDto("Echo", 3, time(1)));
        when(collectionService.summary(secondCollection)).thenReturn(collectionDto("bravo", 10, time(2)));

        FavoritesDto result = service.favorites(userId, null, "alphabet");

        assertThat(result.items()).extracting(ItemSummaryDto::title).containsExactly("alpha", "zeta");
        assertThat(result.collections()).extracting(CollectionSummaryDto::title).containsExactly("bravo", "Echo");
    }

    @Test
    void favoritesSupportsTitleAliasAndQueryFilter() {
        UUID userId = UUID.randomUUID();
        Item firstItem = item(UUID.randomUUID());
        Item secondItem = item(UUID.randomUUID());
        CollectionEntity firstCollection = collection(UUID.randomUUID());
        CollectionEntity secondCollection = collection(UUID.randomUUID());

        when(favoriteItemRepository.findByIdUserId(userId)).thenReturn(List.of(favoriteItem(firstItem), favoriteItem(secondItem)));
        when(favoriteCollectionRepository.findByIdUserId(userId)).thenReturn(List.of(favoriteCollection(firstCollection), favoriteCollection(secondCollection)));

        when(itemService.summary(firstItem, userId)).thenReturn(itemDto("beta card", 1, time(1)));
        when(itemService.summary(secondItem, userId)).thenReturn(itemDto("alpha set", 9, time(2)));
        when(collectionService.summary(firstCollection)).thenReturn(collectionDto("Beta vault", 3, time(1)));
        when(collectionService.summary(secondCollection)).thenReturn(collectionDto("Alpha shelf", 10, time(2)));

        FavoritesDto result = service.favorites(userId, "  BeTa ", "title");

        assertThat(result.items()).extracting(ItemSummaryDto::title).containsExactly("beta card");
        assertThat(result.collections()).extracting(CollectionSummaryDto::title).containsExactly("Beta vault");
    }

    @Test
    void favoritesSortsByPopularityWhenSortIsPopular() {
        UUID userId = UUID.randomUUID();
        Item firstItem = item(UUID.randomUUID());
        Item secondItem = item(UUID.randomUUID());
        CollectionEntity firstCollection = collection(UUID.randomUUID());
        CollectionEntity secondCollection = collection(UUID.randomUUID());

        when(favoriteItemRepository.findByIdUserId(userId)).thenReturn(List.of(favoriteItem(firstItem), favoriteItem(secondItem)));
        when(favoriteCollectionRepository.findByIdUserId(userId)).thenReturn(List.of(favoriteCollection(firstCollection), favoriteCollection(secondCollection)));

        when(itemService.summary(firstItem, userId)).thenReturn(itemDto("one", 2, time(1)));
        when(itemService.summary(secondItem, userId)).thenReturn(itemDto("two", 10, time(2)));
        when(collectionService.summary(firstCollection)).thenReturn(collectionDto("A", 1, time(1)));
        when(collectionService.summary(secondCollection)).thenReturn(collectionDto("B", 7, time(2)));

        FavoritesDto result = service.favorites(userId, "", "popular");

        assertThat(result.items()).extracting(ItemSummaryDto::likesCount).containsExactly(10L, 2L);
        assertThat(result.collections()).extracting(CollectionSummaryDto::itemsCount).containsExactly(7L, 1L);
    }

    @Test
    void favoritesSortsItemsByLikesAliasAndCollectionsByDefaultSort() {
        UUID userId = UUID.randomUUID();
        Item firstItem = item(UUID.randomUUID());
        Item secondItem = item(UUID.randomUUID());
        CollectionEntity firstCollection = collection(UUID.randomUUID());
        CollectionEntity secondCollection = collection(UUID.randomUUID());

        when(favoriteItemRepository.findByIdUserId(userId)).thenReturn(List.of(favoriteItem(firstItem), favoriteItem(secondItem)));
        when(favoriteCollectionRepository.findByIdUserId(userId)).thenReturn(List.of(favoriteCollection(firstCollection), favoriteCollection(secondCollection)));

        when(itemService.summary(firstItem, userId)).thenReturn(itemDto("one", 4, time(1)));
        when(itemService.summary(secondItem, userId)).thenReturn(itemDto("two", 8, time(2)));
        when(collectionService.summary(firstCollection)).thenReturn(collectionDto("older", 2, time(1)));
        when(collectionService.summary(secondCollection)).thenReturn(collectionDto("newer", 2, time(2)));

        FavoritesDto result = service.favorites(userId, null, "likes");

        assertThat(result.items()).extracting(ItemSummaryDto::likesCount).containsExactly(8L, 4L);
        assertThat(result.collections()).extracting(CollectionSummaryDto::title).containsExactly("newer", "older");
    }

    @Test
    void favoritesUsesDefaultSortWhenSortIsNull() {
        UUID userId = UUID.randomUUID();
        Item firstItem = item(UUID.randomUUID());
        Item secondItem = item(UUID.randomUUID());
        CollectionEntity firstCollection = collection(UUID.randomUUID());
        CollectionEntity secondCollection = collection(UUID.randomUUID());

        when(favoriteItemRepository.findByIdUserId(userId)).thenReturn(List.of(favoriteItem(firstItem), favoriteItem(secondItem)));
        when(favoriteCollectionRepository.findByIdUserId(userId)).thenReturn(List.of(favoriteCollection(firstCollection), favoriteCollection(secondCollection)));

        when(itemService.summary(firstItem, userId)).thenReturn(itemDto("one", 1, time(1)));
        when(itemService.summary(secondItem, userId)).thenReturn(itemDto("two", 1, time(3)));
        when(collectionService.summary(firstCollection)).thenReturn(collectionDto("old", 1, time(2)));
        when(collectionService.summary(secondCollection)).thenReturn(collectionDto("new", 1, time(4)));

        FavoritesDto result = service.favorites(userId, null, null);

        assertThat(result.items()).extracting(ItemSummaryDto::title).containsExactly("two", "one");
        assertThat(result.collections()).extracting(CollectionSummaryDto::title).containsExactly("new", "old");
    }

    private FavoriteItem favoriteItem(Item item) {
        FavoriteItem favoriteItem = new FavoriteItem();
        favoriteItem.setItem(item);
        return favoriteItem;
    }

    private FavoriteCollection favoriteCollection(CollectionEntity collection) {
        FavoriteCollection favoriteCollection = new FavoriteCollection();
        favoriteCollection.setCollection(collection);
        return favoriteCollection;
    }

    private Item item(UUID id) {
        Item item = new Item();
        item.setId(id);
        return item;
    }

    private CollectionEntity collection(UUID id) {
        CollectionEntity collection = new CollectionEntity();
        collection.setId(id);
        return collection;
    }

    private ItemSummaryDto itemDto(String title, long likes, OffsetDateTime createdAt) {
        return new ItemSummaryDto(
                UUID.randomUUID(), UUID.randomUUID(), "owner", null, "collection", title, "", "", "", null,
                "#000000", null, "collection", "В коллекции", null, "0 ₽", likes, 0, false, false, createdAt
        );
    }

    private CollectionSummaryDto collectionDto(String title, long itemsCount, OffsetDateTime createdAt) {
        return new CollectionSummaryDto(
                UUID.randomUUID(), UUID.randomUUID(), "owner", title, "", null, "#000000", "orange", null,
                itemsCount, null, "0 ₽", 0, createdAt
        );
    }

    private OffsetDateTime time(int dayOfMonth) {
        return OffsetDateTime.parse("2026-01-" + (dayOfMonth < 10 ? "0" + dayOfMonth : dayOfMonth) + "T10:00:00+00:00");
    }
}
