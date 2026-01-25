package com.electrodostore.venta_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
/*Clase Dto que me va a definir lo que el cliente me debe mandar desde la vista para consultar y guardar al
respectivo producto*/
public class ProductoRequestDto {

    private Long id;
    private Integer quantity;
}
