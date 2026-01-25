package com.electrodostore.venta_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
//Clase Dto para tranferir los datos que envia el cliente (view) a la clase que los va a persistir en la db
public class VentaRequestDto {

    private LocalDate date;
    //Lista de cada id y cantidad del producto que se va a agregar a la venta
    private Set<ProductoRequestDto> productsList;
    //Id del cliente dueño de la venta
    private Long clientId;
}
