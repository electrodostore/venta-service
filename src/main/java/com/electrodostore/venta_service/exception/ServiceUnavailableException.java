package com.electrodostore.venta_service.exception;

import lombok.Getter;

/**
 * Excepción para errores de infraestructura
 * en la integración con los microservicios
 */
@Getter
public class ServiceUnavailableException extends RuntimeException{
    private final VentaErrorCode errorCode;

    public ServiceUnavailableException(String message){
        super(message);
        this.errorCode = VentaErrorCode.SERVICE_UNAVAILABLE;
    }
}
