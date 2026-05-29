package com.electrodostore.venta_service.dto;

/*Clase Dto que me va a definir lo que el cliente me debe mandar desde la vista para consultar y guardar al
respectivo producto*/
public record ProductoRequestDto(
        Long id,
        Integer quantity
) {}
