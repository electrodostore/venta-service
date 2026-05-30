package com.electrodostore.venta_service.dto;

import java.math.BigDecimal;

public record ProductoResponseDto(
        Long id,
        String name,
        BigDecimal price,
        Integer purchasedQuantity,
        BigDecimal subTotal,
        String description
) {}
