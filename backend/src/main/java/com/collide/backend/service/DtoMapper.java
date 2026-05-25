package com.collide.backend.service;

import com.collide.backend.dto.*;
import com.collide.backend.model.entity.*;
import com.collide.backend.model.enums.ItemStatus;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class DtoMapper {
    private static final List<String> COLORS = List.of("#FD3E3E", "#FB8500", "#FFB703", "#F3D5B5", "#BC8A5F", "#A47148");

    public CategoryDto category(Category category) {
        if (category == null) return null;
        return new CategoryDto(category.getId(), category.getSlug(), category.getSlug(), category.getTitle(), category.getSortOrder());
    }

    public UserDto user(AppUser user, boolean following) {
        if (user == null) return null;
        return new UserDto(
                user.getId(),
                user.getUsername(),
                "@" + user.getUsername(),
                user.getDisplayName(),
                user.getDisplayName(),
                safe(user.getBio()),
                user.getAvatarUrl(),
                avatarTone(user.getId()),
                safe(user.getStatusMessage(), "Заходил недавно"),
                following
        );
    }

    public CollectionSummaryDto collectionSummary(CollectionEntity c, long itemsCount, BigDecimal totalValue, long commentCount) {
        return new CollectionSummaryDto(
                c.getId(),
                c.getOwner().getId(),
                c.getOwner().getDisplayName(),
                c.getTitle(),
                safe(c.getDescription()),
                c.getCoverImageUrl(),
                placeholderColor(c.getId()),
                coverTone(c.getId()),
                category(c.getCategory()),
                itemsCount,
                value(totalValue),
                money(totalValue),
                commentCount,
                c.getCreatedAt()
        );
    }

    public CollectionDetailDto collectionDetail(CollectionEntity c, long itemsCount, BigDecimal totalValue, double avgRating,
                                                long ratingCount, long commentCount, boolean favorite, List<ItemSummaryDto> items) {
        return new CollectionDetailDto(
                c.getId(), c.getOwner().getId(), c.getOwner().getDisplayName(), c.getTitle(), safe(c.getDescription()),
                c.getCoverImageUrl(), placeholderColor(c.getId()), coverTone(c.getId()), category(c.getCategory()), itemsCount,
                value(totalValue), money(totalValue), round(avgRating), ratingCount, commentCount, favorite, c.getCreatedAt(), items
        );
    }

    public ItemSummaryDto itemSummary(Item item, long likesCount, long commentsCount, boolean liked, boolean favorite) {
        String collectionTitle = item.getCollection() == null ? "Без коллекции" : item.getCollection().getTitle();
        UUID collectionId = item.getCollection() == null ? null : item.getCollection().getId();
        return new ItemSummaryDto(
                item.getId(), item.getOwner().getId(), item.getOwner().getDisplayName(), collectionId, collectionTitle,
                item.getTitle(), safe(item.getShortDescription()), safe(item.getShortDescription()), safe(item.getFullDescription()),
                item.getImageUrl(), placeholderColor(item.getId()), category(item.getCategory()), toUiStatus(item.getStatus()),
                statusLabel(item.getStatus()), item.getPriceAmount(), money(item.getPriceAmount()), likesCount, commentsCount,
                liked, favorite, item.getCreatedAt()
        );
    }

    public CommentDto itemComment(ItemComment comment) {
        return new CommentDto(comment.getId(), comment.getAuthor().getId(), comment.getAuthor().getDisplayName(),
                "@" + comment.getAuthor().getUsername(), comment.getBody(), comment.getCreatedAt());
    }

    public CommentDto collectionComment(CollectionComment comment) {
        return new CommentDto(comment.getId(), comment.getAuthor().getId(), comment.getAuthor().getDisplayName(),
                "@" + comment.getAuthor().getUsername(), comment.getBody(), comment.getCreatedAt());
    }

    public MessageDto message(Message message, UUID currentUserId) {
        return new MessageDto(message.getId(), message.getChat().getId(), message.getSender().getId(),
                message.getSender().getDisplayName(), message.getBody(), message.getSender().getId().equals(currentUserId),
                message.getCreatedAt());
    }

    public String toUiStatus(ItemStatus status) {
        if (status == null) return "collection";
        return switch (status) {
            case IN_COLLECTION -> "collection";
            case FOR_SALE -> "sale";
            case FOR_EXCHANGE -> "exchange";
            case ARCHIVED -> "archive";
        };
    }

    public ItemStatus fromUiStatus(String status) {
        if (status == null || status.isBlank()) return ItemStatus.IN_COLLECTION;
        return switch (status) {
            case "sale", "FOR_SALE" -> ItemStatus.FOR_SALE;
            case "exchange", "FOR_EXCHANGE" -> ItemStatus.FOR_EXCHANGE;
            case "archive", "ARCHIVED" -> ItemStatus.ARCHIVED;
            default -> ItemStatus.IN_COLLECTION;
        };
    }

    public String statusLabel(ItemStatus status) {
        if (status == null) return "В коллекции";
        return switch (status) {
            case IN_COLLECTION -> "В коллекции";
            case FOR_SALE -> "В продаже";
            case FOR_EXCHANGE -> "Для обмена";
            case ARCHIVED -> "В архиве";
        };
    }

    public String money(BigDecimal value) {
        if (value == null) return "0 ₽";
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.forLanguageTag("ru-RU"));
        symbols.setGroupingSeparator(' ');
        DecimalFormat format = new DecimalFormat("#,##0.##", symbols);
        return format.format(value) + " ₽";
    }

    public BigDecimal value(BigDecimal value) { return value == null ? BigDecimal.ZERO : value; }

    public String placeholderColor(UUID id) {
        int idx = Math.abs(String.valueOf(id).hashCode()) % COLORS.size();
        return COLORS.get(idx);
    }

    public String coverTone(UUID id) {
        String[] tones = {"orange", "red", "brown", "cream"};
        int idx = Math.abs(String.valueOf(id).hashCode()) % tones.length;
        return tones[idx];
    }

    public String avatarTone(UUID id) { return coverTone(id); }
    public double round(double value) { return Math.round(value * 10.0) / 10.0; }
    private String safe(String v) { return v == null ? "" : v; }
    private String safe(String v, String fallback) { return v == null || v.isBlank() ? fallback : v; }
}
