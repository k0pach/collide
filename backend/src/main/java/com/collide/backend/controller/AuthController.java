package com.collide.backend.controller;

import com.collide.backend.dto.AuthResponseDto;
import com.collide.backend.dto.UserDto;
import com.collide.backend.dto.request.LoginRequest;
import com.collide.backend.dto.request.RegisterRequest;
import com.collide.backend.model.entity.AppUser;
import com.collide.backend.service.AuthService;
import com.collide.backend.service.CurrentUserService;
import com.collide.backend.service.UserService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final CurrentUserService currentUserService;
    private final UserService userService;

    public AuthController(AuthService authService, CurrentUserService currentUserService, UserService userService) {
        this.authService = authService;
        this.currentUserService = currentUserService;
        this.userService = userService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponseDto register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponseDto login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public UserDto me(@RequestHeader(value = "X-User-Id", required = false) UUID userId) {
        AppUser current = currentUserService.currentUser(userId);
        return userService.getUser(current.getId(), current.getId());
    }
}
