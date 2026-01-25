package com.electrodostore.venta_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
//Clase Dto que me expone los datos del cliente de una determinada  venta
public class ClienteResponseDto {

    private Long id;
    private String name;
    private String cellphone;
    private String document;
    private String address;
}
