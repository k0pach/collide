package com.collide.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ChatRequest(@NotNull UUID companionId) {}
