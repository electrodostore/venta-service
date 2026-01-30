package com.electrodostore.venta_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
//Clase Dto que me expone los diferentes productos asignados a un venta
public class ProductoResponseDto {

    private Long id;
    private String name;
    private BigDecimal price;
    private Integer purchasedQuantity;
    private BigDecimal subTotal;
    private String description;

}
