package com.collide.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CollectionRequest(
        @NotBlank @Size(max = 120) String title,
        @Size(max = 5000) String description,
        String category,
        String categorySlug,
        String coverImageUrl
) {}
