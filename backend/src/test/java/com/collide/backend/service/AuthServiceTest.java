package com.collide.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.collide.backend.dto.AuthResponseDto;
import com.collide.backend.dto.UserDto;
import com.collide.backend.dto.request.LoginRequest;
import com.collide.backend.dto.request.RegisterRequest;
import com.collide.backend.exception.BadRequestException;
import com.collide.backend.exception.UnauthorizedException;
import com.collide.backend.model.entity.AppUser;
import com.collide.backend.model.enums.UserRole;
import com.collide.backend.repository.UserRepository;
import com.collide.backend.security.JwtService;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private DtoMapper mapper;

    private AuthService service;

    @BeforeEach
    void setUp() {
        service = new AuthService(userRepository, passwordEncoder, jwtService, mapper);
    }

    @Test
    void registerCreatesUserAndReturnsToken() {
        RegisterRequest request = new RegisterRequest("  USER@MAIL.COM  ", "  NewUser ", "secret123", "  New User  ", "bio");

        when(userRepository.findByEmailIgnoreCase("user@mail.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsernameIgnoreCase("newuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secret123")).thenReturn("encoded");
        when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> {
            AppUser user = invocation.getArgument(0);
            if (user.getId() == null) {
                user.setId(UUID.randomUUID());
            }
            return user;
        });
        when(jwtService.generateToken(any(AppUser.class))).thenReturn("jwt-token");
        when(mapper.user(any(AppUser.class), anyBoolean())).thenReturn(userDto());

        AuthResponseDto result = service.register(request);

        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        verify(userRepository).save(captor.capture());
        AppUser saved = captor.getValue();
        assertThat(saved.getEmail()).isEqualTo("user@mail.com");
        assertThat(saved.getUsername()).isEqualTo("newuser");
        assertThat(saved.getDisplayName()).isEqualTo("New User");
        assertThat(saved.getBio()).isEqualTo("bio");
        assertThat(saved.getPasswordHash()).isEqualTo("encoded");
        assertThat(saved.getRole()).isEqualTo(UserRole.USER);
        assertThat(saved.isEnabled()).isTrue();
        assertThat(saved.getStatusMessage()).isEqualTo("Заходил недавно");

        assertThat(result.token()).isEqualTo("jwt-token");
        assertThat(result.tokenType()).isEqualTo("Bearer");
        assertThat(result.user()).isNotNull();
    }

    @Test
    void registerThrowsWhenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest("test@mail.com", "name", "secret123", "Name", null);
        when(userRepository.findByEmailIgnoreCase("test@mail.com")).thenReturn(Optional.of(new AppUser()));

        assertThatThrownBy(() -> service.register(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Пользователь с таким email уже существует");
    }

    @Test
    void registerThrowsWhenUsernameAlreadyExists() {
        RegisterRequest request = new RegisterRequest("test@mail.com", "name", "secret123", "Name", null);
        when(userRepository.findByEmailIgnoreCase("test@mail.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsernameIgnoreCase("name")).thenReturn(Optional.of(new AppUser()));

        assertThatThrownBy(() -> service.register(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Пользователь с таким username уже существует");
    }

    @Test
    void loginByEmailReturnsToken() {
        AppUser user = user("john", "john@mail.com", true, "hash");
        LoginRequest request = new LoginRequest("  JOHN@mail.com ", "secret123");

        when(userRepository.findByEmailIgnoreCase("JOHN@mail.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret123", "hash")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");
        when(mapper.user(user, false)).thenReturn(userDto());

        AuthResponseDto result = service.login(request);

        assertThat(result.token()).isEqualTo("jwt-token");
        assertThat(result.tokenType()).isEqualTo("Bearer");
    }

    @Test
    void loginByUsernameReturnsToken() {
        AppUser user = user("john", "john@mail.com", true, "hash");
        LoginRequest request = new LoginRequest("  john  ", "secret123");

        when(userRepository.findByEmailIgnoreCase("john")).thenReturn(Optional.empty());
        when(userRepository.findByUsernameIgnoreCase("john")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret123", "hash")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");
        when(mapper.user(user, false)).thenReturn(userDto());

        AuthResponseDto result = service.login(request);

        assertThat(result.token()).isEqualTo("jwt-token");
    }

    @Test
    void loginThrowsWhenUserMissing() {
        when(userRepository.findByEmailIgnoreCase("missing")).thenReturn(Optional.empty());
        when(userRepository.findByUsernameIgnoreCase("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(new LoginRequest("missing", "pass")))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Неверный логин или пароль");
    }

    @Test
    void loginThrowsWhenUserDisabled() {
        AppUser user = user("john", "john@mail.com", false, "hash");
        when(userRepository.findByEmailIgnoreCase("john")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.login(new LoginRequest("john", "pass")))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Неверный логин или пароль");
    }

    @Test
    void loginThrowsWhenPasswordMismatch() {
        AppUser user = user("john", "john@mail.com", true, "hash");
        when(userRepository.findByEmailIgnoreCase("john")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pass", "hash")).thenReturn(false);

        assertThatThrownBy(() -> service.login(new LoginRequest("john", "pass")))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Неверный логин или пароль");
    }

    private AppUser user(String username, String email, boolean enabled, String passwordHash) {
        AppUser user = new AppUser();
        user.setId(UUID.randomUUID());
        user.setUsername(username);
        user.setEmail(email);
        user.setDisplayName("User");
        user.setEnabled(enabled);
        user.setPasswordHash(passwordHash);
        user.setRole(UserRole.USER);
        return user;
    }

    private UserDto userDto() {
        return new UserDto(UUID.randomUUID(), "user", "@user", "User", "User", "", null, "orange", "Online", false);
    }
}
