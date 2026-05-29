package com.electrodostore.venta_service.dto;

//Clase Dto que me expone los datos del cliente de una determinada  venta
public record ClienteResponseDto(
        Long id,
        String name,
        String cellphone,
        String document,
        String address
) {}
