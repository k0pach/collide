package com.collide.backend.controller;

import com.collide.backend.dto.*;
import com.collide.backend.dto.request.CommentRequest;
import com.collide.backend.dto.request.ItemRequest;
import com.collide.backend.model.entity.AppUser;
import com.collide.backend.service.CurrentUserService;
import com.collide.backend.service.ItemService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/items")
public class ItemController {
    private final ItemService itemService;
    private final CurrentUserService currentUserService;

    public ItemController(ItemService itemService, CurrentUserService currentUserService) {
        this.itemService = itemService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public List<ItemSummaryDto> list(@RequestParam(required = false) UUID ownerId,
                                     @RequestParam(required = false) UUID collectionId,
                                     @RequestParam(required = false) String category,
                                     @RequestParam(required = false) String q,
                                     @RequestParam(required = false) String sort,
                                     @RequestHeader(value = "X-User-Id", required = false) UUID userId) {
        UUID currentUserId = currentUserService.currentUserId(userId).orElse(null);
        return itemService.list(ownerId, collectionId, category, q, sort, currentUserId);
    }

    @GetMapping("/{id}")
    public ItemDetailDto detail(@PathVariable UUID id, @RequestHeader(value = "X-User-Id", required = false) UUID userId) {
        UUID currentUserId = currentUserService.currentUserId(userId).orElse(null);
        return itemService.detail(id, currentUserId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDetailDto create(@Valid @RequestBody ItemRequest request,
                                @RequestHeader(value = "X-User-Id", required = false) UUID userId) {
        AppUser current = currentUserService.currentUser(userId);
        return itemService.create(current.getId(), request);
    }

    @PutMapping("/{id}")
    public ItemDetailDto update(@PathVariable UUID id, @Valid @RequestBody ItemRequest request,
                                @RequestHeader(value = "X-User-Id", required = false) UUID userId) {
        AppUser current = currentUserService.currentUser(userId);
        return itemService.update(id, current.getId(), request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id, @RequestHeader(value = "X-User-Id", required = false) UUID userId) {
        AppUser current = currentUserService.currentUser(userId);
        itemService.delete(id, current.getId());
    }

    @PostMapping("/{id}/like")
    public Map<String, Boolean> toggleLike(@PathVariable UUID id,
                                           @RequestHeader(value = "X-User-Id", required = false) UUID userId) {
        AppUser current = currentUserService.currentUser(userId);
        return Map.of("liked", itemService.toggleLike(id, current.getId()));
    }

    @PostMapping("/{id}/favorite")
    public Map<String, Boolean> toggleFavorite(@PathVariable UUID id,
                                               @RequestHeader(value = "X-User-Id", required = false) UUID userId) {
        AppUser current = currentUserService.currentUser(userId);
        return Map.of("favorite", itemService.toggleFavorite(id, current.getId()));
    }

    @GetMapping("/{id}/comments")
    public List<CommentDto> comments(@PathVariable UUID id) { return itemService.comments(id); }

    @PostMapping("/{id}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto addComment(@PathVariable UUID id, @Valid @RequestBody CommentRequest request,
                                 @RequestHeader(value = "X-User-Id", required = false) UUID userId) {
        AppUser current = currentUserService.currentUser(userId);
        return itemService.addComment(id, current.getId(), request);
    }
}
