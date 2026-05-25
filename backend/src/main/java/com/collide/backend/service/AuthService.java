package com.collide.backend.service;

import com.collide.backend.dto.AuthResponseDto;
import com.collide.backend.dto.request.LoginRequest;
import com.collide.backend.dto.request.RegisterRequest;
import com.collide.backend.exception.BadRequestException;
import com.collide.backend.exception.UnauthorizedException;
import com.collide.backend.model.entity.AppUser;
import com.collide.backend.model.enums.UserRole;
import com.collide.backend.repository.UserRepository;
import com.collide.backend.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final DtoMapper mapper;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, DtoMapper mapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.mapper = mapper;
    }

    @Transactional
    public AuthResponseDto register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        String username = request.username().trim().toLowerCase();
        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new BadRequestException("Пользователь с таким email уже существует");
        }
        if (userRepository.findByUsernameIgnoreCase(username).isPresent()) {
            throw new BadRequestException("Пользователь с таким username уже существует");
        }

        AppUser user = new AppUser();
        user.setEmail(email);
        user.setUsername(username);
        user.setDisplayName(request.displayName().trim());
        user.setBio(request.bio());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(UserRole.USER);
        user.setEnabled(true);
        user.setStatusMessage("Заходил недавно");
        userRepository.save(user);
        return response(user);
    }

    @Transactional(readOnly = true)
    public AuthResponseDto login(LoginRequest request) {
        String login = request.login().trim();
        AppUser user = userRepository.findByEmailIgnoreCase(login)
                .or(() -> userRepository.findByUsernameIgnoreCase(login))
                .orElseThrow(() -> new UnauthorizedException("Неверный логин или пароль"));
        if (!user.isEnabled() || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Неверный логин или пароль");
        }
        return response(user);
    }

    private AuthResponseDto response(AppUser user) {
        return new AuthResponseDto(jwtService.generateToken(user), "Bearer", mapper.user(user, false));
    }
}
