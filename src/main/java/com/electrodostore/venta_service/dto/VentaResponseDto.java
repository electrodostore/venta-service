package com.electrodostore.venta_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
//Clase de objetos de transferencia de datos para exponer a una venta al cliente (view)
public class VentaResponseDto {

    private Long id;
    private LocalDate date;
    private Integer totalItems;
    private BigDecimal totalPrice;

    //También se exponen los DTO de los objetos embebidos de producto y cliente
    private List<ProductoResponseDto> productsList = new ArrayList<>();
    private ClienteResponseDto client;
}
