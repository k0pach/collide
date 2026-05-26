package com.collide.backend.service;

import com.collide.backend.exception.NotFoundException;
import com.collide.backend.exception.UnauthorizedException;
import com.collide.backend.model.entity.AppUser;
import com.collide.backend.repository.UserRepository;
import com.collide.backend.security.AppUserPrincipal;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {
    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public AppUser currentUser(UUID headerUserId) {
        UUID currentId = currentUserId(headerUserId)
                .orElseThrow(() -> new UnauthorizedException("Необходимо войти в аккаунт"));
        return find(currentId, "Пользователь не найден");
    }

    public Optional<UUID> currentUserId(UUID headerUserId) {
        Optional<UUID> authenticatedUserId = authenticatedUserId();
        if (authenticatedUserId.isPresent()) return authenticatedUserId;
        return Optional.ofNullable(headerUserId);
    }

    public Optional<UUID> authenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) return Optional.empty();
        Object principal = authentication.getPrincipal();
        if (principal instanceof AppUserPrincipal userPrincipal) return Optional.of(userPrincipal.getId());
        return Optional.empty();
    }

    private AppUser find(UUID id, String message) {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException(message));
    }
}
