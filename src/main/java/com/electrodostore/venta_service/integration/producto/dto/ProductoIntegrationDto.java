package com.electrodostore.venta_service.integration.producto.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductoIntegrationDto {

    private Long id;
    private String name;
    private Integer stock;
    private BigDecimal price;
    private String description;
}
