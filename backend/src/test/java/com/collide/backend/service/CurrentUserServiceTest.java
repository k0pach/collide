package com.collide.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.collide.backend.exception.NotFoundException;
import com.collide.backend.exception.UnauthorizedException;
import com.collide.backend.model.entity.AppUser;
import com.collide.backend.model.enums.UserRole;
import com.collide.backend.repository.UserRepository;
import com.collide.backend.security.AppUserPrincipal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class CurrentUserServiceTest {

    @Mock
    private UserRepository userRepository;

    private CurrentUserService service;

    @BeforeEach
    void setUp() {
        service = new CurrentUserService(userRepository);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void authenticatedUserIdReturnsEmptyWhenAuthenticationIsMissing() {
        assertThat(service.authenticatedUserId()).isEmpty();
    }

    @Test
    void authenticatedUserIdReturnsEmptyWhenAuthenticationIsNotAuthenticated() {
        setAuthentication(false, "principal");

        assertThat(service.authenticatedUserId()).isEmpty();
    }

    @Test
    void authenticatedUserIdReturnsEmptyWhenPrincipalIsNotAppUserPrincipal() {
        setAuthentication(true, "plain-string-principal");

        assertThat(service.authenticatedUserId()).isEmpty();
    }

    @Test
    void authenticatedUserIdReturnsPrincipalIdWhenPresent() {
        UUID id = UUID.randomUUID();
        setAuthentication(true, appUserPrincipal(id));

        assertThat(service.authenticatedUserId()).contains(id);
    }

    @Test
    void currentUserIdPrefersAuthenticatedIdOverHeaderId() {
        UUID authId = UUID.randomUUID();
        UUID headerId = UUID.randomUUID();
        setAuthentication(true, appUserPrincipal(authId));

        assertThat(service.currentUserId(headerId)).contains(authId);
    }

    @Test
    void currentUserIdFallsBackToHeaderId() {
        UUID headerId = UUID.randomUUID();

        assertThat(service.currentUserId(headerId)).contains(headerId);
        assertThat(service.currentUserId(null)).isEqualTo(Optional.empty());
    }

    @Test
    void currentUserThrowsUnauthorizedWhenNoAuthenticatedOrHeaderId() {
        assertThatThrownBy(() -> service.currentUser(null))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Необходимо войти в аккаунт");
    }

    @Test
    void currentUserThrowsNotFoundWhenUserDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.currentUser(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Пользователь не найден");
    }

    @Test
    void currentUserReturnsUserWhenFoundByResolvedId() {
        UUID id = UUID.randomUUID();
        AppUser user = appUser(id);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        assertThat(service.currentUser(id)).isSameAs(user);
    }

    private void setAuthentication(boolean authenticated, Object principal) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(authenticated);
        if (authenticated) {
            when(authentication.getPrincipal()).thenReturn(principal);
        }

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    private AppUserPrincipal appUserPrincipal(UUID id) {
        return new AppUserPrincipal(appUser(id));
    }

    private AppUser appUser(UUID id) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setUsername("jane");
        user.setPasswordHash("hash");
        user.setRole(UserRole.USER);
        user.setEnabled(true);
        return user;
    }
}
