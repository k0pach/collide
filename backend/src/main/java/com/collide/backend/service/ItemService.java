package com.collide.backend.service;

import com.collide.backend.dto.*;
import com.collide.backend.dto.request.CommentRequest;
import com.collide.backend.dto.request.ItemRequest;
import com.collide.backend.exception.ForbiddenException;
import com.collide.backend.exception.NotFoundException;
import com.collide.backend.model.entity.*;
import com.collide.backend.model.enums.Visibility;
import com.collide.backend.model.id.FavoriteItemId;
import com.collide.backend.model.id.ItemLikeId;
import com.collide.backend.repository.*;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ItemService {
    private final ItemRepository itemRepository;
    private final ItemLikeRepository likeRepository;
    private final ItemCommentRepository commentRepository;
    private final FavoriteItemRepository favoriteRepository;
    private final CollectionRepository collectionRepository;
    private final CategoryService categoryService;
    private final UserService userService;
    private final DtoMapper mapper;

    public ItemService(ItemRepository itemRepository, ItemLikeRepository likeRepository, ItemCommentRepository commentRepository, FavoriteItemRepository favoriteRepository, CollectionRepository collectionRepository, CategoryService categoryService, UserService userService, DtoMapper mapper) {
        this.itemRepository = itemRepository;
        this.likeRepository = likeRepository;
        this.commentRepository = commentRepository;
        this.favoriteRepository = favoriteRepository;
        this.collectionRepository = collectionRepository;
        this.categoryService = categoryService;
        this.userService = userService;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<ItemSummaryDto> list(UUID ownerId, UUID collectionId, String category, String query, String sort, UUID currentUserId) {
        String normalizedCategory = categoryOrNull(category);
        String normalizedQuery = blankToNull(query);
        List<Item> items = itemRepository.findAll().stream().filter(item -> ownerId == null || item.getOwner().getId().equals(ownerId)).filter(item -> collectionId == null || item.getCollection() != null && item.getCollection().getId().equals(collectionId)).filter(item -> normalizedCategory == null || item.getCategory() != null && item.getCategory().getSlug().equals(normalizedCategory)).filter(item -> normalizedQuery == null || item.getTitle().toLowerCase().contains(normalizedQuery.toLowerCase())).toList();
        return sort(items, sort).stream().map(item -> summary(item, currentUserId)).toList();
    }

    @Transactional(readOnly = true)
    public ItemDetailDto detail(UUID id, UUID currentUserId) {
        Item item = find(id);
        return new ItemDetailDto(summary(item, currentUserId), item.getOwner().getId(), mapper.user(item.getOwner(), false), comments(id), item.getUpdatedAt());
    }

    @Transactional
    public ItemDetailDto create(UUID ownerId, ItemRequest request) {
        Item item = new Item();
        item.setOwner(userService.find(ownerId));
        item.setVisibility(Visibility.PUBLIC);
        apply(item, request, ownerId);
        itemRepository.save(item);
        return detail(item.getId(), ownerId);
    }

    @Transactional
    public ItemDetailDto update(UUID id, UUID currentUserId, ItemRequest request) {
        Item item = find(id);
        ensureOwner(item, currentUserId);
        apply(item, request, currentUserId);
        itemRepository.save(item);
        return detail(item.getId(), currentUserId);
    }

    @Transactional
    public void delete(UUID id, UUID currentUserId) {
        Item item = find(id);
        ensureOwner(item, currentUserId);
        itemRepository.delete(item);
    }

    @Transactional
    public boolean toggleLike(UUID itemId, UUID userId) {
        Item item = find(itemId);
        AppUser user = userService.find(userId);
        ItemLikeId id = new ItemLikeId(itemId, userId);
        if (likeRepository.existsById(id)) {
            likeRepository.deleteById(id);
            return false;
        }
        ItemLike like = new ItemLike();
        like.setId(id);
        like.setItem(item);
        like.setUser(user);
        likeRepository.save(like);
        return true;
    }

    @Transactional
    public boolean toggleFavorite(UUID itemId, UUID userId) {
        Item item = find(itemId);
        AppUser user = userService.find(userId);
        FavoriteItemId id = new FavoriteItemId(userId, itemId);
        if (favoriteRepository.existsById(id)) {
            favoriteRepository.deleteById(id);
            return false;
        }
        FavoriteItem favorite = new FavoriteItem();
        favorite.setId(id);
        favorite.setUser(user);
        favorite.setItem(item);
        favoriteRepository.save(favorite);
        return true;
    }

    @Transactional(readOnly = true)
    public List<CommentDto> comments(UUID itemId) {
        return commentRepository.findByItemIdOrderByCreatedAtAsc(itemId).stream().map(mapper::itemComment).toList();
    }

    @Transactional
    public CommentDto addComment(UUID itemId, UUID authorId, CommentRequest request) {
        Item item = find(itemId);
        ItemComment comment = new ItemComment();
        comment.setItem(item);
        comment.setAuthor(userService.find(authorId));
        comment.setBody(request.body().trim());
        return mapper.itemComment(commentRepository.save(comment));
    }

    public Item find(UUID id) {
        return itemRepository.findById(id).orElseThrow(() -> new NotFoundException("Предмет не найден"));
    }

    public ItemSummaryDto summary(Item item, UUID currentUserId) {
        return mapper.itemSummary(item, likeRepository.countByIdItemId(item.getId()), commentRepository.countByItemId(item.getId()), currentUserId != null && likeRepository.existsById(new ItemLikeId(item.getId(), currentUserId)), currentUserId != null && favoriteRepository.existsById(new FavoriteItemId(currentUserId, item.getId())));
    }

    private void apply(Item item, ItemRequest request, UUID ownerId) {
        item.setTitle(request.title().trim());
        item.setShortDescription(firstNonBlank(request.shortDescription(), request.description()));
        if (item.getShortDescription() != null && item.getShortDescription().length() > 60) {
            item.setShortDescription(item.getShortDescription().substring(0, 60));
        }
        item.setFullDescription(request.fullDescription());
        item.setImageUrl(request.imageUrl());
        item.setStatus(mapper.fromUiStatus(request.status()));
        item.setPriceAmount(request.priceAmount() != null ? request.priceAmount() : parsePrice(request.price()));
        String slug = request.categorySlug() != null ? request.categorySlug() : request.category();
        item.setCategory(categoryService.getBySlugOrNull(slug));
        if (request.collectionId() == null) {
            item.setCollection(null);
        } else {
            CollectionEntity collection = collectionRepository.findById(request.collectionId()).orElseThrow(() -> new NotFoundException("Коллекция для предмета не найдена"));
            if (!collection.getOwner().getId().equals(ownerId)) {
                throw new ForbiddenException("Нельзя добавить предмет в чужую коллекцию");
            }
            item.setCollection(collection);
        }
    }

    private void ensureOwner(Item item, UUID currentUserId) {
        if (currentUserId == null || !item.getOwner().getId().equals(currentUserId)) {
            throw new ForbiddenException("Нельзя редактировать чужой предмет");
        }
    }

    private List<Item> sort(List<Item> list, String sort) {
        Comparator<Item> comparator = switch (sort == null ? "new" : sort) {
            case "alphabet", "title" -> Comparator.comparing(i -> i.getTitle().toLowerCase());
            case "likes", "popular" ->
                    Comparator.comparing((Item i) -> likeRepository.countByIdItemId(i.getId())).reversed();
            case "price" ->
                    Comparator.comparing(i -> i.getPriceAmount() == null ? BigDecimal.ZERO : i.getPriceAmount(), Comparator.reverseOrder());
            default ->
                    Comparator.comparing(Item::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed();
        };
        return list.stream().sorted(comparator).toList();
    }

    private BigDecimal parsePrice(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String normalized = raw.replace(" ", "").replace(",", ".");
        Matcher matcher = Pattern.compile("[0-9]+(\\.[0-9]+)?").matcher(normalized);
        if (!matcher.find()) return null;
        return new BigDecimal(matcher.group());
    }

    private String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) return a.trim();
        if (b != null && !b.isBlank()) return b.trim();
        return null;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String categoryOrNull(String value) {
        return value == null || value.isBlank() || "all".equals(value) ? null : value;
    }
}
