package com.electrodostore.venta_service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ProductoRequestDto(
        @NotNull Long id,
        @NotNull @PositiveOrZero Integer quantity
) {}
