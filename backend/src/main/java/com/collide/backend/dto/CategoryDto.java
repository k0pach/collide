package com.collide.backend.dto;

import java.util.UUID;

public record CategoryDto(UUID uuid, String id, String slug, String title, int sortOrder) {}
