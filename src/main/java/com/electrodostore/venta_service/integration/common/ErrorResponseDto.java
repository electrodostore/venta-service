package com.electrodostore.venta_service.integration.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter  @Setter
// Permite deserializar respuestas aunque contengan campos no utilizados por este servicio.
@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorResponseDto {

    private String errorCode;
    private String mensaje;
}
