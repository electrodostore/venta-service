package com.electrodostore.venta_service.integration.producto.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter  @Setter
@AllArgsConstructor
@NoArgsConstructor
//DTO que se encarga de recoger los datos relevantes cuando se va a hacer una petición a producto-service para una operación sobre el stock de un producto
public class ProductoIntegrationStockDto {

    private Long productoId;
    private Integer cantidadOperar;
}
