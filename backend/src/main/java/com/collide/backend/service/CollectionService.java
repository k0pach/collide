package com.collide.backend.service;

import com.collide.backend.dto.*;
import com.collide.backend.dto.request.CollectionRequest;
import com.collide.backend.dto.request.CommentRequest;
import com.collide.backend.dto.request.RatingRequest;
import com.collide.backend.exception.ForbiddenException;
import com.collide.backend.exception.NotFoundException;
import com.collide.backend.model.entity.*;
import com.collide.backend.model.enums.Visibility;
import com.collide.backend.model.id.CollectionRatingId;
import com.collide.backend.model.id.FavoriteCollectionId;
import com.collide.backend.repository.*;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CollectionService {
    private final CollectionRepository collectionRepository;
    private final ItemRepository itemRepository;
    private final CollectionCommentRepository commentRepository;
    private final CollectionRatingRepository ratingRepository;
    private final FavoriteCollectionRepository favoriteRepository;
    private final ItemLikeRepository itemLikeRepository;
    private final ItemCommentRepository itemCommentRepository;
    private final FavoriteItemRepository favoriteItemRepository;
    private final CategoryService categoryService;
    private final UserService userService;
    private final DtoMapper mapper;

    public CollectionService(CollectionRepository collectionRepository, ItemRepository itemRepository,
                             CollectionCommentRepository commentRepository, CollectionRatingRepository ratingRepository,
                             FavoriteCollectionRepository favoriteRepository, ItemLikeRepository itemLikeRepository,
                             ItemCommentRepository itemCommentRepository, FavoriteItemRepository favoriteItemRepository,
                             CategoryService categoryService, UserService userService, DtoMapper mapper) {
        this.collectionRepository = collectionRepository;
        this.itemRepository = itemRepository;
        this.commentRepository = commentRepository;
        this.ratingRepository = ratingRepository;
        this.favoriteRepository = favoriteRepository;
        this.itemLikeRepository = itemLikeRepository;
        this.itemCommentRepository = itemCommentRepository;
        this.favoriteItemRepository = favoriteItemRepository;
        this.categoryService = categoryService;
        this.userService = userService;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<CollectionSummaryDto> list(UUID ownerId, String category, String query, String sort, UUID currentUserId) {
        String normalizedCategory = categoryOrNull(category);
        String normalizedQuery = blankToNull(query);
        List<CollectionEntity> collections = collectionRepository.findAll().stream()
                .filter(collection -> ownerId == null || collection.getOwner().getId().equals(ownerId))
                .filter(collection -> normalizedCategory == null || collection.getCategory() != null && collection.getCategory().getSlug().equals(normalizedCategory))
                .filter(collection -> normalizedQuery == null || collection.getTitle().toLowerCase().contains(normalizedQuery.toLowerCase()))
                .toList();
        return sortCollections(collections, sort).stream().map(this::summary).toList();
    }

    @Transactional(readOnly = true)
    public CollectionDetailDto detail(UUID id, UUID currentUserId, String itemSort) {
        CollectionEntity collection = find(id);
        List<ItemSummaryDto> items = sortItems(itemRepository.findByCollectionId(id), itemSort).stream()
                .map(item -> itemSummary(item, currentUserId))
                .toList();
        boolean favorite = currentUserId != null && favoriteRepository.existsById(new FavoriteCollectionId(currentUserId, id));
        return mapper.collectionDetail(collection, items.size(), totalValue(collection.getId()), averageRating(id),
                ratingRepository.countByIdCollectionId(id), commentRepository.countByCollectionId(id), favorite, items);
    }

    @Transactional
    public CollectionDetailDto create(UUID ownerId, CollectionRequest request) {
        AppUser owner = userService.find(ownerId);
        CollectionEntity collection = new CollectionEntity();
        apply(collection, request);
        collection.setOwner(owner);
        collection.setVisibility(Visibility.PUBLIC);
        collectionRepository.save(collection);
        return detail(collection.getId(), ownerId, null);
    }

    @Transactional
    public CollectionDetailDto update(UUID id, UUID currentUserId, CollectionRequest request) {
        CollectionEntity collection = find(id);
        ensureOwner(collection, currentUserId);
        apply(collection, request);
        collectionRepository.save(collection);
        return detail(collection.getId(), currentUserId, null);
    }

    @Transactional
    public void delete(UUID id, UUID currentUserId) {
        CollectionEntity collection = find(id);
        ensureOwner(collection, currentUserId);
        collectionRepository.delete(collection);
    }

    @Transactional
    public CollectionDetailDto removeItem(UUID collectionId, UUID itemId, UUID currentUserId) {
        CollectionEntity collection = find(collectionId);
        ensureOwner(collection, currentUserId);
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new NotFoundException("Предмет не найден"));
        if (item.getCollection() == null || !item.getCollection().getId().equals(collectionId)) {
            throw new NotFoundException("Предмет не найден в этой коллекции");
        }
        if (!item.getOwner().getId().equals(currentUserId)) throw new ForbiddenException("Нельзя изменять чужой предмет");
        item.setCollection(null);
        itemRepository.save(item);
        return detail(collectionId, currentUserId, null);
    }

    @Transactional
    public CollectionDetailDto rate(UUID id, UUID userId, RatingRequest request) {
        CollectionEntity collection = find(id);
        AppUser user = userService.find(userId);
        CollectionRatingId ratingId = new CollectionRatingId(id, userId);
        CollectionRating rating = ratingRepository.findById(ratingId).orElseGet(CollectionRating::new);
        rating.setId(ratingId);
        rating.setCollection(collection);
        rating.setUser(user);
        rating.setRating(request.rating());
        ratingRepository.save(rating);
        return detail(id, userId, null);
    }

    @Transactional(readOnly = true)
    public List<CommentDto> comments(UUID collectionId) {
        return commentRepository.findByCollectionIdOrderByCreatedAtAsc(collectionId).stream().map(mapper::collectionComment).toList();
    }

    @Transactional
    public CommentDto addComment(UUID collectionId, UUID authorId, CommentRequest request) {
        CollectionEntity collection = find(collectionId);
        CollectionComment comment = new CollectionComment();
        comment.setCollection(collection);
        comment.setAuthor(userService.find(authorId));
        comment.setBody(request.body().trim());
        return mapper.collectionComment(commentRepository.save(comment));
    }

    @Transactional
    public boolean toggleFavorite(UUID collectionId, UUID userId) {
        CollectionEntity collection = find(collectionId);
        AppUser user = userService.find(userId);
        FavoriteCollectionId id = new FavoriteCollectionId(userId, collectionId);
        if (favoriteRepository.existsById(id)) {
            favoriteRepository.deleteById(id);
            return false;
        }
        FavoriteCollection favorite = new FavoriteCollection();
        favorite.setId(id);
        favorite.setUser(user);
        favorite.setCollection(collection);
        favoriteRepository.save(favorite);
        return true;
    }

    public CollectionEntity find(UUID id) {
        return collectionRepository.findById(id).orElseThrow(() -> new NotFoundException("Коллекция не найдена"));
    }

    public CollectionSummaryDto summary(CollectionEntity collection) {
        return mapper.collectionSummary(collection, itemRepository.findByCollectionId(collection.getId()).size(),
                totalValue(collection.getId()), commentRepository.countByCollectionId(collection.getId()));
    }

    public ItemSummaryDto itemSummary(Item item, UUID currentUserId) {
        return mapper.itemSummary(item, itemLikeRepository.countByIdItemId(item.getId()),
                itemCommentRepository.countByItemId(item.getId()),
                currentUserId != null && itemLikeRepository.existsById(new com.collide.backend.model.id.ItemLikeId(item.getId(), currentUserId)),
                currentUserId != null && favoriteItemRepository.existsById(new com.collide.backend.model.id.FavoriteItemId(currentUserId, item.getId())));
    }

    public BigDecimal totalValue(UUID collectionId) {
        return itemRepository.findByCollectionId(collectionId).stream()
                .map(Item::getPriceAmount).filter(v -> v != null).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public double averageRating(UUID collectionId) {
        return ratingRepository.averageForCollection(collectionId).orElse(0.0);
    }

    private void apply(CollectionEntity collection, CollectionRequest request) {
        collection.setTitle(request.title().trim());
        collection.setDescription(request.description());
        collection.setCoverImageUrl(request.coverImageUrl());
        String slug = request.categorySlug() != null ? request.categorySlug() : request.category();
        collection.setCategory(categoryService.getBySlugOrNull(slug));
    }

    private void ensureOwner(CollectionEntity collection, UUID currentUserId) {
        if (currentUserId == null || !collection.getOwner().getId().equals(currentUserId)) {
            throw new ForbiddenException("Нельзя редактировать чужую коллекцию");
        }
    }

    private List<CollectionEntity> sortCollections(List<CollectionEntity> list, String sort) {
        Comparator<CollectionEntity> comparator = switch (sort == null ? "new" : sort) {
            case "alphabet", "title" -> Comparator.comparing(c -> c.getTitle().toLowerCase());
            case "popular" -> Comparator.comparing((CollectionEntity c) -> itemRepository.findByCollectionId(c.getId()).size()).reversed();
            default -> Comparator.comparing(CollectionEntity::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed();
        };
        return list.stream().sorted(comparator).toList();
    }

    private List<Item> sortItems(List<Item> list, String sort) {
        Comparator<Item> comparator = switch (sort == null ? "alphabet" : sort) {
            case "likes" -> Comparator.comparing((Item i) -> itemLikeRepository.countByIdItemId(i.getId())).reversed();
            case "price" -> Comparator.comparing(i -> i.getPriceAmount() == null ? BigDecimal.ZERO : i.getPriceAmount(), Comparator.reverseOrder());
            default -> Comparator.comparing(i -> i.getTitle().toLowerCase());
        };
        return list.stream().sorted(comparator).toList();
    }

    private String blankToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private String categoryOrNull(String value) { return value == null || value.isBlank() || "all".equals(value) ? null : value; }
}
