package com.collide.backend.dto;

public record AuthResponseDto(String token, String tokenType, UserDto user) {}
