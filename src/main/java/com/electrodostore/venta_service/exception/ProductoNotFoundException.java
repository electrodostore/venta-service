package com.electrodostore.venta_service.exception;

import lombok.Getter;

@Getter
public class ProductoNotFoundException extends BusinessException{
    private final VentaErrorCode errorCode;

    public ProductoNotFoundException(String message){
        super(message);
        this.errorCode = VentaErrorCode.PRODUCT_NOT_FOUND;
    }

}
