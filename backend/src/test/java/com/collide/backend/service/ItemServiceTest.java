package com.collide.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.collide.backend.dto.CommentDto;
import com.collide.backend.dto.ItemDetailDto;
import com.collide.backend.dto.ItemSummaryDto;
import com.collide.backend.dto.UserDto;
import com.collide.backend.dto.request.CommentRequest;
import com.collide.backend.dto.request.ItemRequest;
import com.collide.backend.exception.ForbiddenException;
import com.collide.backend.exception.NotFoundException;
import com.collide.backend.model.entity.AppUser;
import com.collide.backend.model.entity.Category;
import com.collide.backend.model.entity.CollectionEntity;
import com.collide.backend.model.entity.FavoriteItem;
import com.collide.backend.model.entity.Item;
import com.collide.backend.model.entity.ItemComment;
import com.collide.backend.model.entity.ItemLike;
import com.collide.backend.model.enums.ItemStatus;
import com.collide.backend.model.enums.UserRole;
import com.collide.backend.model.id.FavoriteItemId;
import com.collide.backend.model.id.ItemLikeId;
import com.collide.backend.repository.CollectionRepository;
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
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;
    @Mock
    private ItemLikeRepository likeRepository;
    @Mock
    private ItemCommentRepository commentRepository;
    @Mock
    private FavoriteItemRepository favoriteRepository;
    @Mock
    private CollectionRepository collectionRepository;
    @Mock
    private CategoryService categoryService;
    @Mock
    private UserService userService;
    @Mock
    private DtoMapper mapper;

    private ItemService service;

    @BeforeEach
    void setUp() {
        service = new ItemService(itemRepository, likeRepository, commentRepository, favoriteRepository, collectionRepository, categoryService, userService, mapper);
    }

    @Test
    void listFiltersAndSortsByTitleAlias() {
        UUID ownerId = UUID.randomUUID();
        UUID collectionId = UUID.randomUUID();
        Category category = new Category();
        category.setSlug("books");

        Item includedA = item(ownerId, "alpha");
        includedA.setCollection(collection(collectionId, ownerId));
        includedA.setCategory(category);

        Item includedB = item(ownerId, "beta");
        includedB.setCollection(collection(collectionId, ownerId));
        includedB.setCategory(category);

        Item excluded = item(UUID.randomUUID(), "zzz");
        excluded.setCollection(collection(UUID.randomUUID(), ownerId));

        when(itemRepository.findAll()).thenReturn(List.of(includedB, excluded, includedA));
        when(mapper.itemSummary(any(Item.class), anyLong(), anyLong(), anyBoolean(), anyBoolean()))
                .thenAnswer(invocation -> {
                    Item i = invocation.getArgument(0);
                    return summary(i.getTitle());
                });

        List<ItemSummaryDto> result = service.list(ownerId, collectionId, "books", "a", "title", null);

        assertThat(result).extracting(ItemSummaryDto::title).containsExactly("alpha", "beta");
    }

    @Test
    void detailReturnsItemAndMappedComments() {
        UUID itemId = UUID.randomUUID();
        UUID currentUserId = UUID.randomUUID();
        AppUser owner = user(UUID.randomUUID(), "owner");
        Item item = item(owner.getId(), "title");
        item.setId(itemId);
        item.setOwner(owner);
        item.setUpdatedAt(OffsetDateTime.now());

        ItemComment comment = new ItemComment();
        comment.setId(UUID.randomUUID());
        comment.setAuthor(owner);
        comment.setBody("Nice");
        comment.setCreatedAt(OffsetDateTime.now());
        CommentDto commentDto = new CommentDto(comment.getId(), owner.getId(), owner.getDisplayName(), "@owner", "Nice", comment.getCreatedAt());
        UserDto ownerDto = new UserDto(owner.getId(), "owner", "@owner", "Owner", "Owner", "", null, "orange", "online", false);
        ItemSummaryDto summary = summary("title");

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(commentRepository.findByItemIdOrderByCreatedAtAsc(itemId)).thenReturn(List.of(comment));
        when(mapper.itemComment(comment)).thenReturn(commentDto);
        when(mapper.itemSummary(any(Item.class), anyLong(), anyLong(), anyBoolean(), anyBoolean())).thenReturn(summary);
        when(mapper.user(owner, false)).thenReturn(ownerDto);

        ItemDetailDto result = service.detail(itemId, currentUserId);

        assertThat(result.item()).isSameAs(summary);
        assertThat(result.owner()).isSameAs(ownerDto);
        assertThat(result.comments()).containsExactly(commentDto);
        assertThat(result.updatedAt()).isEqualTo(item.getUpdatedAt());
    }

    @Test
    void createParsesPriceAndSetsDerivedFields() {
        UUID ownerId = UUID.randomUUID();
        AppUser owner = user(ownerId, "owner");
        Category category = new Category();
        category.setSlug("coins");
        ItemSummaryDto summary = summary("Item");
        UserDto ownerDto = new UserDto(ownerId, "owner", "@owner", "Owner", "Owner", "", null, "orange", "online", false);

        when(userService.find(ownerId)).thenReturn(owner);
        when(mapper.fromUiStatus("sale")).thenReturn(ItemStatus.FOR_SALE);
        when(categoryService.getBySlugOrNull("coins")).thenReturn(category);
        final Item[] savedRef = new Item[1];
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> {
            Item saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            savedRef[0] = saved;
            return saved;
        });
        when(itemRepository.findById(any(UUID.class))).thenAnswer(invocation -> Optional.of(savedRef[0]));
        when(commentRepository.findByItemIdOrderByCreatedAtAsc(any(UUID.class))).thenReturn(List.of());
        when(mapper.itemSummary(any(Item.class), anyLong(), anyLong(), anyBoolean(), anyBoolean())).thenReturn(summary);
        when(mapper.user(owner, false)).thenReturn(ownerDto);

        ItemRequest request = new ItemRequest(
                "  Item  ",
                null,
                null,
                "coins",
                "sale",
                null,
                "1 234,50 руб",
                "  Description used as short  ",
                "",
                "full",
                "img"
        );

        ItemDetailDto result = service.create(ownerId, request);

        ArgumentCaptor<Item> captor = ArgumentCaptor.forClass(Item.class);
        verify(itemRepository).save(captor.capture());
        Item saved = captor.getValue();
        assertThat(saved.getTitle()).isEqualTo("Item");
        assertThat(saved.getShortDescription()).isEqualTo("Description used as short");
        assertThat(saved.getStatus()).isEqualTo(ItemStatus.FOR_SALE);
        assertThat(saved.getPriceAmount()).isEqualByComparingTo("1234.50");
        assertThat(saved.getCollection()).isNull();
        assertThat(saved.getCategory()).isSameAs(category);
        assertThat(result.item()).isSameAs(summary);
    }

    @Test
    void createThrowsWhenCollectionMissingOrOwnedByAnotherUser() {
        UUID ownerId = UUID.randomUUID();
        AppUser owner = user(ownerId, "owner");
        UUID collectionId = UUID.randomUUID();
        when(userService.find(ownerId)).thenReturn(owner);
        when(mapper.fromUiStatus(nullable(String.class))).thenReturn(ItemStatus.IN_COLLECTION);

        ItemRequest request = new ItemRequest("Item", collectionId, null, null, null, BigDecimal.ONE, null, "d", "s", "full", "img");

        when(collectionRepository.findById(collectionId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.create(ownerId, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Коллекция для предмета не найдена");

        CollectionEntity foreignCollection = collection(collectionId, UUID.randomUUID());
        when(collectionRepository.findById(collectionId)).thenReturn(Optional.of(foreignCollection));
        assertThatThrownBy(() -> service.create(ownerId, request))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Нельзя добавить предмет в чужую коллекцию");
    }

    @Test
    void updateAndDeleteRequireOwner() {
        UUID itemId = UUID.randomUUID();
        AppUser owner = user(UUID.randomUUID(), "owner");
        Item item = item(owner.getId(), "title");
        item.setId(itemId);
        item.setOwner(owner);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        ItemRequest request = new ItemRequest("x", null, null, null, null, null, null, "d", "s", "full", "img");
        assertThatThrownBy(() -> service.update(itemId, UUID.randomUUID(), request))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Нельзя редактировать чужой предмет");

        assertThatThrownBy(() -> service.delete(itemId, null))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Нельзя редактировать чужой предмет");
    }

    @Test
    void toggleLikeAndFavoriteSwitchBasedOnExistingRelation() {
        UUID itemId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Item item = item(UUID.randomUUID(), "title");
        item.setId(itemId);
        AppUser user = user(userId, "u");

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(userService.find(userId)).thenReturn(user);

        ItemLikeId likeId = new ItemLikeId(itemId, userId);
        when(likeRepository.existsById(likeId)).thenReturn(true, false);
        assertThat(service.toggleLike(itemId, userId)).isFalse();
        assertThat(service.toggleLike(itemId, userId)).isTrue();

        FavoriteItemId favoriteId = new FavoriteItemId(userId, itemId);
        when(favoriteRepository.existsById(favoriteId)).thenReturn(true, false);
        assertThat(service.toggleFavorite(itemId, userId)).isFalse();
        assertThat(service.toggleFavorite(itemId, userId)).isTrue();
    }

    @Test
    void commentsAndAddCommentUseMapper() {
        UUID itemId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        AppUser author = user(authorId, "author");
        Item item = item(authorId, "title");
        item.setId(itemId);
        item.setOwner(author);

        ItemComment existing = new ItemComment();
        existing.setId(UUID.randomUUID());
        existing.setAuthor(author);
        existing.setBody("text");
        existing.setCreatedAt(OffsetDateTime.now());
        CommentDto existingDto = new CommentDto(existing.getId(), authorId, "Author", "@author", "text", existing.getCreatedAt());

        when(commentRepository.findByItemIdOrderByCreatedAtAsc(itemId)).thenReturn(List.of(existing));
        when(mapper.itemComment(existing)).thenReturn(existingDto);
        assertThat(service.comments(itemId)).containsExactly(existingDto);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(userService.find(authorId)).thenReturn(author);
        when(commentRepository.save(any(ItemComment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        CommentDto createdDto = new CommentDto(UUID.randomUUID(), authorId, "Author", "@author", "new", OffsetDateTime.now());
        when(mapper.itemComment(any(ItemComment.class))).thenReturn(createdDto);

        CommentDto result = service.addComment(itemId, authorId, new CommentRequest("  new  "));
        assertThat(result).isSameAs(createdDto);
    }

    @Test
    void findThrowsWhenMissing() {
        UUID id = UUID.randomUUID();
        when(itemRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.find(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Предмет не найден");
    }

    @Test
    void listCoversPopularPriceAndDefaultSortBranches() {
        UUID ownerId = UUID.randomUUID();
        Item itemA = item(ownerId, "A");
        itemA.setPriceAmount(new BigDecimal("5"));
        Item itemB = item(ownerId, "B");
        itemB.setPriceAmount(null);
        itemA.setCollection(null);
        itemB.setCollection(collection(UUID.randomUUID(), ownerId));
        itemA.setCategory(null);
        itemB.setCategory(new Category());
        itemB.getCategory().setSlug("coins");

        when(itemRepository.findAll()).thenReturn(List.of(itemA, itemB));
        when(likeRepository.countByIdItemId(itemA.getId())).thenReturn(1L);
        when(likeRepository.countByIdItemId(itemB.getId())).thenReturn(10L);
        when(mapper.itemSummary(any(Item.class), anyLong(), anyLong(), anyBoolean(), anyBoolean()))
                .thenAnswer(invocation -> summary(((Item) invocation.getArgument(0)).getTitle()));

        assertThat(service.list(null, null, "all", " ", "popular", null))
                .extracting(ItemSummaryDto::title).containsExactly("B", "A");
        assertThat(service.list(null, null, null, null, "price", null))
                .extracting(ItemSummaryDto::title).containsExactly("A", "B");
        assertThat(service.list(null, null, "", "", null, null)).hasSize(2);
    }

    @Test
    void summaryCoversCurrentUserBranches() {
        Item item = item(UUID.randomUUID(), "title");
        UUID currentUserId = UUID.randomUUID();

        when(likeRepository.countByIdItemId(item.getId())).thenReturn(3L);
        when(commentRepository.countByItemId(item.getId())).thenReturn(2L);
        when(likeRepository.existsById(new ItemLikeId(item.getId(), currentUserId))).thenReturn(true);
        when(favoriteRepository.existsById(new FavoriteItemId(currentUserId, item.getId()))).thenReturn(true);
        when(mapper.itemSummary(any(Item.class), anyLong(), anyLong(), anyBoolean(), anyBoolean()))
                .thenReturn(summary("title"));

        service.summary(item, currentUserId);
        service.summary(item, null);
    }

    @Test
    void createParsesInvalidPriceAsNullAndUsesCategoryFallback() {
        UUID ownerId = UUID.randomUUID();
        AppUser owner = user(ownerId, "owner");
        Category category = new Category();
        category.setSlug("fallback");
        when(userService.find(ownerId)).thenReturn(owner);
        when(mapper.fromUiStatus(nullable(String.class))).thenReturn(ItemStatus.IN_COLLECTION);
        when(categoryService.getBySlugOrNull("fallback")).thenReturn(category);

        final Item[] savedRef = new Item[1];
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> {
            Item saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            savedRef[0] = saved;
            return saved;
        });
        when(itemRepository.findById(any(UUID.class))).thenAnswer(invocation -> Optional.of(savedRef[0]));
        when(commentRepository.findByItemIdOrderByCreatedAtAsc(any(UUID.class))).thenReturn(List.of());
        when(mapper.itemSummary(any(Item.class), anyLong(), anyLong(), anyBoolean(), anyBoolean())).thenReturn(summary("x"));
        when(mapper.user(any(AppUser.class), anyBoolean())).thenReturn(new UserDto(ownerId, "owner", "@owner", "o", "o", "", null, "orange", "", false));

        ItemRequest request = new ItemRequest("x", null, "fallback", null, null, null, "no-number", " ", " ", "f", "i");
        service.create(ownerId, request);

        assertThat(savedRef[0].getPriceAmount()).isNull();
        assertThat(savedRef[0].getShortDescription()).isNull();
        assertThat(savedRef[0].getCategory()).isSameAs(category);
    }

    @Test
    void createTruncatesShortDescriptionAndSetsOwnedCollection() {
        UUID ownerId = UUID.randomUUID();
        UUID collectionId = UUID.randomUUID();
        AppUser owner = user(ownerId, "owner");
        CollectionEntity collection = collection(collectionId, ownerId);
        String longText = "x".repeat(90);

        when(userService.find(ownerId)).thenReturn(owner);
        when(mapper.fromUiStatus(nullable(String.class))).thenReturn(ItemStatus.IN_COLLECTION);
        when(collectionRepository.findById(collectionId)).thenReturn(Optional.of(collection));

        final Item[] savedRef = new Item[1];
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> {
            Item saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            savedRef[0] = saved;
            return saved;
        });
        when(itemRepository.findById(any(UUID.class))).thenAnswer(invocation -> Optional.of(savedRef[0]));
        when(commentRepository.findByItemIdOrderByCreatedAtAsc(any(UUID.class))).thenReturn(List.of());
        when(mapper.itemSummary(any(Item.class), anyLong(), anyLong(), anyBoolean(), anyBoolean())).thenReturn(summary("x"));
        when(mapper.user(any(AppUser.class), anyBoolean())).thenReturn(new UserDto(ownerId, "owner", "@owner", "o", "o", "", null, "orange", "", false));

        ItemRequest request = new ItemRequest("x", collectionId, null, null, null, BigDecimal.ONE, null, null, longText, "f", "i");
        service.create(ownerId, request);

        assertThat(savedRef[0].getCollection()).isSameAs(collection);
        assertThat(savedRef[0].getShortDescription()).hasSize(60);
    }

    @Test
    void deleteAsOwnerRemovesItem() {
        UUID ownerId = UUID.randomUUID();
        Item item = item(ownerId, "title");
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        service.delete(item.getId(), ownerId);

        verify(itemRepository).delete(item);
    }

    @Test
    void listCoversNegativeFilterPredicates() {
        UUID ownerId = UUID.randomUUID();
        UUID collectionId = UUID.randomUUID();
        Category books = new Category();
        books.setSlug("books");

        Item target = item(ownerId, "Target");
        target.setCollection(collection(collectionId, ownerId));
        target.setCategory(books);

        Item wrongCollection = item(ownerId, "Target");
        wrongCollection.setCollection(collection(UUID.randomUUID(), ownerId));
        wrongCollection.setCategory(books);

        Item wrongCategory = item(ownerId, "Target");
        wrongCategory.setCollection(collection(collectionId, ownerId));
        wrongCategory.setCategory(new Category());
        wrongCategory.getCategory().setSlug("coins");

        Item wrongQuery = item(ownerId, "Other");
        wrongQuery.setCollection(collection(collectionId, ownerId));
        wrongQuery.setCategory(books);

        Item missingCollection = item(ownerId, "Target");
        missingCollection.setCollection(null);
        missingCollection.setCategory(books);

        Item missingCategory = item(ownerId, "Target");
        missingCategory.setCollection(collection(collectionId, ownerId));
        missingCategory.setCategory(null);

        when(itemRepository.findAll()).thenReturn(List.of(target, wrongCollection, wrongCategory, wrongQuery, missingCollection, missingCategory));
        when(mapper.itemSummary(any(Item.class), anyLong(), anyLong(), anyBoolean(), anyBoolean()))
                .thenAnswer(invocation -> summary(((Item) invocation.getArgument(0)).getTitle()));

        List<ItemSummaryDto> result = service.list(ownerId, collectionId, "books", "tar", "title", null);

        assertThat(result).hasSize(1);
    }

    @Test
    void createWithBlankPriceAndNullDescriptionsCoversFallbackBranches() {
        UUID ownerId = UUID.randomUUID();
        AppUser owner = user(ownerId, "owner");

        when(userService.find(ownerId)).thenReturn(owner);
        when(mapper.fromUiStatus(nullable(String.class))).thenReturn(ItemStatus.IN_COLLECTION);
        final Item[] savedRef = new Item[1];
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> {
            Item saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            savedRef[0] = saved;
            return saved;
        });
        when(itemRepository.findById(any(UUID.class))).thenAnswer(invocation -> Optional.of(savedRef[0]));
        when(commentRepository.findByItemIdOrderByCreatedAtAsc(any(UUID.class))).thenReturn(List.of());
        when(mapper.itemSummary(any(Item.class), anyLong(), anyLong(), anyBoolean(), anyBoolean())).thenReturn(summary("x"));
        when(mapper.user(any(AppUser.class), anyBoolean())).thenReturn(new UserDto(ownerId, "owner", "@owner", "o", "o", "", null, "orange", "", false));

        service.create(ownerId, new ItemRequest("x", null, null, null, null, null, " ", null, null, "f", "i"));

        assertThat(savedRef[0].getShortDescription()).isNull();
        assertThat(savedRef[0].getPriceAmount()).isNull();

        service.create(ownerId, new ItemRequest("x", null, null, null, null, null, null, null, null, "f", "i"));
        assertThat(savedRef[0].getPriceAmount()).isNull();
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

    private Item item(UUID ownerId, String title) {
        Item item = new Item();
        item.setId(UUID.randomUUID());
        item.setTitle(title);
        item.setOwner(user(ownerId, "owner"));
        item.setCreatedAt(OffsetDateTime.now());
        return item;
    }

    private CollectionEntity collection(UUID id, UUID ownerId) {
        CollectionEntity collection = new CollectionEntity();
        collection.setId(id);
        collection.setOwner(user(ownerId, "owner"));
        return collection;
    }

    private ItemSummaryDto summary(String title) {
        return new ItemSummaryDto(
                UUID.randomUUID(), UUID.randomUUID(), "owner", null, "collection", title, "", "", "", null,
                "#000000", null, "collection", "В коллекции", null, "0 ₽", 0, 0, false, false, OffsetDateTime.now()
        );
    }
}
