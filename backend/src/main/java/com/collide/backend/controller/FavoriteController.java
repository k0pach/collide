package com.collide.backend.controller;

import com.collide.backend.dto.FavoritesDto;
import com.collide.backend.model.entity.AppUser;
import com.collide.backend.service.CurrentUserService;
import com.collide.backend.service.FavoriteService;

import java.util.UUID;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {
    private final FavoriteService favoriteService;
    private final CurrentUserService currentUserService;

    public FavoriteController(FavoriteService favoriteService, CurrentUserService currentUserService) {
        this.favoriteService = favoriteService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public FavoritesDto favorites(@RequestParam(required = false) String q, @RequestParam(required = false) String sort, @RequestHeader(value = "X-User-Id", required = false) UUID userId) {
        AppUser current = currentUserService.currentUser(userId);
        return favoriteService.favorites(current.getId(), q, sort);
    }
}
