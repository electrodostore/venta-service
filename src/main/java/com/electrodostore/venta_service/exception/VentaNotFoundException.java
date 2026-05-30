package com.electrodostore.venta_service.exception;

import lombok.Getter;

@Getter
public class VentaNotFoundException extends BusinessException{
    //Código de error identificativo
    private final VentaErrorCode errorCode;

    public VentaNotFoundException(String message){
        super(message);
        this.errorCode = VentaErrorCode.VENTA_NOT_FOUND;

    }
}
