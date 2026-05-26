package com.collide.backend.controller;

import com.collide.backend.dto.ProfileStatsDto;
import com.collide.backend.dto.UserDto;
import com.collide.backend.dto.request.UserUpdateRequest;
import com.collide.backend.model.entity.AppUser;
import com.collide.backend.service.CurrentUserService;
import com.collide.backend.service.UserService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final CurrentUserService currentUserService;
    private final UserService userService;

    public UserController(CurrentUserService currentUserService, UserService userService) {
        this.currentUserService = currentUserService;
        this.userService = userService;
    }

    @GetMapping("/me")
    public UserDto me(@RequestHeader(value = "X-User-Id", required = false) UUID userId) {
        AppUser current = currentUserService.currentUser(userId);
        return userService.getUser(current.getId(), current.getId());
    }

    @PutMapping("/me")
    public UserDto updateMe(@Valid @RequestBody UserUpdateRequest request,
                            @RequestHeader(value = "X-User-Id", required = false) UUID userId) {
        AppUser current = currentUserService.currentUser(userId);
        return userService.updateMe(current.getId(), request);
    }

    @GetMapping("/{id}")
    public UserDto get(@PathVariable UUID id, @RequestHeader(value = "X-User-Id", required = false) UUID userId) {
        UUID currentUserId = currentUserService.currentUserId(userId).orElse(null);
        return userService.getUser(id, currentUserId);
    }

    @GetMapping("/{id}/stats")
    public ProfileStatsDto stats(@PathVariable UUID id) { return userService.stats(id); }

    @PostMapping("/{id}/follow")
    public UserDto follow(@PathVariable UUID id, @RequestHeader(value = "X-User-Id", required = false) UUID userId) {
        AppUser current = currentUserService.currentUser(userId);
        return userService.follow(current.getId(), id);
    }

    @DeleteMapping("/{id}/follow")
    public UserDto unfollow(@PathVariable UUID id, @RequestHeader(value = "X-User-Id", required = false) UUID userId) {
        AppUser current = currentUserService.currentUser(userId);
        return userService.unfollow(current.getId(), id);
    }
}
