package com.electrodostore.venta_service.integration.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter  @Setter
/*Clase DTO para transferencia de los datos del body (JSON) de la Response de un servicio de integración a objeto Java siempre y
cuando el statusCode de esta sea diferente a 2xx*/
@JsonIgnoreProperties(ignoreUnknown = true) /*->Annotation de Jackson para ignorar aquellos campos del body de la Response que no
son necesarios o son redundantes en este dominio (timestamp, error, status, etc.)*/
public class ErrorResponseDto {

    //Del JSON solo es necesario sacar el errorCode y el mensaje para construir la determinada excepción
    private String errorCode;
    private String mensaje;
}
