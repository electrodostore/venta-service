package com.electrodostore.venta_service.dto;

import java.math.BigDecimal;

//Clase DTO que me expone los diferentes productos asignados a una venta
public record ProductoResponseDto(
        Long id,
        String name,
        BigDecimal price,
        Integer purchasedQuantity,
        BigDecimal subTotal,
        String description
) {}
