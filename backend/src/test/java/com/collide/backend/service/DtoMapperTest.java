package com.collide.backend.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.collide.backend.dto.CategoryDto;
import com.collide.backend.dto.CollectionDetailDto;
import com.collide.backend.dto.CollectionSummaryDto;
import com.collide.backend.dto.CommentDto;
import com.collide.backend.dto.ItemSummaryDto;
import com.collide.backend.dto.MessageDto;
import com.collide.backend.dto.UserDto;
import com.collide.backend.model.entity.AppUser;
import com.collide.backend.model.entity.Category;
import com.collide.backend.model.entity.Chat;
import com.collide.backend.model.entity.CollectionComment;
import com.collide.backend.model.entity.CollectionEntity;
import com.collide.backend.model.entity.Item;
import com.collide.backend.model.entity.ItemComment;
import com.collide.backend.model.entity.Message;
import com.collide.backend.model.enums.ItemStatus;
import com.collide.backend.model.enums.UserRole;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DtoMapperTest {

    private DtoMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new DtoMapper();
    }

    @Test
    void categoryMapsAndHandlesNull() {
        assertThat(mapper.category(null)).isNull();

        Category category = new Category();
        UUID id = UUID.randomUUID();
        category.setId(id);
        category.setSlug("books");
        category.setTitle("Books");
        category.setSortOrder(3);

        CategoryDto dto = mapper.category(category);

        assertThat(dto.uuid()).isEqualTo(id);
        assertThat(dto.id()).isEqualTo("books");
        assertThat(dto.slug()).isEqualTo("books");
        assertThat(dto.title()).isEqualTo("Books");
        assertThat(dto.sortOrder()).isEqualTo(3);
    }

    @Test
    void userMapsAndHandlesFallbacks() {
        assertThat(mapper.user(null, false)).isNull();

        AppUser user = user("jane", "Jane");
        user.setBio(null);
        user.setStatusMessage(" ");

        UserDto dto = mapper.user(user, true);

        assertThat(dto.id()).isEqualTo(user.getId());
        assertThat(dto.username()).isEqualTo("jane");
        assertThat(dto.handle()).isEqualTo("@jane");
        assertThat(dto.name()).isEqualTo("Jane");
        assertThat(dto.displayName()).isEqualTo("Jane");
        assertThat(dto.about()).isEmpty();
        assertThat(dto.avatarUrl()).isEqualTo("https://img");
        assertThat(dto.status()).isEqualTo("Заходил недавно");
        assertThat(dto.following()).isTrue();
        assertThat(dto.avatarTone()).isEqualTo(mapper.coverTone(user.getId()));

        AppUser userWithNullStatus = user("john", "John");
        userWithNullStatus.setStatusMessage(null);
        assertThat(mapper.user(userWithNullStatus, false).status()).isEqualTo("Заходил недавно");

        AppUser userWithCustomStatus = user("kate", "Kate");
        userWithCustomStatus.setStatusMessage("Была в сети недавно");
        assertThat(mapper.user(userWithCustomStatus, false).status()).isEqualTo("Была в сети недавно");
    }

    @Test
    void collectionSummaryAndDetailMapExpectedFields() {
        Category category = category("coins", "Coins", 5);
        AppUser owner = user("owner", "Owner");
        CollectionEntity collection = collection(owner, category, "Vault", "Rare coins");
        BigDecimal totalValue = new BigDecimal("1234.50");

        CollectionSummaryDto summary = mapper.collectionSummary(collection, 9, totalValue, 4);

        assertThat(summary.id()).isEqualTo(collection.getId());
        assertThat(summary.ownerId()).isEqualTo(owner.getId());
        assertThat(summary.ownerName()).isEqualTo("Owner");
        assertThat(summary.title()).isEqualTo("Vault");
        assertThat(summary.description()).isEqualTo("Rare coins");
        assertThat(summary.category().slug()).isEqualTo("coins");
        assertThat(summary.itemsCount()).isEqualTo(9);
        assertThat(summary.totalValue()).isEqualTo(totalValue);
        assertThat(summary.totalValueLabel()).isEqualTo("1 234,5 ₽");
        assertThat(summary.commentCount()).isEqualTo(4);

        ItemSummaryDto item = itemSummary("Item", 1, 2, time(2));
        CollectionDetailDto detail = mapper.collectionDetail(collection, 1, totalValue, 4.44, 3, 2, true, List.of(item));

        assertThat(detail.averageRating()).isEqualTo(4.4);
        assertThat(detail.favorite()).isTrue();
        assertThat(detail.ratingCount()).isEqualTo(3);
        assertThat(detail.items()).containsExactly(item);
    }

    @Test
    void itemSummaryMapsWithAndWithoutCollection() {
        AppUser owner = user("owner", "Owner");
        Category category = category("books", "Books", 1);
        CollectionEntity collection = collection(owner, category, "Shelf", "Books shelf");
        Item itemWithCollection = item(owner, collection, category, "Novel", ItemStatus.FOR_SALE);
        itemWithCollection.setShortDescription(null);
        itemWithCollection.setFullDescription(null);
        itemWithCollection.setPriceAmount(new BigDecimal("1999.90"));

        ItemSummaryDto mappedWithCollection = mapper.itemSummary(itemWithCollection, 5, 7, true, false);

        assertThat(mappedWithCollection.collectionTitle()).isEqualTo("Shelf");
        assertThat(mappedWithCollection.collectionId()).isEqualTo(collection.getId());
        assertThat(mappedWithCollection.description()).isEmpty();
        assertThat(mappedWithCollection.shortDescription()).isEmpty();
        assertThat(mappedWithCollection.fullDescription()).isEmpty();
        assertThat(mappedWithCollection.status()).isEqualTo("sale");
        assertThat(mappedWithCollection.statusLabel()).isEqualTo("В продаже");
        assertThat(mappedWithCollection.price()).isEqualTo("1 999,9 ₽");
        assertThat(mappedWithCollection.likesCount()).isEqualTo(5);
        assertThat(mappedWithCollection.commentsCount()).isEqualTo(7);
        assertThat(mappedWithCollection.liked()).isTrue();
        assertThat(mappedWithCollection.favorite()).isFalse();

        Item itemWithoutCollection = item(owner, null, category, "Loose", null);
        ItemSummaryDto mappedWithoutCollection = mapper.itemSummary(itemWithoutCollection, 0, 0, false, true);

        assertThat(mappedWithoutCollection.collectionId()).isNull();
        assertThat(mappedWithoutCollection.collectionTitle()).isEqualTo("Без коллекции");
        assertThat(mappedWithoutCollection.status()).isEqualTo("collection");
        assertThat(mappedWithoutCollection.statusLabel()).isEqualTo("В коллекции");
    }

    @Test
    void commentAndMessageMappingWorks() {
        AppUser author = user("reader", "Reader");

        ItemComment itemComment = new ItemComment();
        itemComment.setId(UUID.randomUUID());
        itemComment.setAuthor(author);
        itemComment.setBody("Great item");
        itemComment.setCreatedAt(time(1));

        CommentDto itemCommentDto = mapper.itemComment(itemComment);
        assertThat(itemCommentDto.authorHandle()).isEqualTo("@reader");
        assertThat(itemCommentDto.body()).isEqualTo("Great item");

        CollectionComment collectionComment = new CollectionComment();
        collectionComment.setId(UUID.randomUUID());
        collectionComment.setAuthor(author);
        collectionComment.setBody("Nice collection");
        collectionComment.setCreatedAt(time(2));

        CommentDto collectionCommentDto = mapper.collectionComment(collectionComment);
        assertThat(collectionCommentDto.authorName()).isEqualTo("Reader");
        assertThat(collectionCommentDto.body()).isEqualTo("Nice collection");

        AppUser sender = user("sender", "Sender");
        Chat chat = new Chat();
        UUID chatId = UUID.randomUUID();
        chat.setId(chatId);
        Message message = new Message();
        message.setId(UUID.randomUUID());
        message.setChat(chat);
        message.setSender(sender);
        message.setBody("hello");
        message.setCreatedAt(time(3));

        MessageDto mine = mapper.message(message, sender.getId());
        MessageDto notMine = mapper.message(message, UUID.randomUUID());

        assertThat(mine.chatId()).isEqualTo(chatId);
        assertThat(mine.mine()).isTrue();
        assertThat(notMine.mine()).isFalse();
    }

    @Test
    void uiStatusMappingsCoverAllBranches() {
        assertThat(mapper.toUiStatus(null)).isEqualTo("collection");
        assertThat(mapper.toUiStatus(ItemStatus.IN_COLLECTION)).isEqualTo("collection");
        assertThat(mapper.toUiStatus(ItemStatus.FOR_SALE)).isEqualTo("sale");
        assertThat(mapper.toUiStatus(ItemStatus.FOR_EXCHANGE)).isEqualTo("exchange");
        assertThat(mapper.toUiStatus(ItemStatus.ARCHIVED)).isEqualTo("archive");

        assertThat(mapper.fromUiStatus(null)).isEqualTo(ItemStatus.IN_COLLECTION);
        assertThat(mapper.fromUiStatus(" ")).isEqualTo(ItemStatus.IN_COLLECTION);
        assertThat(mapper.fromUiStatus("sale")).isEqualTo(ItemStatus.FOR_SALE);
        assertThat(mapper.fromUiStatus("FOR_SALE")).isEqualTo(ItemStatus.FOR_SALE);
        assertThat(mapper.fromUiStatus("exchange")).isEqualTo(ItemStatus.FOR_EXCHANGE);
        assertThat(mapper.fromUiStatus("FOR_EXCHANGE")).isEqualTo(ItemStatus.FOR_EXCHANGE);
        assertThat(mapper.fromUiStatus("archive")).isEqualTo(ItemStatus.ARCHIVED);
        assertThat(mapper.fromUiStatus("ARCHIVED")).isEqualTo(ItemStatus.ARCHIVED);
        assertThat(mapper.fromUiStatus("unknown")).isEqualTo(ItemStatus.IN_COLLECTION);
    }

    @Test
    void statusLabelMoneyAndHelpersCoverAllBranches() {
        assertThat(mapper.statusLabel(null)).isEqualTo("В коллекции");
        assertThat(mapper.statusLabel(ItemStatus.IN_COLLECTION)).isEqualTo("В коллекции");
        assertThat(mapper.statusLabel(ItemStatus.FOR_SALE)).isEqualTo("В продаже");
        assertThat(mapper.statusLabel(ItemStatus.FOR_EXCHANGE)).isEqualTo("Для обмена");
        assertThat(mapper.statusLabel(ItemStatus.ARCHIVED)).isEqualTo("В архиве");

        assertThat(mapper.money(null)).isEqualTo("0 ₽");
        assertThat(mapper.money(new BigDecimal("1234567"))).isEqualTo("1 234 567 ₽");
        assertThat(mapper.value(null)).isEqualTo(BigDecimal.ZERO);

        BigDecimal value = new BigDecimal("10.10");
        assertThat(mapper.value(value)).isSameAs(value);

        UUID id = UUID.randomUUID();
        assertThat(mapper.placeholderColor(id)).startsWith("#");
        assertThat(List.of("orange", "red", "brown", "cream")).contains(mapper.coverTone(id));
        assertThat(mapper.avatarTone(id)).isEqualTo(mapper.coverTone(id));
        assertThat(mapper.round(4.46)).isEqualTo(4.5);
    }

    private AppUser user(String username, String displayName) {
        AppUser user = new AppUser();
        user.setId(UUID.randomUUID());
        user.setUsername(username);
        user.setDisplayName(displayName);
        user.setEmail(username + "@mail.test");
        user.setPasswordHash("hash");
        user.setRole(UserRole.USER);
        user.setEnabled(true);
        user.setAvatarUrl("https://img");
        user.setStatusMessage("Online");
        return user;
    }

    private Category category(String slug, String title, int sortOrder) {
        Category category = new Category();
        category.setId(UUID.randomUUID());
        category.setSlug(slug);
        category.setTitle(title);
        category.setSortOrder(sortOrder);
        return category;
    }

    private CollectionEntity collection(AppUser owner, Category category, String title, String description) {
        CollectionEntity collection = new CollectionEntity();
        collection.setId(UUID.randomUUID());
        collection.setOwner(owner);
        collection.setCategory(category);
        collection.setTitle(title);
        collection.setDescription(description);
        collection.setCoverImageUrl("https://cover");
        collection.setCreatedAt(time(1));
        return collection;
    }

    private Item item(AppUser owner, CollectionEntity collection, Category category, String title, ItemStatus status) {
        Item item = new Item();
        item.setId(UUID.randomUUID());
        item.setOwner(owner);
        item.setCollection(collection);
        item.setCategory(category);
        item.setTitle(title);
        item.setShortDescription("Short");
        item.setFullDescription("Full");
        item.setImageUrl("https://item");
        item.setStatus(status);
        item.setCreatedAt(time(2));
        return item;
    }

    private ItemSummaryDto itemSummary(String title, long likes, long comments, OffsetDateTime createdAt) {
        return new ItemSummaryDto(
                UUID.randomUUID(), UUID.randomUUID(), "Owner", null, "Collection", title, "", "", "", null,
                "#000000", null, "collection", "В коллекции", null, "0 ₽", likes, comments, false, false, createdAt
        );
    }

    private OffsetDateTime time(int day) {
        return OffsetDateTime.parse("2026-02-" + (day < 10 ? "0" + day : day) + "T09:00:00+00:00");
    }
}
