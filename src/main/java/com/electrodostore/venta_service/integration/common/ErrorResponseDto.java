package com.electrodostore.venta_service.integration.common;

import lombok.Getter;
import lombok.Setter;

@Getter  @Setter
/*Clase DTO para transferencia de los datos del body (JSON) de la Response de un servicio de integración a objeto Java siempre y
cuando el statusCode de esta sea diferente a 2xx*/
public class ErrorResponseDto {

    //Del JSON solo es necesario sacar el errorCode y el mensaje para construir la determinada excepción
    private String errorCode;
    private String mensaje;
}
