package com.electrodostore.venta_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter  @Setter
@AllArgsConstructor
@NoArgsConstructor
//DTO encargado de transferir el dato mínimo necesario cuando se crea una venta --> El ID de esta
//Si después se necesita más detalle, se podrá consultar por este mismo ID
public class VentaCreadaDto {

    private Long ventaId;
}
