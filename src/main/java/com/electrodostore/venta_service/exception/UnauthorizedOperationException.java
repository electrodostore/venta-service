package com.electrodostore.venta_service.exception;

import lombok.Getter;

//Excepción personalizada para evitar que usuarios no autorizados accedan a recursos privados
@Getter
public class UnauthorizedOperationException extends BusinessException {
    private final VentaErrorCode errorCode;

    public UnauthorizedOperationException(String message) {
        super(message);
        this.errorCode = VentaErrorCode.UNAUTHORIZED_OPERATION;
    }
}
