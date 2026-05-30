package com.electrodostore.venta_service.exception;

import lombok.Getter;

@Getter
public class ClienteNotFoundException extends BusinessException{
    private final VentaErrorCode errorCode;

    public ClienteNotFoundException(String message){
        super(message);
        this.errorCode = VentaErrorCode.CLIENT_NOT_FOUND;
    }

}
