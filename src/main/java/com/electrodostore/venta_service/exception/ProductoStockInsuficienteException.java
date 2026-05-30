package com.electrodostore.venta_service.exception;

import lombok.Getter;

@Getter
public class ProductoStockInsuficienteException extends BusinessException{
    private final VentaErrorCode errorCode;

    public ProductoStockInsuficienteException(String message){
        super(message);
        errorCode = VentaErrorCode.PRODUCT_STOCK_INSUFICIENTE;
    }
}
