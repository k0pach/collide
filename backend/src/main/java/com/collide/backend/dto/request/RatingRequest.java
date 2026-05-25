package com.collide.backend.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record RatingRequest(@Min(1) @Max(5) short rating) {}
