package com.collide.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.collide.backend.dto.CollectionDetailDto;
import com.collide.backend.dto.CollectionSummaryDto;
import com.collide.backend.dto.CommentDto;
import com.collide.backend.dto.ItemSummaryDto;
import com.collide.backend.dto.request.CollectionRequest;
import com.collide.backend.dto.request.CommentRequest;
import com.collide.backend.dto.request.RatingRequest;
import com.collide.backend.exception.ForbiddenException;
import com.collide.backend.exception.NotFoundException;
import com.collide.backend.model.entity.AppUser;
import com.collide.backend.model.entity.Category;
import com.collide.backend.model.entity.CollectionComment;
import com.collide.backend.model.entity.CollectionEntity;
import com.collide.backend.model.entity.CollectionRating;
import com.collide.backend.model.entity.FavoriteCollection;
import com.collide.backend.model.entity.Item;
import com.collide.backend.model.enums.UserRole;
import com.collide.backend.model.id.CollectionRatingId;
import com.collide.backend.model.id.FavoriteCollectionId;
import com.collide.backend.repository.CollectionCommentRepository;
import com.collide.backend.repository.CollectionRatingRepository;
import com.collide.backend.repository.CollectionRepository;
import com.collide.backend.repository.FavoriteCollectionRepository;
import com.collide.backend.repository.FavoriteItemRepository;
import com.collide.backend.repository.ItemCommentRepository;
import com.collide.backend.repository.ItemLikeRepository;
import com.collide.backend.repository.ItemRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CollectionServiceTest {

    @Mock
    private CollectionRepository collectionRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private CollectionCommentRepository commentRepository;
    @Mock
    private CollectionRatingRepository ratingRepository;
    @Mock
    private FavoriteCollectionRepository favoriteRepository;
    @Mock
    private ItemLikeRepository itemLikeRepository;
    @Mock
    private ItemCommentRepository itemCommentRepository;
    @Mock
    private FavoriteItemRepository favoriteItemRepository;
    @Mock
    private CategoryService categoryService;
    @Mock
    private UserService userService;
    @Mock
    private DtoMapper mapper;

    private CollectionService service;

    @BeforeEach
    void setUp() {
        service = new CollectionService(collectionRepository, itemRepository, commentRepository, ratingRepository, favoriteRepository,
                itemLikeRepository, itemCommentRepository, favoriteItemRepository, categoryService, userService, mapper);
    }

    @Test
    void listFiltersAndSortsByTitleAlias() {
        UUID ownerId = UUID.randomUUID();
        Category category = new Category();
        category.setSlug("books");

        CollectionEntity alpha = collection(ownerId, "alpha", category);
        CollectionEntity beta = collection(ownerId, "beta", category);
        CollectionEntity outsider = collection(UUID.randomUUID(), "zzz", category);

        CollectionSummaryDto alphaDto = summary("alpha");
        CollectionSummaryDto betaDto = summary("beta");

        when(collectionRepository.findAll()).thenReturn(List.of(beta, outsider, alpha));
        when(itemRepository.findByCollectionId(alpha.getId())).thenReturn(List.of());
        when(itemRepository.findByCollectionId(beta.getId())).thenReturn(List.of());
        when(commentRepository.countByCollectionId(alpha.getId())).thenReturn(0L);
        when(commentRepository.countByCollectionId(beta.getId())).thenReturn(0L);
        when(mapper.collectionSummary(alpha, 0, BigDecimal.ZERO, 0)).thenReturn(alphaDto);
        when(mapper.collectionSummary(beta, 0, BigDecimal.ZERO, 0)).thenReturn(betaDto);

        List<CollectionSummaryDto> result = service.list(ownerId, "books", "a", "title", null);

        assertThat(result).containsExactly(alphaDto, betaDto);
    }

    @Test
    void detailBuildsDtoWithFavoriteAndItems() {
        UUID collectionId = UUID.randomUUID();
        UUID currentUserId = UUID.randomUUID();
        CollectionEntity collection = collection(UUID.randomUUID(), "col", new Category());
        collection.setId(collectionId);

        Item first = item(UUID.randomUUID(), "B", new BigDecimal("10"));
        Item second = item(UUID.randomUUID(), "A", null);
        when(collectionRepository.findById(collectionId)).thenReturn(Optional.of(collection));
        when(itemRepository.findByCollectionId(collectionId)).thenReturn(List.of(first, second));
        when(itemLikeRepository.countByIdItemId(any(UUID.class))).thenReturn(2L);
        when(itemCommentRepository.countByItemId(any(UUID.class))).thenReturn(1L);
        when(itemLikeRepository.existsById(any())).thenReturn(false);
        when(favoriteItemRepository.existsById(any())).thenReturn(false);
        when(favoriteRepository.existsById(new FavoriteCollectionId(currentUserId, collectionId))).thenReturn(true);
        when(ratingRepository.averageForCollection(collectionId)).thenReturn(Optional.of(4.5));
        when(ratingRepository.countByIdCollectionId(collectionId)).thenReturn(3L);
        when(commentRepository.countByCollectionId(collectionId)).thenReturn(4L);
        when(mapper.itemSummary(any(Item.class), anyLong(), anyLong(), anyBoolean(), anyBoolean())).thenAnswer(invocation -> {
            Item i = invocation.getArgument(0);
            return itemSummary(i.getTitle());
        });
        CollectionDetailDto dto = new CollectionDetailDto(collectionId, collection.getOwner().getId(), collection.getOwner().getDisplayName(),
                "col", "", null, "#000", "orange", null, 2, new BigDecimal("10"), "10 ₽", 4.5, 3, 4, true,
                OffsetDateTime.now(), List.of(itemSummary("A"), itemSummary("B")));
        when(mapper.collectionDetail(any(CollectionEntity.class), anyLong(), any(BigDecimal.class), anyDouble(), anyLong(), anyLong(),
                anyBoolean(), any(List.class))).thenReturn(dto);

        CollectionDetailDto result = service.detail(collectionId, currentUserId, "alphabet");

        assertThat(result).isSameAs(dto);
    }

    @Test
    void createAndUpdateApplyFieldsAndRequireOwner() {
        UUID ownerId = UUID.randomUUID();
        AppUser owner = user(ownerId, "owner");
        Category category = new Category();
        CollectionRequest request = new CollectionRequest("  Title ", "desc", null, "books", "cover");

        when(userService.find(ownerId)).thenReturn(owner);
        when(categoryService.getBySlugOrNull("books")).thenReturn(category);

        final CollectionEntity[] savedRef = new CollectionEntity[1];
        when(collectionRepository.save(any(CollectionEntity.class))).thenAnswer(invocation -> {
            CollectionEntity saved = invocation.getArgument(0);
            if (saved.getId() == null) {
                saved.setId(UUID.randomUUID());
            }
            savedRef[0] = saved;
            return saved;
        });
        when(collectionRepository.findById(any(UUID.class))).thenAnswer(invocation -> Optional.of(savedRef[0]));
        when(itemRepository.findByCollectionId(any(UUID.class))).thenReturn(List.of());
        when(ratingRepository.averageForCollection(any(UUID.class))).thenReturn(Optional.of(0.0));
        when(ratingRepository.countByIdCollectionId(any(UUID.class))).thenReturn(0L);
        when(commentRepository.countByCollectionId(any(UUID.class))).thenReturn(0L);
        when(mapper.collectionDetail(any(), anyLong(), any(BigDecimal.class), anyDouble(), anyLong(), anyLong(), anyBoolean(), any(List.class)))
                .thenReturn(new CollectionDetailDto(UUID.randomUUID(), ownerId, "owner", "Title", "desc", "cover", "#0", "orange", null,
                        0, BigDecimal.ZERO, "0 ₽", 0.0, 0, 0, false, OffsetDateTime.now(), List.of()));

        service.create(ownerId, request);

        ArgumentCaptor<CollectionEntity> captor = ArgumentCaptor.forClass(CollectionEntity.class);
        verify(collectionRepository).save(captor.capture());
        assertThat(captor.getValue().getTitle()).isEqualTo("Title");
        assertThat(captor.getValue().getCategory()).isSameAs(category);

        CollectionEntity persisted = captor.getValue();
        when(collectionRepository.findById(persisted.getId())).thenReturn(Optional.of(persisted));
        service.update(persisted.getId(), ownerId, request);

        assertThatThrownBy(() -> service.update(persisted.getId(), UUID.randomUUID(), request))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Нельзя редактировать чужую коллекцию");

        assertThatThrownBy(() -> service.delete(persisted.getId(), null))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Нельзя редактировать чужую коллекцию");
    }

    @Test
    void removeItemCoversValidationBranches() {
        UUID ownerId = UUID.randomUUID();
        UUID collectionId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        CollectionEntity collection = collection(ownerId, "title", new Category());
        collection.setId(collectionId);
        Item item = item(ownerId, "item", BigDecimal.ONE);
        item.setId(itemId);
        item.setCollection(collection);
        item.setOwner(user(ownerId, "owner"));

        when(collectionRepository.findById(collectionId)).thenReturn(Optional.of(collection));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(itemRepository.findByCollectionId(collectionId)).thenReturn(List.of());
        when(ratingRepository.averageForCollection(collectionId)).thenReturn(Optional.of(0.0));
        when(ratingRepository.countByIdCollectionId(collectionId)).thenReturn(0L);
        when(commentRepository.countByCollectionId(collectionId)).thenReturn(0L);
        when(mapper.collectionDetail(any(), anyLong(), any(BigDecimal.class), anyDouble(), anyLong(), anyLong(), anyBoolean(), any(List.class)))
                .thenReturn(new CollectionDetailDto(collectionId, ownerId, "owner", "t", "", null, "#0", "orange", null,
                        0, BigDecimal.ZERO, "0 ₽", 0.0, 0, 0, false, OffsetDateTime.now(), List.of()));

        service.removeItem(collectionId, itemId, ownerId);
        assertThat(item.getCollection()).isNull();

        item.setCollection(null);
        assertThatThrownBy(() -> service.removeItem(collectionId, itemId, ownerId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Предмет не найден в этой коллекции");
    }

    @Test
    void rateCreatesOrUpdatesRatingAndReturnsDetail() {
        UUID collectionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CollectionEntity collection = collection(UUID.randomUUID(), "title", new Category());
        collection.setId(collectionId);
        AppUser user = user(userId, "u");

        when(collectionRepository.findById(collectionId)).thenReturn(Optional.of(collection));
        when(userService.find(userId)).thenReturn(user);
        when(ratingRepository.findById(new CollectionRatingId(collectionId, userId))).thenReturn(Optional.of(new CollectionRating()));
        when(itemRepository.findByCollectionId(collectionId)).thenReturn(List.of());
        when(ratingRepository.averageForCollection(collectionId)).thenReturn(Optional.of(0.0));
        when(ratingRepository.countByIdCollectionId(collectionId)).thenReturn(1L);
        when(commentRepository.countByCollectionId(collectionId)).thenReturn(0L);
        when(mapper.collectionDetail(any(), anyLong(), any(BigDecimal.class), anyDouble(), anyLong(), anyLong(), anyBoolean(), any(List.class)))
                .thenReturn(new CollectionDetailDto(collectionId, collection.getOwner().getId(), "owner", "t", "", null, "#0", "orange", null,
                        0, BigDecimal.ZERO, "0 ₽", 0.0, 1, 0, false, OffsetDateTime.now(), List.of()));

        service.rate(collectionId, userId, new RatingRequest((short) 5));

        ArgumentCaptor<CollectionRating> captor = ArgumentCaptor.forClass(CollectionRating.class);
        verify(ratingRepository).save(captor.capture());
        assertThat(captor.getValue().getRating()).isEqualTo((short) 5);
    }

    @Test
    void commentsAddCommentToggleFavoriteAndFind() {
        UUID collectionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CollectionEntity collection = collection(userId, "title", new Category());
        collection.setId(collectionId);

        CollectionComment existing = new CollectionComment();
        existing.setId(UUID.randomUUID());
        existing.setAuthor(user(userId, "user"));
        existing.setBody("text");
        existing.setCreatedAt(OffsetDateTime.now());
        CommentDto dto = new CommentDto(existing.getId(), userId, "owner", "@owner", "text", existing.getCreatedAt());

        when(commentRepository.findByCollectionIdOrderByCreatedAtAsc(collectionId)).thenReturn(List.of(existing));
        when(mapper.collectionComment(existing)).thenReturn(dto);
        assertThat(service.comments(collectionId)).containsExactly(dto);

        when(collectionRepository.findById(collectionId)).thenReturn(Optional.of(collection));
        when(userService.find(userId)).thenReturn(user(userId, "user"));
        when(commentRepository.save(any(CollectionComment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.collectionComment(any(CollectionComment.class))).thenReturn(dto);
        assertThat(service.addComment(collectionId, userId, new CommentRequest("  text  "))).isSameAs(dto);

        FavoriteCollectionId id = new FavoriteCollectionId(userId, collectionId);
        when(favoriteRepository.existsById(id)).thenReturn(true, false);
        assertThat(service.toggleFavorite(collectionId, userId)).isFalse();
        assertThat(service.toggleFavorite(collectionId, userId)).isTrue();

        when(collectionRepository.findById(collectionId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.find(collectionId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Коллекция не найдена");
    }

    @Test
    void listCoversPopularAndDefaultSortingAndBlankFilters() {
        UUID ownerId = UUID.randomUUID();
        CollectionEntity older = collection(ownerId, "A", null);
        older.setCreatedAt(OffsetDateTime.parse("2026-01-01T10:00:00+00:00"));
        CollectionEntity newer = collection(ownerId, "B", null);
        newer.setCreatedAt(OffsetDateTime.parse("2026-01-02T10:00:00+00:00"));

        when(collectionRepository.findAll()).thenReturn(List.of(older, newer));
        when(itemRepository.findByCollectionId(older.getId())).thenReturn(List.of(item(ownerId, "x", BigDecimal.ONE)));
        when(itemRepository.findByCollectionId(newer.getId())).thenReturn(List.of(item(ownerId, "x", BigDecimal.ONE), item(ownerId, "y", BigDecimal.TEN)));
        when(commentRepository.countByCollectionId(any(UUID.class))).thenReturn(0L);
        when(mapper.collectionSummary(any(CollectionEntity.class), anyLong(), any(BigDecimal.class), anyLong()))
                .thenAnswer(invocation -> summary(((CollectionEntity) invocation.getArgument(0)).getTitle()));

        assertThat(service.list(ownerId, "all", " ", "popular", null))
                .extracting(CollectionSummaryDto::title).containsExactly("B", "A");
        assertThat(service.list(ownerId, null, null, null, null))
                .extracting(CollectionSummaryDto::title).containsExactly("B", "A");
    }

    @Test
    void detailWithNullCurrentUserAndPriceSortCoversItemSummaryBranch() {
        UUID collectionId = UUID.randomUUID();
        CollectionEntity collection = collection(UUID.randomUUID(), "col", null);
        collection.setId(collectionId);
        Item priced = item(UUID.randomUUID(), "A", new BigDecimal("10"));
        Item free = item(UUID.randomUUID(), "B", null);

        when(collectionRepository.findById(collectionId)).thenReturn(Optional.of(collection));
        when(itemRepository.findByCollectionId(collectionId)).thenReturn(List.of(free, priced));
        when(itemLikeRepository.countByIdItemId(any(UUID.class))).thenReturn(0L);
        when(itemCommentRepository.countByItemId(any(UUID.class))).thenReturn(0L);
        when(ratingRepository.averageForCollection(collectionId)).thenReturn(Optional.of(0.0));
        when(ratingRepository.countByIdCollectionId(collectionId)).thenReturn(0L);
        when(commentRepository.countByCollectionId(collectionId)).thenReturn(0L);
        when(mapper.itemSummary(any(Item.class), anyLong(), anyLong(), anyBoolean(), anyBoolean()))
                .thenAnswer(invocation -> itemSummary(((Item) invocation.getArgument(0)).getTitle()));
        when(mapper.collectionDetail(any(), anyLong(), any(BigDecimal.class), anyDouble(), anyLong(), anyLong(), anyBoolean(), any(List.class)))
                .thenReturn(new CollectionDetailDto(collectionId, collection.getOwner().getId(), "owner", "t", "", null, "#0", "orange", null,
                        2, new BigDecimal("10"), "10 ₽", 0.0, 0, 0, false, OffsetDateTime.now(), List.of()));

        service.detail(collectionId, null, "price");
    }

    @Test
    void removeItemThrowsForbiddenWhenItemOwnerDiffers() {
        UUID ownerId = UUID.randomUUID();
        UUID collectionId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        CollectionEntity collection = collection(ownerId, "title", new Category());
        collection.setId(collectionId);
        Item item = item(UUID.randomUUID(), "item", BigDecimal.ONE);
        item.setId(itemId);
        item.setCollection(collection);

        when(collectionRepository.findById(collectionId)).thenReturn(Optional.of(collection));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> service.removeItem(collectionId, itemId, ownerId))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Нельзя изменять чужой предмет");
    }

    @Test
    void removeItemThrowsWhenItemMissing() {
        UUID ownerId = UUID.randomUUID();
        UUID collectionId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        CollectionEntity collection = collection(ownerId, "title", new Category());
        collection.setId(collectionId);

        when(collectionRepository.findById(collectionId)).thenReturn(Optional.of(collection));
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.removeItem(collectionId, itemId, ownerId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Предмет не найден");
    }

    @Test
    void removeItemThrowsWhenItemBelongsToAnotherCollection() {
        UUID ownerId = UUID.randomUUID();
        UUID collectionId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        CollectionEntity collection = collection(ownerId, "title", new Category());
        collection.setId(collectionId);

        CollectionEntity otherCollection = collection(ownerId, "other", new Category());
        Item item = item(ownerId, "item", BigDecimal.ONE);
        item.setId(itemId);
        item.setCollection(otherCollection);

        when(collectionRepository.findById(collectionId)).thenReturn(Optional.of(collection));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> service.removeItem(collectionId, itemId, ownerId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Предмет не найден в этой коллекции");
    }

    @Test
    void rateCreatesNewRatingWhenMissing() {
        UUID collectionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CollectionEntity collection = collection(UUID.randomUUID(), "title", new Category());
        collection.setId(collectionId);
        AppUser user = user(userId, "u");

        when(collectionRepository.findById(collectionId)).thenReturn(Optional.of(collection));
        when(userService.find(userId)).thenReturn(user);
        when(ratingRepository.findById(new CollectionRatingId(collectionId, userId))).thenReturn(Optional.empty());
        when(itemRepository.findByCollectionId(collectionId)).thenReturn(List.of());
        when(ratingRepository.averageForCollection(collectionId)).thenReturn(Optional.of(0.0));
        when(ratingRepository.countByIdCollectionId(collectionId)).thenReturn(1L);
        when(commentRepository.countByCollectionId(collectionId)).thenReturn(0L);
        when(mapper.collectionDetail(any(), anyLong(), any(BigDecimal.class), anyDouble(), anyLong(), anyLong(), anyBoolean(), any(List.class)))
                .thenReturn(new CollectionDetailDto(collectionId, collection.getOwner().getId(), "owner", "t", "", null, "#0", "orange", null,
                        0, BigDecimal.ZERO, "0 ₽", 0.0, 1, 0, false, OffsetDateTime.now(), List.of()));

        service.rate(collectionId, userId, new RatingRequest((short) 3));

        ArgumentCaptor<CollectionRating> captor = ArgumentCaptor.forClass(CollectionRating.class);
        verify(ratingRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(new CollectionRatingId(collectionId, userId));
    }

    @Test
    void summaryAndItemSummaryCoverCurrentUserBranches() {
        UUID ownerId = UUID.randomUUID();
        UUID currentUserId = UUID.randomUUID();
        CollectionEntity collection = collection(ownerId, "title", null);
        Item item = item(ownerId, "item", null);

        when(itemRepository.findByCollectionId(collection.getId())).thenReturn(List.of(item));
        when(commentRepository.countByCollectionId(collection.getId())).thenReturn(1L);
        when(mapper.collectionSummary(any(), anyLong(), any(BigDecimal.class), anyLong())).thenReturn(summary("title"));
        when(itemLikeRepository.countByIdItemId(item.getId())).thenReturn(2L);
        when(itemCommentRepository.countByItemId(item.getId())).thenReturn(1L);
        when(itemLikeRepository.existsById(any())).thenReturn(true);
        when(favoriteItemRepository.existsById(any())).thenReturn(true);
        when(mapper.itemSummary(any(), anyLong(), anyLong(), anyBoolean(), anyBoolean())).thenReturn(itemSummary("item"));

        service.summary(collection);
        service.itemSummary(item, currentUserId);
        service.itemSummary(item, null);
    }

    @Test
    void detailSortsItemsByLikesBranch() {
        UUID collectionId = UUID.randomUUID();
        UUID currentUserId = UUID.randomUUID();
        CollectionEntity collection = collection(UUID.randomUUID(), "col", null);
        collection.setId(collectionId);
        Item first = item(UUID.randomUUID(), "A", BigDecimal.ONE);
        Item second = item(UUID.randomUUID(), "B", BigDecimal.ONE);

        when(collectionRepository.findById(collectionId)).thenReturn(Optional.of(collection));
        when(itemRepository.findByCollectionId(collectionId)).thenReturn(List.of(first, second));
        when(itemLikeRepository.countByIdItemId(first.getId())).thenReturn(1L);
        when(itemLikeRepository.countByIdItemId(second.getId())).thenReturn(5L);
        when(itemCommentRepository.countByItemId(any(UUID.class))).thenReturn(0L);
        when(itemLikeRepository.existsById(any())).thenReturn(false);
        when(favoriteItemRepository.existsById(any())).thenReturn(false);
        when(ratingRepository.averageForCollection(collectionId)).thenReturn(Optional.of(0.0));
        when(ratingRepository.countByIdCollectionId(collectionId)).thenReturn(0L);
        when(commentRepository.countByCollectionId(collectionId)).thenReturn(0L);
        when(mapper.itemSummary(any(Item.class), anyLong(), anyLong(), anyBoolean(), anyBoolean()))
                .thenAnswer(invocation -> itemSummary(((Item) invocation.getArgument(0)).getTitle()));
        when(mapper.collectionDetail(any(), anyLong(), any(BigDecimal.class), anyDouble(), anyLong(), anyLong(), anyBoolean(), any(List.class)))
                .thenReturn(new CollectionDetailDto(collectionId, collection.getOwner().getId(), "owner", "t", "", null, "#0", "orange", null,
                        2, BigDecimal.ZERO, "0 ₽", 0.0, 0, 0, false, OffsetDateTime.now(), List.of()));

        service.detail(collectionId, currentUserId, "likes");
    }

    @Test
    void listCoversNegativeFilterPredicates() {
        UUID ownerId = UUID.randomUUID();
        Category books = new Category();
        books.setSlug("books");

        CollectionEntity target = collection(ownerId, "Target", books);
        CollectionEntity wrongOwner = collection(UUID.randomUUID(), "Target", books);
        CollectionEntity wrongCategory = collection(ownerId, "Target", new Category());
        wrongCategory.getCategory().setSlug("coins");
        CollectionEntity wrongQuery = collection(ownerId, "Other", books);
        CollectionEntity missingCategory = collection(ownerId, "Target", null);

        when(collectionRepository.findAll()).thenReturn(List.of(target, wrongOwner, wrongCategory, wrongQuery, missingCategory));
        when(itemRepository.findByCollectionId(any(UUID.class))).thenReturn(List.of());
        when(commentRepository.countByCollectionId(any(UUID.class))).thenReturn(0L);
        when(mapper.collectionSummary(any(CollectionEntity.class), anyLong(), any(BigDecimal.class), anyLong()))
                .thenAnswer(invocation -> summary(((CollectionEntity) invocation.getArgument(0)).getTitle()));

        List<CollectionSummaryDto> result = service.list(ownerId, "books", "tar", "title", null);

        assertThat(result).hasSize(1);

        List<CollectionSummaryDto> withBlankCategory = service.list(null, " ", "tar", "title", null);
        assertThat(withBlankCategory).hasSize(4);
    }

    @Test
    void createAppliesCategoryFallbackFromLegacyField() {
        UUID ownerId = UUID.randomUUID();
        AppUser owner = user(ownerId, "owner");
        Category category = new Category();
        when(userService.find(ownerId)).thenReturn(owner);
        when(categoryService.getBySlugOrNull("legacy")).thenReturn(category);
        final CollectionEntity[] savedRef = new CollectionEntity[1];
        when(collectionRepository.save(any(CollectionEntity.class))).thenAnswer(invocation -> {
            CollectionEntity saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            savedRef[0] = saved;
            return saved;
        });
        when(collectionRepository.findById(any(UUID.class))).thenAnswer(invocation -> Optional.ofNullable(savedRef[0]));
        when(itemRepository.findByCollectionId(any(UUID.class))).thenReturn(List.of());
        when(ratingRepository.averageForCollection(any(UUID.class))).thenReturn(Optional.of(0.0));
        when(ratingRepository.countByIdCollectionId(any(UUID.class))).thenReturn(0L);
        when(commentRepository.countByCollectionId(any(UUID.class))).thenReturn(0L);
        when(mapper.collectionDetail(any(), anyLong(), any(BigDecimal.class), anyDouble(), anyLong(), anyLong(), anyBoolean(), any(List.class)))
                .thenReturn(new CollectionDetailDto(UUID.randomUUID(), ownerId, "owner", "Title", "desc", "cover", "#0", "orange", null,
                        0, BigDecimal.ZERO, "0 ₽", 0.0, 0, 0, false, OffsetDateTime.now(), List.of()));

        service.create(ownerId, new CollectionRequest(" Title ", "desc", "legacy", null, "cover"));

        assertThat(savedRef[0].getCategory()).isSameAs(category);
    }

    private AppUser user(UUID id, String username) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setUsername(username);
        user.setDisplayName("Owner");
        user.setEmail(username + "@mail.test");
        user.setPasswordHash("hash");
        user.setRole(UserRole.USER);
        user.setEnabled(true);
        return user;
    }

    private CollectionEntity collection(UUID ownerId, String title, Category category) {
        CollectionEntity c = new CollectionEntity();
        c.setId(UUID.randomUUID());
        c.setOwner(user(ownerId, "owner"));
        c.setCategory(category);
        c.setTitle(title);
        c.setCreatedAt(OffsetDateTime.now());
        return c;
    }

    private Item item(UUID ownerId, String title, BigDecimal price) {
        Item item = new Item();
        item.setId(UUID.randomUUID());
        item.setOwner(user(ownerId, "owner"));
        item.setTitle(title);
        item.setPriceAmount(price);
        item.setCreatedAt(OffsetDateTime.now());
        return item;
    }

    private CollectionSummaryDto summary(String title) {
        return new CollectionSummaryDto(UUID.randomUUID(), UUID.randomUUID(), "owner", title, "", null, "#000", "orange", null,
                0, BigDecimal.ZERO, "0 ₽", 0, OffsetDateTime.now());
    }

    private ItemSummaryDto itemSummary(String title) {
        return new ItemSummaryDto(UUID.randomUUID(), UUID.randomUUID(), "owner", null, "collection", title, "", "", "", null,
                "#000", null, "collection", "В коллекции", null, "0 ₽", 0, 0, false, false, OffsetDateTime.now());
    }
}
