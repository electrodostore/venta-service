package com.electrodostore.venta_service.dto;

import com.electrodostore.venta_service.model.VentaStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

//Clase de objetos de transferencia de datos para exponer a una venta al cliente (view)
public record VentaResponseDto(
        Long id,
        LocalDate date,
        Integer totalItems,
        BigDecimal totalPrice,
        VentaStatus status,
        //También se exponen los DTO de los objetos embebidos de producto y cliente
        List<ProductoResponseDto> productsList,
        ClienteResponseDto client
) {

}
