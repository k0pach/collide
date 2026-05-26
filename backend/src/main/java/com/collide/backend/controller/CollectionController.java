package com.collide.backend.controller;

import com.collide.backend.dto.*;
import com.collide.backend.dto.request.CollectionRequest;
import com.collide.backend.dto.request.CommentRequest;
import com.collide.backend.dto.request.RatingRequest;
import com.collide.backend.model.entity.AppUser;
import com.collide.backend.service.CollectionService;
import com.collide.backend.service.CurrentUserService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/collections")
public class CollectionController {
    private final CollectionService collectionService;
    private final CurrentUserService currentUserService;

    public CollectionController(CollectionService collectionService, CurrentUserService currentUserService) {
        this.collectionService = collectionService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public List<CollectionSummaryDto> list(@RequestParam(required = false) UUID ownerId,
                                           @RequestParam(required = false) String category,
                                           @RequestParam(required = false) String q,
                                           @RequestParam(required = false) String sort,
                                           @RequestHeader(value = "X-User-Id", required = false) UUID userId) {
        UUID currentUserId = currentUserService.currentUserId(userId).orElse(null);
        return collectionService.list(ownerId, category, q, sort, currentUserId);
    }

    @GetMapping("/{id}")
    public CollectionDetailDto detail(@PathVariable UUID id,
                                      @RequestParam(required = false) String itemSort,
                                      @RequestHeader(value = "X-User-Id", required = false) UUID userId) {
        UUID currentUserId = currentUserService.currentUserId(userId).orElse(null);
        return collectionService.detail(id, currentUserId, itemSort);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CollectionDetailDto create(@Valid @RequestBody CollectionRequest request,
                                      @RequestHeader(value = "X-User-Id", required = false) UUID userId) {
        AppUser current = currentUserService.currentUser(userId);
        return collectionService.create(current.getId(), request);
    }

    @PutMapping("/{id}")
    public CollectionDetailDto update(@PathVariable UUID id, @Valid @RequestBody CollectionRequest request,
                                      @RequestHeader(value = "X-User-Id", required = false) UUID userId) {
        AppUser current = currentUserService.currentUser(userId);
        return collectionService.update(id, current.getId(), request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id, @RequestHeader(value = "X-User-Id", required = false) UUID userId) {
        AppUser current = currentUserService.currentUser(userId);
        collectionService.delete(id, current.getId());
    }

    @DeleteMapping("/{collectionId}/items/{itemId}")
    public CollectionDetailDto removeItem(@PathVariable UUID collectionId, @PathVariable UUID itemId,
                                          @RequestHeader(value = "X-User-Id", required = false) UUID userId) {
        AppUser current = currentUserService.currentUser(userId);
        return collectionService.removeItem(collectionId, itemId, current.getId());
    }

    @PostMapping("/{id}/rating")
    public CollectionDetailDto rate(@PathVariable UUID id, @Valid @RequestBody RatingRequest request,
                                    @RequestHeader(value = "X-User-Id", required = false) UUID userId) {
        AppUser current = currentUserService.currentUser(userId);
        return collectionService.rate(id, current.getId(), request);
    }

    @PostMapping("/{id}/favorite")
    public Map<String, Boolean> toggleFavorite(@PathVariable UUID id,
                                               @RequestHeader(value = "X-User-Id", required = false) UUID userId) {
        AppUser current = currentUserService.currentUser(userId);
        return Map.of("favorite", collectionService.toggleFavorite(id, current.getId()));
    }

    @GetMapping("/{id}/comments")
    public List<CommentDto> comments(@PathVariable UUID id) { return collectionService.comments(id); }

    @PostMapping("/{id}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto addComment(@PathVariable UUID id, @Valid @RequestBody CommentRequest request,
                                 @RequestHeader(value = "X-User-Id", required = false) UUID userId) {
        AppUser current = currentUserService.currentUser(userId);
        return collectionService.addComment(id, current.getId(), request);
    }
}
