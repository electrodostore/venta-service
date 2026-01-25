package com.electrodostore.venta_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
/*/Esta clase me va a recibir los datos que sean enviados desde producto-service para después copiarlos al
correspondiente Snapshot y persistirlos en la base de datos*/
public class ProductoIntegrationDto {

    private Long id;
    private String name;
    private Integer stock;
    private Double price;
    private String description;
}
