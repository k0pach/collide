package com.collide.backend.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

public record ItemRequest(
        @NotBlank @Size(max = 120) String title,
        UUID collectionId,
        String category,
        String categorySlug,
        String status,
        @DecimalMin(value = "0.0", inclusive = true) BigDecimal priceAmount,
        String price,
        @Size(max = 60) String description,
        @Size(max = 60) String shortDescription,
        String fullDescription,
        String imageUrl
) {}
