package com.electrodostore.venta_service.integration.producto.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO utilizado para operaciones de stock
 * sobre productos en producto-service.
 */
@Getter  @Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductoIntegrationStockDto {

    private Long productoId;
    private Integer cantidadOperar;
}
