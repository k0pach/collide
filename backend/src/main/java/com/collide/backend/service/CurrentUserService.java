package com.collide.backend.service;

import com.collide.backend.exception.NotFoundException;
import com.collide.backend.model.entity.AppUser;
import com.collide.backend.repository.UserRepository;
import com.collide.backend.security.AppUserPrincipal;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {
    private final UserRepository userRepository;
    private final String defaultUsername;

    public CurrentUserService(UserRepository userRepository, @Value("${collide.dev.default-user:jesseyo}") String defaultUsername) {
        this.userRepository = userRepository;
        this.defaultUsername = defaultUsername;
    }

    public AppUser currentUser(UUID headerUserId) {
        Optional<UUID> authenticatedUserId = authenticatedUserId();
        if (authenticatedUserId.isPresent()) {
            return find(authenticatedUserId.get(), "Пользователь из JWT не найден");
        }
        if (headerUserId != null) {
            return find(headerUserId, "Пользователь из X-User-Id не найден");
        }
        return userRepository.findByUsernameIgnoreCase(defaultUsername)
                .or(() -> userRepository.findAll().stream().findFirst())
                .orElseThrow(() -> new NotFoundException("В базе нет пользователей. Зарегистрируйтесь или включите демо-инициализацию."));
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
